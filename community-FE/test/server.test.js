import { test } from "node:test";
import assert from "node:assert/strict";
import http from "node:http";
import { createApp } from "../app.js";

const listen = (server) =>
  new Promise((resolve) =>
    server.listen(0, "127.0.0.1", () => resolve(server)),
  );
const close = (server) => new Promise((resolve) => server.close(resolve));
test("only public assets are served, and API bodies and auth headers are forwarded", async () => {
  const backend = await listen(
    http.createServer((req, res) => {
      let data = "";
      req.on("data", (chunk) => (data += chunk));
      req.on("end", () => {
        res.setHeader("Content-Type", "application/json");
        res.setHeader("Set-Cookie", "refreshToken=test; HttpOnly; Path=/");
        res.end(
          JSON.stringify({
            path: req.url,
            auth: req.headers.authorization,
            data,
          }),
        );
      });
    }),
  );
  const server = await listen(
    http.createServer(createApp(`http://127.0.0.1:${backend.address().port}`)),
  );
  const base = `http://127.0.0.1:${server.address().port}`;
  try {
    for (const path of [
      "/package.json",
      "/app.js",
      "/.env",
      "/node_modules/express/package.json",
    ])
      assert.equal((await fetch(base + path)).status, 404);
    assert.match(
      await (await fetch(base + "/html/index.html")).text(),
      /lang="ko"/,
    );
    assert.match(await (await fetch(base + "/config.js")).text(), /\/api/);
    const result = await fetch(base + "/api/posts?q=한화", {
      method: "POST",
      headers: { Authorization: "Bearer test" },
      body: "hello",
    });
    assert.match(result.headers.get("set-cookie"), /HttpOnly/);
    assert.deepEqual(await result.json(), {
      path: "/posts?q=%ED%95%9C%ED%99%94",
      auth: "Bearer test",
      data: "hello",
    });
  } finally {
    await close(server);
    await close(backend);
  }
});
