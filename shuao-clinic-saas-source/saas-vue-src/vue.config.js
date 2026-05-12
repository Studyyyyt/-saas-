const { defineConfig } = require('@vue/cli-service')
const API_TARGET = process.env.VUE_APP_API_TARGET || 'http://127.0.0.1:8080'
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
    historyApiFallback: true,
    proxy: {
      '/loginController': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/consultations': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/accounts': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/appointments': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/patients': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/treatments': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/finances': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/doctors': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/Inventory': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/treatment-catalog': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/insurance': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/business-analysis': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/admin-report-portal': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/medical-records': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/lab-factories': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/lab-orders': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/lab-bills': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/lab-statistics': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/material-categories': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/materials': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/material-purchases': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/material-statistics': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/api': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/ai-agent-configs': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/treatment-projects': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/treatment-project-categories': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/medical-record-phrases': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/treatment-operations': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/medical-record-templates': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/payment-channels': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/patient360': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/patient-images': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/followup': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/patient-consent': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/risk-tags': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/consent-template': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/advertising-spending': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/purchase': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/patient-portal': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/staff-portal': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/patient-insights': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/file-transfer': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/medical-record-operations': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/doctor-home-reminders': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/patient-groups': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/role-menu-permissions': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/treatment_plans': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/wechat': {
        target: API_TARGET,
        changeOrigin: true
      }
    }
  }
})
