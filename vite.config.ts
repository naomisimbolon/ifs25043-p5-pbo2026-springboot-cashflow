import { UserConfigFn } from 'vite';
import { overrideVaadinConfig } from './vite.generated';

const customConfig: UserConfigFn = () => ({
  build: {
    // audit Lighthouse "Missing source maps for large first-party JavaScript"
    sourcemap: true,
  },
});

export default overrideVaadinConfig(customConfig);
