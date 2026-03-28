package com.api.moviebooking.models.dtos.movie;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MovieDataResponse {

    private UUID movieId;
    private String title;
    private String genre;
    private String description;
    private Integer duration;
    private Integer minimumAge;
    private String imdbId;
    private Integer releaseYear;
    private Double imdbRating;
    private String region;
    private String director;
    private String actors;
    private String posterUrl;
    private String posterCloudinaryId;
    private String trailerUrl;
    private String status;
    private String language;
}
