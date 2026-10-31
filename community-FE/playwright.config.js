import { defineConfig, devices } from "@playwright/test";
export default defineConfig({
  testDir: "./e2e",
  fullyParallel: false,
  workers: 1,
  use: { baseURL: "http://127.0.0.1:3000", trace: "retain-on-failure" },
  projects: [
    { name: "desktop", use: { ...devices["Desktop Chrome"] } },
    {
      name: "mobile",
      use: { ...devices["iPhone 13"], defaultBrowserType: "chromium" },
    },
  ],
  webServer: {
    command: "node app.js",
    url: "http://127.0.0.1:3000",
    reuseExistingServer: !process.env.CI,
    env: { API_UPSTREAM: process.env.API_UPSTREAM || "http://127.0.0.1:18080" },
  },
});
