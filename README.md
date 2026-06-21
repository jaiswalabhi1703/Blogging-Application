# Blogging Application — Full Stack

A production-grade blog: a **Spring Boot 3.3 / Java 17** REST API plus a **React + Vite + Tailwind** SPA frontend ("Blogify", an *Aurora Glass* theme with dark/light mode). The backend is secured with **JWT access + refresh tokens** and **role-based access control**, documented with **OpenAPI/Swagger**, containerised with **Docker**, tested in **CI**, and enhanced with an **OpenAI-powered AI authoring assistant** that generates post summaries and tags.

| Part | Path | Stack | Hosts on |
|------|------|-------|----------|
| Backend API | [`/`](.) (root) | Spring Boot, JPA, Spring Security | Container host (Render/Railway/Fly) behind Cloudflare |
| Frontend SPA | [`frontend/`](frontend/) | React, Vite, Tailwind, Framer Motion | **Cloudflare Pages** |

> **The frontend is the part that lives on Cloudflare.** A JVM app can't run on Cloudflare Workers/Pages, but the static SPA does — see [frontend/README.md](frontend/README.md) and [DEPLOYMENT.md](DEPLOYMENT.md).

> Originally a tutorial-style CRUD project, this repo has been hardened and extended into something deployable: externalised secrets, refresh-token rotation, layered profiles (H2 for dev/test, PostgreSQL for prod), an AI feature, automated tests, and a container/CI/deploy story.

---

## ✨ Features

- **Authentication & authorization** — register/login, BCrypt password hashing, stateless JWT **access tokens** (15 min) + DB-backed **refresh tokens** (7 days) with **rotation & revocation**, and `@PreAuthorize` role checks (`ROLE_ADMIN` / `ROLE_USER`).
- **Blog domain** — users, posts, categories, comments; pagination, sorting, keyword search, and image upload/download.
- **AI authoring assistant** 🤖 — `POST /api/posts/{id}/ai/summarize` calls **OpenAI** (`gpt-4o-mini`) to generate a concise summary and topic tags, then persists them. Degrades gracefully when no API key is set.
- **API docs** — interactive Swagger UI with a JWT "Authorize" button.
- **Operability** — Spring Boot Actuator health endpoint, structured logging, container health checks.
- **Quality** — unit tests (Mockito), an end-to-end auth integration test on H2, and JaCoCo coverage.
- **Portability** — runs with zero setup on in-memory H2 (`dev`), PostgreSQL in `prod`, Dockerfile + docker-compose, and a GitHub Actions pipeline.

---

## 🧱 Tech stack

| Area | Choice |
|------|--------|
| Language / runtime | Java 17, Spring Boot 3.3.5 |
| Security | Spring Security 6, jjwt 0.12.x |
| Persistence | Spring Data JPA / Hibernate; H2 (dev/test), PostgreSQL (prod) |
| Mapping / validation | ModelMapper, Jakarta Bean Validation |
| Docs | springdoc-openapi (Swagger UI) |
| AI | OpenAI Chat Completions API (plain HTTP via Spring `RestClient`) |
| Build / test | Maven, JUnit 5, Mockito, JaCoCo |
| Ops | Docker, docker-compose, GitHub Actions |

---

## 🚀 Quick start (local, zero setup)

The default `dev` profile uses an in-memory H2 database — no DB install required.

```bash
./mvnw spring-boot:run
```

Then open:

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- H2 console: <http://localhost:8080/h2-console> (JDBC URL `jdbc:h2:mem:blogdb`, user `sa`)
- Health: <http://localhost:8080/actuator/health>

A default admin is seeded on first start:

```
email:    admin@blog.com
password: Admin@123     # change immediately in any real environment
```

### Run the full stack with Docker

```bash
cp .env.example .env        # then edit values
docker compose up --build
```

This starts PostgreSQL and the API (in the `prod` profile) together.

### Run the frontend

```bash
cd frontend
npm install
npm run dev            # http://localhost:5173  (talks to the API on :8080)
```

Full frontend docs: [frontend/README.md](frontend/README.md).

---

## 🔐 Auth flow

