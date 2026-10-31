import express from "express";
import dotenv from "dotenv";
import http from "node:http";
import https from "node:https";
import { fileURLToPath } from "node:url";
import path from "node:path";

dotenv.config();
const root = path.dirname(fileURLToPath(import.meta.url));
export function createApp(
  upstream = process.env.API_UPSTREAM || "http://localhost:8080",
) {
  const app = express();
  app.disable("x-powered-by");
  app.use((req, res, next) => {
    res.set("X-Content-Type-Options", "nosniff");
    res.set("Referrer-Policy", "strict-origin-when-cross-origin");
    next();
  });
  // Keep auth cookies and API requests on the same origin in local development.
  app.use("/api", (req, res) => {
    const target = new URL(upstream + req.url);
    const transport = target.protocol === "https:" ? https : http;
    const proxy = transport.request(
      target,
      {
        method: req.method,
        headers: { ...req.headers, host: target.host },
        timeout: 10000,
      },
      (response) => {
        res.writeHead(response.statusCode, response.headers);
        response.pipe(res);
      },
    );
    proxy.on("timeout", () => proxy.destroy());
    proxy.on("error", () => {
      if (!res.headersSent)
        res
          .status(502)
          .json({
            code: "API_UNAVAILABLE",
            message: "서버에 연결하지 못했습니다. 잠시 후 다시 시도해주세요.",
          });
      else res.destroy();
    });
    req.on("aborted", () => proxy.destroy());
    req.pipe(proxy);
  });
  app.get("/config.js", (req, res) => {
    res.set("Cache-Control", "no-store");
    res
      .type("js")
      .send(
        `window.__APP_CONFIG__ = ${JSON.stringify({ API_BASE_URL: process.env.API_BASE_URL || "/api" })};`,
      );
  });
  for (const directory of ["html", "css", "js", "public"]) {
    app.use(`/${directory}`, express.static(path.join(root, directory)));
  }
  app.get("/", (req, res) => res.redirect("/html/index.html"));
  return app;
}
if (
  process.argv[1] &&
  path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)
) {
  const port = Number(process.env.PORT || 3000);
  createApp().listen(port, () =>
    console.log(`더그아웃: http://localhost:${port}`),
  );
}
