# Multi-stage build for CRA React app
FROM node:18-alpine AS build
WORKDIR /app

# Install dependencies robustly: prefer npm ci; fallback to npm install if lockfile is out of sync
COPY package*.json ./
RUN npm config set fund false \
 && npm config set audit false \
 && (npm ci --no-audit --fund=false || npm install --no-audit --fund=false --legacy-peer-deps)

# Copy the rest of the sources and build
COPY . .
# Optional: allow passing build-time envs like REACT_APP_API_BASE
ARG REACT_APP_API_BASE
ENV REACT_APP_API_BASE=${REACT_APP_API_BASE}
# Keep CRA from failing build on warnings inside containers
ENV CI=false
RUN npm run build

# --- Runtime image ---
FROM nginx:alpine AS runtime
# Copy build output
COPY --from=build /app/build /usr/share/nginx/html
# Nginx configuration for SPA
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
