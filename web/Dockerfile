# ==============================================================
# Dockerfile for the Calculator WEB (frontend) service (Lab 7)
# --------------------------------------------------------------
# Packages the static web page (HTML + CSS + JS) into the official
# nginx:1.27-alpine image. nginx serves the page on port 80 and
# forwards /api/* requests to the backend container, addressed by
# its Compose service name "backend" (see web/nginx.conf).
# Build:  docker build -t calculator-web:v1 .
# Run:    docker run --rm -p 8080:80 calculator-web:v1
# ==============================================================

# FROM selects the base image. nginx:1.27-alpine is the small Alpine
# variant of the official nginx web server - ideal for serving a
# handful of static files and proxying a few API routes.
FROM nginx:1.27-alpine

# Metadata label: who maintains this image and what it is for.
LABEL maintainer="Yang Ziyi 202583930026"
LABEL description="Calculator web frontend - static page + /api reverse proxy"

# COPY puts the static frontend files into nginx's default document
# root, which the stock configuration serves on port 80.
COPY index.html /usr/share/nginx/html/index.html
COPY style.css  /usr/share/nginx/html/style.css
COPY app.js     /usr/share/nginx/html/app.js

# COPY replaces nginx's default site configuration with our own.
# The custom config adds the /api/ location block that proxies to
# http://backend:8000 - the Compose service name, NOT localhost.
COPY nginx.conf /etc/nginx/conf.d/default.conf

# EXPOSE documents that the container listens on port 80. The host
# mapping 8080:80 is done by docker-compose ("ports: 8080:80").
EXPOSE 80

# No ENTRYPOINT/CMD needed: the nginx base image already starts the
# nginx daemon in the foreground by default.
