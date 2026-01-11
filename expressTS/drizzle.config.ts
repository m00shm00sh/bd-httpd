import { defineConfig } from "drizzle-kit"

import { env, loadEnvFile } from "node:process"
loadEnvFile()

export default defineConfig({
  schema: "src/db/schema.ts",
  out: "src/db/_gen",
  dialect: "postgresql",
  dbCredentials: {
    url: env['DB_URL']
  },
});

