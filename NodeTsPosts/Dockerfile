# syntax=docker/dockerfile:1.7
FROM node:22-alpine
WORKDIR /app
COPY package*.json ./
RUN --mount=type=cache,target=/root/.npm npm ci --omit=dev --no-audit --no-fund
COPY . .
ENV PORT=3001 \
    UPLOAD_DIR=/data/upload \
    NODE_ENV=production
RUN mkdir -p /data/upload && chown -R node:node /data /app
USER node
VOLUME ["/data/upload"]
EXPOSE 3001
HEALTHCHECK --interval=3s --timeout=2s --start-period=10s --retries=20 \
  CMD nc -z localhost "${PORT}" || exit 1
CMD ["node", "index.ts"]
