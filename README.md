# Movie Booking Uncertainty Score

Cinema booking platform with a recommendation engine that ranks movies by **uncertainty score** and explains each suggestion with the movie that influenced it most.

The repo contains a Spring Boot backend and a React/Vite frontend. Users can browse movies, book tickets, rate watched films, and see personalized Top-K recommendations with confidence/uncertainty indicators.

## Highlights

- Personalized recommendation Top-K for logged-in users
- Uncertainty score-based ranking and explainability
- Movie detail page with user rating, IMDb rating, genre, language, region, and release year
- Page to review movies you already rated
- Admin/movie management, cinemas, showtimes, bookings, checkout, payments, promotions, memberships
- Redis-based seat locking and booking flow
- Docker Compose development setup

## Recommendation Engine

This project emphasizes movie recommendation with **uncertainty score**.

### What the API returns

The recommendation endpoint returns each suggested movie with:

- `predictedRating`: predicted user rating on a 0-5 scale
- `uncertaintyScore`: how uncertain the prediction is, from 0-100
- `nearestMovieId`, `nearestMovieTitle`, `nearestMoviePosterUrl`: the most relevant historical movie used to explain the prediction

### How it is used in the UI

- `Độ phù hợp` is shown as `predictedRating × 20%`
- `Độ tin cậy` is shown as `100 - uncertaintyScore`
- The homepage Top-K section also shows:
	- `Vì bạn đã đánh giá {Tên phim}`
	- clicking the title navigates to the source movie detail page

This makes the recommendation transparent and easy to compare manually against movies the user already rated.

## Tech Stack

**Backend**: Spring Boot 3.5.6, Java 21, PostgreSQL, Redis, JWT, Spring Security, JPA/Hibernate, MapStruct, Swagger/OpenAPI

**Frontend**: React 18, Vite, React Router, TailwindCSS, Framer Motion, Playwright, Vitest

**Infra & Ops**: Docker Compose, Prometheus, Grafana, k6

## Repository Structure

```text
MovieBooking/
├── backend/                 Spring Boot API
│   ├── src/main/java/       Application source
│   ├── src/test/java/       Tests
│   └── docs/                API documentation
├── infrastructure/          Terraform assets
├── k6-tests/                Load/performance scenarios
├── docker-compose.yml       Local dev stack
└── movie-booking-frontend/  React/Vite client
```

## Quick Start

### 1. Prerequisites

- Java 21
- Node.js 18+
- npm 9+
- Docker and Docker Compose (recommended)

### 2. Backend environment

Create `MovieBooking/backend/.env` for local development. The exact values depend on your setup, but a typical file includes:

```env
POSTGRES_DB=moviebooking
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_password
DATA_SOURCE_URL=jdbc:postgresql://db:5432/moviebooking

REDIS_HOST=redis
REDIS_PORT=6379

JWT_SECRET=your-secret-key-min-256-bits
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=86400000

MOMO_PARTNER_CODE=your_partner_code
MOMO_ACCESS_KEY=your_access_key
MOMO_SECRET_KEY=your_secret_key
MOMO_ENDPOINT=https://test-payment.momo.vn/v2/gateway/api/create
MOMO_REDIRECT_URL=http://localhost:5173/payment/callback
MOMO_IPN_URL=http://localhost:8080/api/payments/momo/ipn

PAYPAL_CLIENT_ID=your_paypal_client_id
PAYPAL_CLIENT_SECRET=your_paypal_client_secret
PAYPAL_MODE=sandbox
PAYPAL_RETURN_URL=http://localhost:5173/payment/callback
PAYPAL_CANCEL_URL=http://localhost:5173/payment/cancelled

CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```

### 3. Frontend environment

Create `movie-booking-frontend/.env.local`:

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_NAME=MovieBooking
VITE_APP_VERSION=0.0.1
```

### 4. Run with Docker Compose

From the `MovieBooking/` root:

```bash
docker compose up --build
```

Useful commands:

```bash
docker compose logs -f
docker compose down
docker compose down -v
```

### 5. Run locally without Docker

Backend:

```bash
cd backend
./mvnw test
./mvnw spring-boot:run
```

Frontend:

```bash
cd movie-booking-frontend
npm install
npm run dev
```

## Main Pages and Flows

- Home page: Top-K recommendations, currently showing movies, upcoming movies
- Movie detail page: poster, metadata, user rating, showtimes, booking entry point
- My Ratings page: list of movies the current user has rated
- Checkout flow: lock seats, confirm booking, initiate payment
- Admin area: movies, cinemas, rooms, seats, showtimes, pricing, promotions, users, bookings, orders

## Recommendation-Related API Endpoints

- `GET /recommendations/top-k?k=5` - personalized Top-K movies for current user
- `GET /recommendations/predict?targetMovieId=...` - predicted rating for a movie
- `GET /user-ratings/me` - current user's ratings
- `GET /user-ratings/me/list?page=0&size=20` - paginated current user's ratings

## Testing

Backend:

```bash
cd backend
./mvnw test
```

Frontend:

```bash
cd movie-booking-frontend
npm run test:run
npm run test:e2e
```

## API Documentation

Backend docs are available in `MovieBooking/backend/docs/`.

Key docs include:

- [API-Authentication.md](MovieBooking/backend/docs/API-Authentication.md)
- [API-Bookings.md](MovieBooking/backend/docs/API-Bookings.md)
- [API-Checkout.md](MovieBooking/backend/docs/API-Checkout.md)
- [API-Movies.md](MovieBooking/backend/docs/API-Movies.md)
- [API-Payments.md](MovieBooking/backend/docs/API-Payments.md)
- [API-Promotions.md](MovieBooking/backend/docs/API-Promotions.md)
- [API-Showtimes.md](MovieBooking/backend/docs/API-Showtimes.md)

## Notes

- The frontend expects the backend API base URL to be set correctly in `movie-booking-frontend/.env.local`.
- The recommendation experience is designed for manual comparison: users can inspect the source movie behind each Top-K suggestion.
- If you run the app via Docker Compose, make sure the backend `.env` file is present first.

## License

University project / course work.
