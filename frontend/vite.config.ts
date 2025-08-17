import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  css: {
    postcss: "./postcss.config.js",
  },
  server: {
    proxy: {
      "/api": {
        target: process.env.VITE_PROXY_TARGET || "http://backend:8080",
        changeOrigin: true,
        secure: false,
      },
    },
  },
});
