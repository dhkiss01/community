FROM node:24-trixie-slim@sha256:54f84b4cf8432b21aaffd5d3ce8d41e12cea0d10d33c09d2f3c9879d49d6b446 AS dependencies
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --omit=dev --ignore-scripts --no-audit --no-fund

FROM gcr.io/distroless/nodejs24-debian13:nonroot@sha256:b8cec4b2f2126ff7bd034ec9ac207ffb3272667dfdc48376e3b035d3270549ba AS runtime
ENV NODE_ENV=production \
    PORT=3000
WORKDIR /app
# 실행 사용자가 앱 파일을 수정할 수 없도록 root 소유로 복사합니다.
COPY --from=dependencies /app/node_modules ./node_modules
COPY package.json ./
COPY app.js ./
COPY html ./html
COPY css ./css
COPY js ./js
COPY public ./public
EXPOSE 3000
USER 65532:65532
# distroless 이미지의 기본 진입점이 Node.js이므로 실행할 파일만 지정합니다.
CMD ["app.js"]
