import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
        '@': resolve(__dirname, 'src'), // 配置@指向src目录
        '&': resolve(__dirname, 'public'), // 配置@指向src目录
    }
  },
    server: {
        host: "0.0.0.0",
        open: true,
        proxy: {
            // 代理 /dev-api 的请求
            '/dev-api': {
                changeOrigin: true,
                // 代理目标地址：https://api.youlai.tech
                target: 'http://localhost:8000',
                rewrite: (path) => path.replace(new RegExp("^" + '/dev-api'), ""),
            },
        },
    },
})
