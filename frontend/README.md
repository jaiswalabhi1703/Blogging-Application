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

## Deploy to Cloudflare

> ⚠️ **You must serve the Vite *build* (`dist/`), not the source folder.** If Cloudflare
> serves the source, the page is blank because `index.html` points at `/src/main.jsx`
> (dev-only). `wrangler.jsonc` already points the deployment at `./dist`.

### Option A — Cloudflare Workers (Git-connected, `*.workers.dev`)

Cloudflare dashboard → **Workers & Pages → your `blogify` Worker → Settings → Build**:
- **Root directory:** `frontend`
- **Build command:** `npm run build`
- **Deploy command:** `npx wrangler deploy`  (uses `wrangler.jsonc`)

`wrangler.jsonc` serves `./dist` and handles SPA routing (`not_found_handling: single-page-application`). Trigger a new deployment after setting this.

### Option B — CLI

```bash
cd frontend
npm install
npx wrangler login      # once
npm run deploy          # builds, then wrangler deploy
```

### Option C — Cloudflare Pages (`*.pages.dev`)

Create → Pages → Connect to Git → Root `frontend`, Build `npm run build`, Output `dist`. `public/_redirects` handles SPA routing.

### API base URL (important)

`VITE_API_BASE_URL` is baked in **at build time**. The local `.env` is git-ignored, so
the Cloudflare build defaults to `http://localhost:8080`. Once the backend is deployed,
set **`VITE_API_BASE_URL = https://your-api-host`** as a build environment variable in
Cloudflare and redeploy — otherwise the app loads but shows no data.

This is the clean split: **static frontend on Cloudflare's edge, JVM API on a container host behind Cloudflare** (see [../DEPLOYMENT.md](../DEPLOYMENT.md)).