```bash
# 1) Register
curl -X POST localhost:8080/api/auth/register -H 'Content-Type: application/json' \
  -d '{"name":"Ada","email":"ada@blog.com","password":"Password@1","about":"hello"}'

# 2) Login -> { accessToken, refreshToken, ... }
curl -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"ada@blog.com","password":"Password@1"}'

# 3) Call a protected endpoint
curl localhost:8080/api/users/ -H "Authorization: Bearer <accessToken>"

# 4) Rotate the access token when it expires
curl -X POST localhost:8080/api/auth/refresh -H 'Content-Type: application/json' \
  -d '{"refreshToken":"<refreshToken>"}'
```

---

## 🤖 AI assistant

```bash
# Is AI enabled on this deployment?
curl localhost:8080/api/ai/status            # {"enabled": true|false}

# Generate + persist a summary and tags for a post (auth required)
curl -X POST localhost:8080/api/posts/1/ai/summarize \
  -H "Authorization: Bearer <accessToken>"
```

Enable it by exporting your key before starting the app:

```bash
export OPENAI_API_KEY=sk-...
```

Without a key the rest of the API runs normally and the AI endpoint returns a clear "not configured" message.

---

## 📚 Key endpoints

| Method | Path | Access |
|--------|------|--------|
| POST | `/api/auth/register` | public |
| POST | `/api/auth/login` | public |
| POST | `/api/auth/refresh` | public (valid refresh token) |
| POST | `/api/auth/logout` | public (valid refresh token) |
| GET | `/api/posts`, `/api/posts/{id}` | public |
| POST | `/api/user/{userId}/category/{categoryId}/posts` | authenticated |
| POST | `/api/posts/{id}/ai/summarize` | authenticated |
| DELETE | `/api/users/{id}` | `ROLE_ADMIN` |
| GET | `/api/categories`, `/api/categories/{id}` | public |
| GET | `/actuator/health` | public |

Full, always-current contract: **Swagger UI**.

---

## ⚙️ Configuration

All configuration is environment-driven; **no secrets are committed**. See [.env.example](.env.example).

| Variable | Purpose | Default (dev) |
|----------|---------|---------------|
| `SPRING_PROFILES_ACTIVE` | `dev` \| `prod` | `dev` |
| `DATABASE_URL` / `DATABASE_USERNAME` / `DATABASE_PASSWORD` | PostgreSQL (prod) | — |
| `JWT_SECRET` | Base64 HMAC secret (≥256 bits) | bundled dev key |
| `JWT_ACCESS_EXPIRATION_MS` / `JWT_REFRESH_EXPIRATION_MS` | token lifetimes | 15 min / 7 days |
| `CORS_ALLOWED_ORIGINS` | allowed browser origins | localhost |
| `OPENAI_API_KEY` | enables AI features | empty (disabled) |
| `AI_MODEL` | OpenAI model id | `gpt-4o-mini` |

---

## 🧪 Tests

```bash
./mvnw verify        # runs all tests + writes coverage to target/site/jacoco/index.html
```

- `JwtTokenHelperTest` — token generation/validation (pure unit).
- `CategoryServiceImplTest` — service logic with Mockito.
- `AuthFlowIntegrationTest` — register → login → protected access → refresh, on H2.

---

## 🚢 Deployment

A JVM app cannot run directly on Cloudflare Workers. The supported path is to deploy the container to a Java-friendly host and put **Cloudflare** in front for DNS, CDN, TLS and WAF. See **[DEPLOYMENT.md](DEPLOYMENT.md)** for a step-by-step Render/Railway + Cloudflare guide.

---

## 🗂️ Project structure

```
src/main/java/com/blog
├── config         # security, OpenAPI, typed properties, constants
├── controllers    # REST controllers (auth, users, posts, categories, comments, ai)
├── entities       # JPA entities (incl. RefreshToken)
├── exceptions     # custom exceptions + global handler
├── payloads       # request/response DTOs
├── repositories   # Spring Data repositories
├── security       # JWT helper, filter, entry point, user details
└── service        # service interfaces + impl (incl. AiContentService)
```

---

## 📈 Notable hardening done to the original project

- Removed **leaked production database credentials** and a startup `System.out` that printed a BCrypt hash.
- Replaced the hardcoded weak JWT secret and ancient `jjwt 0.9.1` with externalised secrets and modern `jjwt 0.12.x`.
- Fixed a bug where `updateUser` stored the **raw (unencoded) password**, breaking login.
- Stopped serializing the password field in API responses.
- Modernised Spring Security to the lambda DSL, added CORS, and corrected role-based authorization.
- Added refresh tokens, AI features, tests, Docker, CI and docs.
