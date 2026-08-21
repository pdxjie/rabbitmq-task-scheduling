/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        // 深色主题
        'bg-primary': '#0a0a0a',
        'bg-secondary': '#161616',
        'bg-tertiary': '#262626',
        'text-primary': '#ffffff',
        'text-secondary': '#a1a1a1',
        'text-tertiary': '#6b6b6b',
        'border-primary': '#2a2a2a',
        'border-secondary': '#404040',
        // 品牌色
        'brand-primary': '#3b82f6',
        'brand-hover': '#2563eb',
        'brand-light': '#60a5fa',
      },
    },
  },
  plugins: [],
}
