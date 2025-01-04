import { message } from 'ant-design-vue'
import router from './router'
import { useLoginUserStore } from '@/stores/useLoginUserStore'

let firstFetchLoginUser = true

router.beforeEach(async (to, from, next) => {
  const loginUserStore = useLoginUserStore()
  let loginUser = loginUserStore.loginUser
  //确认页面刷新时，首次加载时，能等待后端返回用户信息后再校验权限
  if (firstFetchLoginUser) {
    await loginUserStore.fetchLoginUser()
    loginUser = loginUserStore.loginUser
    firstFetchLoginUser = false
  }
  const toUrl = to.fullPath
  //可以自定义权限校验逻辑，比如只有管理员才能访问/admin开头的页面
  if (toUrl.startsWith('/admin')) {
    if (!loginUser || loginUser.userRole !== 'admin') {
      message.error('没有权限访问')
      next(`/user/login?redirect=${toUrl}`)
      return
    }
  }
  next()
})
