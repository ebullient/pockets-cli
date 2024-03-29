import { defineConfig } from 'vite'
import { svelte } from '@sveltejs/vite-plugin-svelte'

// https://vitejs.dev/config/
export default defineConfig({
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: '@use "src/variables.scss" as *;',
      },
    },
  },
  plugins: [svelte()],
  server: {
    port: 5173,
    host: '127.0.0.1',
    hmr: {
      // for proxying from quarkus dev
      port: 5173,
      // for testing standalone
      // protocol: 'ws',
      host: '127.0.0.1',
    }
  }
})
