# syntax=docker/dockerfile:1.7
# Astro build with PUBLIC_API_URL baked at build time (browser-facing gateway URL).
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN --mount=type=cache,target=/root/.npm npm ci --no-audit --no-fund
COPY . .
ARG PUBLIC_API_URL=http://localhost:8080
ENV PUBLIC_API_URL=${PUBLIC_API_URL}
RUN npm run build

FROM node:22-alpine
WORKDIR /app
ENV HOST=0.0.0.0 \
    PORT=4321 \
    NODE_ENV=production
COPY --from=build --chown=node:node /app /app
USER node
EXPOSE 4321
HEALTHCHECK --interval=3s --timeout=2s --start-period=20s --retries=30 \
  CMD wget -qO /dev/null "http://127.0.0.1:${PORT}/login" || exit 1
# SSR with @astrojs/node standalone (= npm start). HOST/PORT from env.
CMD ["node", "dist/server/entry.mjs"]
