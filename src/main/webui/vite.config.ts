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
    hmr: {
        protocol: 'ws',
        host: 'localhost',
    }
  }
})
