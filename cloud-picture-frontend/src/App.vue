<template>
  <div id="app">
    <a-config-provider :locale="locale">
      <BasicLayout />
    </a-config-provider>
  </div>
</template>

<script setup lang="ts">
import BasicLayout from '@/layout/BasicLayout.vue'

import zhCN from 'ant-design-vue/es/locale/zh_CN'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
dayjs.locale('zh-cn')

const locale = zhCN

//已经改为在权限校验中获取
// const userLoginStore = useLoginUserStore()
// userLoginStore.fetchLoginUser()

// healthUsingGet().then((res) => {
//   console.log(res)
// })
</script>

<style scoped></style>
