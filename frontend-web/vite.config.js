import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
export default defineConfig({
  plugins: [react()],
  define: { global: 'globalThis' },
  server: {
    port: 3000,
    proxy: {
      '/api':     { target: 'http://localhost:8080', changeOrigin: true },
      '/uploads': { target: 'http://localhost:8080', changeOrigin: true },
      '/ws':      { target: 'http://localhost:8080', changeOrigin: true, ws: true },
      '/vision':  { target: 'http://localhost:8000', changeOrigin: true, rewrite: p => p.replace(/^\/vision/, '') },
    },
  }
})
