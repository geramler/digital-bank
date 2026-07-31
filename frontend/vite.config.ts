import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api/customers': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        rewrite: (path: string) => path.replace(/^\/api\/customers/, '/customers')
      },
      '/api/accounts': {
        target: 'http://localhost:8083',
        changeOrigin: true,
        rewrite: (path: string) => path.replace(/^\/api\/accounts/, '/accounts')
      },
      '/api/transactions': {
        target: 'http://localhost:8084',
        changeOrigin: true,
        rewrite: (path: string) => path.replace(/^\/api\/transactions/, '/transactions')
      },
      '/api/auth': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        rewrite: (path: string) => path.replace(/^\/api\/auth/, '/auth')
      }
    }
  },
  build: {
    outDir: 'dist',
    sourcemap: true
  }
})
