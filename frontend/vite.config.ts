import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";
import type { PreviewOptions } from "vite";

export default defineConfig({
  plugins: [react()],
  preview: {
    allowedHosts: true,
  } as unknown as PreviewOptions,
});
