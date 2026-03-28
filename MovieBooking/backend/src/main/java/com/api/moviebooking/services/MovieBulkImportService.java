package com.api.moviebooking.services;

import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.api.moviebooking.helpers.exceptions.CustomException;
import com.api.moviebooking.models.entities.Movie;
import com.api.moviebooking.repositories.MovieRepo;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieBulkImportService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final RestTemplate restTemplate;
    private final ImdbSyncService imdbSyncService;
    private final MovieRepo movieRepo;

    @Value("${imdb.api.base-url:https://api.imdbapi.dev}")
    private String imdbApiBaseUrl;

    @Value("${imdb.bulk-import.delay-ms:1200}")
    private long delayMs;

    @Async
    public CompletableFuture<Void> importMoviesByGenre(String genre, int limit) {
        if (genre == null || genre.isBlank()) {
            throw new IllegalArgumentException("genre must not be blank");
        }

        int normalizedLimit = normalizeLimit(limit);
        List<String> imdbIds = fetchImdbIdsByGenre(genre.trim(), normalizedLimit);
        int total = imdbIds.size();

        log.info("Bulk import started. genre={}, requestedLimit={}, fetchedIds={}, delayMs={}, startedAt={}",
                genre, normalizedLimit, total, delayMs, Instant.now());

        for (int index = 0; index < total; index++) {
            String imdbId = imdbIds.get(index);
            int progress = index + 1;

            log.info("Dang nhap phim thu {}/{}... imdbId={}", progress, total, imdbId);

            try {
                if (movieRepo.existsByImdbId(imdbId)) {
                    log.info("Skip imdbId={} because it already exists", imdbId);
                } else {
                    Movie importedMovie = imdbSyncService.syncMovieByImdbId(imdbId);
                    log.info("Imported movie imdbId={} movieId={} title={}",
                            importedMovie.getImdbId(), importedMovie.getId(), importedMovie.getTitle());
                }
            } catch (Exception exception) {
                log.warn("Failed to import imdbId={}: {}", imdbId, exception.getMessage());
            }

            applyRateLimitDelay();
        }

        log.info("Bulk import completed. genre={}, totalProcessed={}, finishedAt={}", genre, total, Instant.now());
        return CompletableFuture.completedFuture(null);
    }

    private List<String> fetchImdbIdsByGenre(String genre, int limit) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromHttpUrl(imdbApiBaseUrl + "/titles")
                .queryParam("genre", genre)
                .queryParam("limit", limit);

        try {
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(uriBuilder.build(true).toUri(), JsonNode.class);
            JsonNode body = response.getBody();
            if (body == null || body.isNull()) {
                return List.of();
            }

            JsonNode titlesNode = unwrapTitleArray(body);
            if (titlesNode == null || !titlesNode.isArray()) {
                return List.of();
            }

            Set<String> uniqueIds = new LinkedHashSet<>();
            for (JsonNode titleNode : titlesNode) {
                String imdbId = firstText(titleNode, "id", "imdbId", "tconst");
                if (imdbId != null && !imdbId.isBlank()) {
                    uniqueIds.add(imdbId.trim());
                }
                if (uniqueIds.size() >= limit) {
                    break;
                }
            }

            return new ArrayList<>(uniqueIds);
        } catch (ResourceAccessException resourceAccessException) {
            Throwable rootCause = resourceAccessException.getMostSpecificCause();
            if (rootCause instanceof SocketTimeoutException) {
                throw new CustomException("IMDb API timeout while fetching titles by genre", HttpStatus.GATEWAY_TIMEOUT);
            }
            throw new CustomException("Cannot access IMDb API: " + resourceAccessException.getMessage(),
                    HttpStatus.BAD_GATEWAY);
        } catch (RestClientException restClientException) {
            throw new CustomException("IMDb API error: " + restClientException.getMessage(), HttpStatus.BAD_GATEWAY);
        }
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private void applyRateLimitDelay() {
        if (delayMs <= 0) {
            return;
        }

        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            log.warn("Bulk import delay interrupted: {}", interruptedException.getMessage());
        }
    }

    private JsonNode unwrapTitleArray(JsonNode body) {
        if (body.isArray()) {
            return body;
        }

        String[] arrayPaths = { "titles", "results", "data", "items", "content" };
        for (String path : arrayPaths) {
            JsonNode node = body.get(path);
            if (node != null && node.isArray()) {
                return node;
            }
        }

        JsonNode nestedData = body.get("data");
        if (nestedData != null && nestedData.isObject()) {
            for (String path : arrayPaths) {
                JsonNode node = nestedData.get(path);
                if (node != null && node.isArray()) {
                    return node;
                }
            }
        }

        return null;
    }

    private String firstText(JsonNode payload, String... paths) {
        for (String path : paths) {
            JsonNode node = findNode(payload, path);
            if (node != null && node.isTextual()) {
                String value = node.asText().trim();
                if (!value.isBlank()) {
                    return value;
                }
            }
        }
        return null;
    }

    private JsonNode findNode(JsonNode payload, String path) {
        if (payload == null || payload.isNull() || path == null || path.isBlank()) {
            return null;
        }

        JsonNode current = payload;
        String[] segments = path.split("\\.");
        for (String segment : segments) {
            if (current == null || current.isNull()) {
                return null;
            }
            current = current.get(segment);
        }

        return current;
    }
}
