const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  pages: {
    index: {
      entry: 'src/main.js',
      title: '舒澳口腔管理系统'
    }
  },
  devServer: {
    host: '0.0.0.0',
    port: 7070,
    proxy: {
      '/loginController': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/accounts': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/appointments': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/patients': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/treatments': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/finances': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/doctors': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/Inventory': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/treatment-catalog': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/insurance': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/business-analysis': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/admin-report-portal': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/medical-records': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/lab-factories': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/lab-orders': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/lab-bills': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/lab-statistics': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/material-categories': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/materials': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/material-purchases': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/material-statistics': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      }
    }
  }
})
