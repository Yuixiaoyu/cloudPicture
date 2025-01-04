<template>
  <div id="userLoginPage">
    <h2 class="title">云图床--用户登录</h2>
    <div class="desc">企业级智能Ai图库</div>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
      <a-form-item
        name="userAccount"
        :rules="[
          { required: true, message: '请输入账号！' },
          { min: 4, message: '账号长度不能小于4位！' },
        ]"
      >
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号">
          <template #prefix><UserOutlined style="color: rgba(0, 0, 0, 0.25)" /></template>
        </a-input>
      </a-form-item>

      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码！' },
          { min: 8, message: '密码长度不能小于8位！' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码">
          <template #prefix><LockOutlined style="color: rgba(0, 0, 0, 0.25)" /></template>
        </a-input-password>
      </a-form-item>
      <div class="tips">
        没有账号?
        <router-link to="/user/register">立即注册</router-link>
      </div>
      <a-form-item>
        <a-button type="primary" style="width: 100%" html-type="submit">登录</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { userLoginUsingPost } from '@/api/userController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { message } from 'ant-design-vue'
import router from '@/router'
import path from 'path'

interface FormState {
  username: string
  password: string
}
const loginUserStore = useLoginUserStore()
//用于接收表单数据
const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})
const handleSubmit = async (values: any) => {
  const res = await userLoginUsingPost(values)
  //登录成功
  if (res.data.code === 200 && res.data.data) {
    //把登录态保存到全局状态中
    await loginUserStore.fetchLoginUser()
    message.success('登录成功')
    //判断url中是否存在redirect参数，如果存在则跳转到redirect参数指定的路径，否则跳转到首页
    const redirect = router.currentRoute.value.query.redirect
    if (redirect) {
      router.push({
        path: redirect as string,
        replace: true,
      })
    } else {
      router.push({
        path: '/',
        replace: true,
      })
    }
  } else {
    message.error('登陆失败:' + res.data.message)
  }
}
</script>

<style scoped>
#userLoginPage {
  max-width: 360px;
  margin: 0 auto;
}
.title {
  font-size: 24px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 20px;
}
.desc {
  font-size: 14px;
  text-align: center;
  color: #6f6d6d;
  margin-bottom: 32px;
}
.tips {
  text-align: right;
  font-size: 14px;
  color: #bbb;
  margin-bottom: 16px;
}
.tips a {
  color: #409bef;
  text-decoration: none;
}
</style>
