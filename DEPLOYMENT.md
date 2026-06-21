# Deployment Guide

This service is a **Spring Boot (JVM) application**. It cannot run directly on Cloudflare Workers/Pages, which execute JavaScript/WASM at the edge rather than a JVM. The realistic, production-grade pattern is:

```
Browser ──HTTPS──▶ Cloudflare (DNS · CDN · TLS · WAF · caching)
                        │  (proxied)
                        ▼
              Container host (Render / Railway / Fly.io)
                        │
                        ▼
                 PostgreSQL (managed)
```

Cloudflare sits **in front** of a container host that actually runs the JAR. You get Cloudflare's TLS, CDN, DDoS protection and a custom domain, while a Java-friendly platform runs the app.

---

## 1. Prepare

1. Generate a strong JWT secret:
   ```bash
   openssl rand -base64 48
   ```
2. Have your `OPENAI_API_KEY` ready if you want AI features.
3. Commit the repo to GitHub (the Dockerfile is the deploy artifact).

---

## 2. Deploy the container (example: Render)

1. **New → Web Service** → connect the GitHub repo. Render auto-detects the `Dockerfile`.
2. **Add a PostgreSQL** instance (Render → New → PostgreSQL) and copy its **internal connection string**.
3. Set environment variables on the web service:

   | Key | Value |
   |-----|-------|
   | `SPRING_PROFILES_ACTIVE` | `prod` |
   | `DATABASE_URL` | `jdbc:postgresql://<host>:5432/<db>` |
   | `DATABASE_USERNAME` | from Render PG |
   | `DATABASE_PASSWORD` | from Render PG |
   | `JWT_SECRET` | output of `openssl rand -base64 48` |
   | `CORS_ALLOWED_ORIGINS` | your frontend domain(s) |
   | `OPENAI_API_KEY` | optional |

   > Render exposes the PG URL as `postgres://...`. Convert it to the JDBC form `jdbc:postgresql://HOST:PORT/DB` and put the username/password in their own variables.

4. Render builds the image and runs the health check at `/actuator/health`. Note the public URL, e.g. `https://blog-api.onrender.com`.

> **Railway / Fly.io** follow the same shape: provision Postgres, set the same env vars, deploy the Dockerfile.

---

## 3. Put Cloudflare in front

1. Add your domain to Cloudflare (update the registrar's nameservers).
2. Create a DNS record for the API, **proxied** (orange cloud):
   - `CNAME  api  →  blog-api.onrender.com`
3. SSL/TLS mode: **Full (strict)**.
4. (Optional) Hardening & performance:
   - **WAF** managed rules on.
   - **Rate limiting** rule on `/api/auth/*` to blunt credential stuffing.
   - **Cache rule**: cache `GET /api/posts*` and `/api/categories*` responses; bypass cache for anything with an `Authorization` header.
5. Update `CORS_ALLOWED_ORIGINS` on the host to include your real frontend origin (e.g. a Cloudflare Pages site `https://your-app.pages.dev`).

Your API is now served at `https://api.yourdomain.com`, proxied through Cloudflare.

---

## 4. (Optional) No public host — Cloudflare Tunnel

To expose a container running on your own machine/VM without a public IP:

```bash
# run the app (or `docker compose up`) locally on :8080, then:
cloudflared tunnel --url http://localhost:8080
```

For a stable hostname, create a named tunnel and map `api.yourdomain.com` to it in the Cloudflare Zero Trust dashboard.

---

## 5. Frontend on Cloudflare Pages (optional)

A separate SPA (React/Vue/etc.) **can** live on Cloudflare Pages. Point it at `https://api.yourdomain.com` and add its origin to `CORS_ALLOWED_ORIGINS`. This is the clean split: **static frontend on Cloudflare's edge, JVM API on a container host behind Cloudflare**.

---

## Post-deploy checklist

- [ ] `GET /actuator/health` returns `{"status":"UP"}` through the Cloudflare domain.
- [ ] Changed the seeded `admin@blog.com` password (or removed the seed for prod).
- [ ] `JWT_SECRET` is a strong, unique value (not the bundled dev default).
- [ ] `CORS_ALLOWED_ORIGINS` lists only your real frontend origins.
- [ ] Swagger UI reachable at `/swagger-ui.html` (consider restricting it in prod).
