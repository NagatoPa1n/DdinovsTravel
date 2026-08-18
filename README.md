# DdinovsTravel — Tour agency REST API

Spring Boot 3.4 / Java 17 / PostgreSQL / JWT. Base package `uz.nagato.touragency`.

## Running

1. Create the database:

   ```sql
   CREATE DATABASE touragency;
   ```

2. Set what differs from the defaults (see `src/main/resources/application.yml`):

   | Variable | Default |
   |---|---|
   | `DB_HOST` / `DB_PORT` / `DB_NAME` | `localhost` / `5432` / `touragency` |
   | `DB_USER` / `DB_PASSWORD` | `postgres` / `postgres` |
   | `JWT_SECRET` | dev-only value — **replace in production**, min 32 chars |
   | `UPLOAD_DIR` | `uploads` |
   | `CORS_ORIGINS` | `http://localhost:3000,http://localhost:5173` |
   | `ADMIN_EMAIL` / `ADMIN_PASSWORD` | `admin@ddinovstravel.uz` / `admin123` |

3. Start it:

   ```bash
   mvn spring-boot:run
   ```

Schema is created by Hibernate (`ddl-auto: update`) and an admin account is seeded on first
start. Swagger UI: <http://localhost:8080/swagger-ui.html>.

`mvn test` runs the context and API smoke tests against in-memory H2 — no database needed.

## Response shape

Every endpoint returns the same envelope; list endpoints wrap a page.

```json
{ "success": true, "message": "Tour created", "data": { }, "timestamp": "..." }
{ "success": true, "data": { "content": [], "page": 0, "size": 10,
                             "totalElements": 0, "totalPages": 0, "last": true } }
```

## Endpoints

| Method | Path | Access |
|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login`, `/api/auth/refresh`, `/api/auth/logout` | public |
| POST | `/api/auth/logout-all` · GET `/api/auth/me` | authenticated |
| GET | `/api/tours` (search, categoryId, destinationId, minPrice, maxPrice, minDuration, maxDuration, featured, page, size, sort) | public |
| GET | `/api/tours/{id}`, `/api/tours/slug/{slug}` | public |
| POST/PUT | `/api/tours` , `/api/tours/{id}` | ADMIN, MANAGER |
| DELETE | `/api/tours/{id}` | ADMIN |
| GET | `/api/categories`, `/api/destinations` (+ `/{id}`, `/slug/{slug}`) | public |
| POST/PUT | `/api/categories`, `/api/destinations` | ADMIN, MANAGER |
| DELETE | `/api/categories/{id}`, `/api/destinations/{id}` | ADMIN |
| GET | `/api/pages`, `/api/pages/{slug}` | public (published only) |
| GET | `/api/pages/admin/all`, `/api/pages/admin/{id}` | ADMIN, MANAGER |
| POST/PUT | `/api/pages`, `/api/pages/{id}` | ADMIN, MANAGER |
| DELETE | `/api/pages/{id}` | ADMIN |
| POST | `/api/media` (multipart `file`, `ownerType`, `ownerId`, `altText`) | ADMIN, MANAGER |
| GET | `/api/media` (library) · PUT/DELETE `/api/media/{id}` | ADMIN, MANAGER |
| GET | `/api/media/files/{fileName}`, `/api/media/owner/{ownerType}/{ownerId}` | public |

Roles: `USER` (default on registration), `MANAGER`, `ADMIN`.

## Notes

- Slugs are generated from the title/name when the request omits `slug`, and are unique per table.
- Media is attached through `ownerType` + `ownerId` rather than a foreign key, so the media
  module stays independent of tour/destination/page. Deleting a tour detaches its files instead
  of removing them.
- Refresh tokens are stored and rotated: refreshing revokes the presented token and issues a new pair.
