import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: { port: 3000 },
  test: {
    environment: 'jsdom',
    setupFiles: './src/test-setup.ts',
    globals: true,
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html', 'lcov'],
      exclude: ['**/*.test.tsx', '**/*.types.ts', 'src/main.tsx', 'src/theme.ts'],
      thresholds: { lines: 10, branches: 40, functions: 15, statements: 10 },
    },
  },
});
