import { apiFetch } from "./fetchConfig";

function mapUserRating(row) {
  return {
    ratingId: row?.ratingId || row?.rating_id || null,
    movieId: row?.movieId || row?.movie_id || null,
    movieTitle: row?.movieTitle || row?.movie_title || "",
    posterUrl: row?.posterUrl || row?.poster_url || "",
    ratingValue: Number(row?.ratingValue ?? row?.rating_value ?? 0),
    createdAt: row?.createdAt || row?.created_at || null,
  };
}

export async function getMyRatings() {
  const res = await apiFetch("/user-ratings/me");
  const data = Array.isArray(res?.data) ? res.data : Array.isArray(res) ? res : [];
  return data.map(mapUserRating);
}

export async function upsertMovieRating(movieId, ratingValue) {
  const payload = {
    movieId,
    ratingValue,
  };

  const res = await apiFetch("/user-ratings", {
    method: "POST",
    body: JSON.stringify(payload),
  });

  return mapUserRating(res?.data || res || {});
}
