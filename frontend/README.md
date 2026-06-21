# Blogify — Aurora Glass frontend

A React SPA for the [Blogging Application API](../README.md). Dark-first **Aurora Glass** theme: frosted-glass surfaces, animated gradient aurora, neon accents, and smooth motion — with a full light mode.

## Stack

React 18 · Vite · Tailwind CSS · React Router · Axios · Framer Motion · lucide-react

## Features

- 🔐 JWT auth with **silent refresh-token rotation** (Axios interceptor refreshes on 401 and retries)
- 🌗 **Dark / light** mode (persisted, respects system preference; dark by default)
- 📝 Create / edit / delete posts, image upload, inline category creation
- 🤖 One-click **AI summary + tags** (calls the backend's OpenAI-powered endpoint)
- 🔎 Search, category browsing, pagination, profile with your posts
- 📱 Responsive glass navbar with mobile menu

## Run locally

The backend must be running on `http://localhost:8080` (see the root README — `./mvnw spring-boot:run`).

```bash
cd frontend
cp .env.example .env        # adjust VITE_API_BASE_URL if needed
npm install
npm run dev                 # http://localhost:5173
```

Vite's dev origin (`http://localhost:5173`) is already in the backend's allowed CORS origins.

## Build

```bash
npm run build               # outputs to dist/
npm run preview             # preview the production build
```

## Deploy to Cloudflare Pages

1. Push the repo to GitHub.
2. Cloudflare dashboard → **Workers & Pages → Create → Pages → Connect to Git**.
3. Build settings:
   - **Root directory:** `frontend`
   - **Build command:** `npm run build`
   - **Build output directory:** `dist`
4. Environment variable: `VITE_API_BASE_URL = https://api.yourdomain.com` (your deployed backend, behind Cloudflare).
5. Deploy. `public/_redirects` already handles SPA routing.

This is the clean split: **static frontend on Cloudflare's edge, JVM API on a container host behind Cloudflare** (see [../DEPLOYMENT.md](../DEPLOYMENT.md)).
