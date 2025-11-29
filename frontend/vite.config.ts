import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  define: {
    global: 'globalThis',
  },
  server: {
    host: '0.0.0.0', // Bind to all network interfaces for network access
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      '/h2-console': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
  // Build configuration for production
  build: {
    outDir: 'dist',
    sourcemap: false,
  },
  preview: {
    host: '0.0.0.0', // Bind preview server to all interfaces too
    port: 5173,
  }
})
