import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

const backendTarget = process.env.VITE_API_TARGET || 'http://localhost:50000'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api/chat': {
        target: backendTarget,
        changeOrigin: true
      },
      '/api/report': {
        target: backendTarget,
        changeOrigin: true
      },
      '/api/resume': {       
        target: backendTarget,
        changeOrigin: true
      },
      '/user': {
        target: backendTarget,
        changeOrigin: true
      },
      '/question': {
        target: backendTarget,
        changeOrigin: true
      },
      '/api/knowledge': {
        target: backendTarget,
        changeOrigin: true
      }
    }
  }
})