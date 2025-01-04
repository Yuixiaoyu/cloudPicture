<template>
  <div id="userRegisterPage">
    <h2 class="title">云图床--用户注册</h2>
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
      <a-form-item
        name="checkPassword"
        :rules="[
          { required: true, message: '请输入确认密码！' },
          { min: 8, message: '确认密码长度不能小于8位！' },
        ]"
      >
        <a-input-password v-model:value="formState.checkPassword" placeholder="请输入确认密码">
          <template #prefix><LockOutlined style="color: rgba(0, 0, 0, 0.25)" /></template>
        </a-input-password>
      </a-form-item>
      <div class="tips">
        已有账号?
        <router-link to="/user/login">立即登录</router-link>
      </div>
      <a-form-item>
        <a-button type="primary" style="width: 100%" html-type="submit">注册</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { userLoginUsingPost, userRegisterUsingPost } from '@/api/userController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { message } from 'ant-design-vue'
import router from '@/router'
import path from 'path'

interface FormState {
  username: string
  password: string
}
//用于接收表单数据
const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})
const handleSubmit = async (values: any) => {
  //校验密码和确认密码是否一致
  if (values.userPassword !== values.checkPassword) {
    message.error('密码和确认密码不一致')
    return
  }
  //调用接口
  const res = await userRegisterUsingPost(values)
  //注册成功
  if (res.data.code === 200 && res.data.data) {
    //跳转到登录页
    message.success('注册成功')
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error('注册失败:' + res.data.message)
  }
}
</script>

<style scoped>
#userRegisterPage {
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
