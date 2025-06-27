import { defineConfig } from 'vite';

export default defineConfig({
  server: {
    host: '0.0.0.0',
    proxy: {
      // /verifier/admin/v1 → http://localhost:8092/verifier/admin/v1
      '/verifier/admin/v1': {
        target: 'http://localhost:8092',
        changeOrigin: true,
      },
    },
  },
});
