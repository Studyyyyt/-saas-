/**
 * API 错误处理工具函数
 * 根据错误类型返回友好的错误提示，并给出解决方案
 * @param {VueComponent} vm - Vue 组件实例（用于调用 $message）
 * @param {string} actionName - 操作名称，如"加载咨询记录"、"获取账号权限"
 * @param {Error} error - axios  catch 到的错误对象
 */
export function showApiError(vm, actionName, error) {
  if (!vm || !vm.$message) {
    console.error('[showApiError] vm 或 $message 不存在')
    return
  }

  let message = ''

  // 无响应对象 = 网络/连接问题（请求未到达服务器）
  if (!error || !error.response) {
    message = `请求失败，请检查网络连接后刷新页面重试。如问题持续，请联系管理员。`
  }
  // 根据 HTTP 状态码给出明确提示
  else if (error.response.status >= 500) {
    message = `服务器暂时无法处理您的请求，请稍后重试。如问题持续，请联系管理员。`
  }
  else if (error.response.status === 404) {
    message = `请求的服务接口未找到，请联系开发人员检查系统配置。`
  }
  else if (error.response.status === 403) {
    message = `权限不足，无法${actionName}。请联系管理员分配权限。`
  }
  else if (error.response.status === 401) {
    message = `登录状态已过期，请重新登录后再试。`
  }
  // 其他情况：优先展示后端返回的业务错误信息
  else {
    const serverMsg = error.response.data && error.response.data.msg
    if (serverMsg) {
      message = `${serverMsg}，请刷新页面重试。如问题持续，请联系管理员。`
    } else {
      message = `${actionName}失败，请刷新页面重试。如问题持续，请联系管理员。`
    }
  }

  vm.$message.error(message)
}
