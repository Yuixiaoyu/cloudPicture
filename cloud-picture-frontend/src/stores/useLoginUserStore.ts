import { getLoginUserUsingGet } from '@/api/userController.ts'
import { defineStore } from 'pinia'
import { ref } from 'vue'

//存储登录用户信息
export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<API.LoginUserVo>({
    userName: '未登录',
  })

  // 获取登录用户信息
  async function fetchLoginUser() {
    // todo 由于后端还没提供接口，暂时注释
    const res = await getLoginUserUsingGet()
    console.log(res)
    if (res.data.code === 200 && res.data.data) {
      loginUser.value = res.data.data
    }
    //测试用户登录，三秒后自动登录
    // setTimeout(() => {
    //   loginUser.value = {
    //     id: '123456',
    //     userName: '测试用户',
    //   }
    // }, 3000)
  }

  // 设置登录用户信息
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  return { loginUser, setLoginUser, fetchLoginUser }
})
