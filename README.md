# Calculator - Multi-Container Application with Docker Compose

![Calculator CI](https://github.com/yangziyi0528/calculator/actions/workflows/ci-calculator.yml/badge.svg)

The Java calculator from Labs 3-6, since **Lab 7** split into a
**multi-container application** orchestrated with Docker Compose:

```
browser ──> web (nginx :80, host 8080) ──/api/*──> backend (Java API :8000)
              static page                            Calculator.eval()
```

* `backend/` - Maven project (business logic from Lab 3) wrapped in a small
  HTTP API (`ApiServer.java`, JDK built-in `HttpServer`, no new dependencies).
  Its Dockerfile builds a multi-stage image that listens on port 8000.
* `web/` - static HTML/CSS/JS page packaged into `nginx:1.27-alpine`. nginx
  serves the page on port 80 and proxies `/api/*` to `http://backend:8000`
  **by Compose service name** - never localhost.
* `docker-compose.yml` - defines both services, builds them from their local
  Dockerfiles, maps `8000:8000` and `8080:80`, sets `depends_on` and
  `restart: on-failure`, and injects `LOG_LEVEL` into the backend.

## Requirements

* Docker Desktop (with Compose v2) - nothing else, the builds are self-contained

## Run the whole stack

```bash
docker compose up --build -d     # build both images and start both containers
docker compose ps                # check both services are running
docker compose logs -f backend   # watch API request logs
docker compose down              # stop and remove containers + network
```

Then open <http://127.0.0.1:8080> and calculate. The API is also reachable
directly on the host:

```bash
curl "http://127.0.0.1:8000/api/eval?a=2&op=%2B&b=3"     # {"a":"2","op":"+","b":"3","result":5.0}
```

## Backend API

| Endpoint        | Meaning                                        |
|-----------------|------------------------------------------------|
| `GET /api/health` | liveness probe, answers `{"status":"ok"}`    |
| `GET /api/eval?a=..&op=..&b=..` | evaluate `a op b`, answers JSON |

Supported operators: `+ - * / ^ %`. Errors (blank input, unknown operator,
division by zero) answer HTTP 400 with `{"error": "..."}`. The `LOG_LEVEL`
environment variable (see compose file) turns request logging `INFO`/`OFF`.

## Continuous Integration

Every push to `main` and every PR targeting `main` triggers the workflow
`.github/workflows/ci-calculator.yml`:

1. **Build & Test** - `mvn compile` + JUnit suite on JDK 17 and 21 (Lab 5)
2. **Backend Image Build & API Smoke Test** - builds `backend/Dockerfile` and
   calls `/api/eval` for `+ - * /` inside a container (Lab 6/7)
3. **Compose Build Validation** (Lab 7) - `docker compose config` plus
   `docker compose build` so a broken compose file or Dockerfile can never
   reach `main`
