/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class', // 开启基于 class 的暗黑模式
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}", // 确保扫描所有组件
  ],
  theme: {
    extend: {
      colors: {
        apple: {
          blue: '#007AFF',         // iCloud 系统蓝核心色
          gray: '#F5F5F7',         // 浅色模式主背景灰（极简留白）
          darker: '#000000',       // 暗黑模式纯黑主背景
          dark: '#1C1C1E',         // 暗黑模式卡片/毛玻璃底色
          border: '#E5E5E9',       // 极简浅色边框
          'border-dark': '#38383A',// 暗黑模式边框
          text: '#1D1D1F',         // 浅色模式主要文字
          'text-muted': '#86868B', // 次要文字（深浅通用）
        }
      }
    },
  },
  plugins: [],
}

