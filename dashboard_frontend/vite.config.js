import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// 개발 서버(npm run dev)에서 /api, /ws 요청을 백엔드로 넘긴다.
// 배포 환경에서는 nginx가 같은 일을 한다 (nginx.conf).
const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  define: {
    global: 'globalThis',
  },
  server: {
    proxy: {
      '/api': { target: BACKEND_URL, changeOrigin: true },
      '/ws': { target: BACKEND_URL, changeOrigin: true, ws: true },
    },
  },
})
