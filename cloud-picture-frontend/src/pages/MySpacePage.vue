<template>
  <div id="mySpacePage">
    <p>正在跳转，请稍后。。。</p>
  </div>
</template>
<script setup lang="ts">
import { listSpaceVoByPageUsingPost } from '@/api/spaceController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { message } from 'ant-design-vue'
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const loginUserStore = useLoginUserStore()

//检查用户是否有个人空间
const checkUserSpace = async () => {
  const loginUser = loginUserStore.loginUser
  //判断用户是否登录
  if (!loginUser?.id) {
    router.replace('/user/login')
    return
  }
  //用户登陆则获取用户创建的空间
  const res = await listSpaceVoByPageUsingPost({
    userId: loginUser.id,
    current: 1,
    pageSize: 1,
  })
  if (res.data.code == 200) {
    if (res.data.data?.records?.length > 0) {
      const space = res.data.data.records[0]
      //跳转到个人空间
      router.replace(`/space/${space.id}`)
    } else {
      router.replace('/add_space')
      message.warn('请先创建个人空间')
    }
  } else {
    message.error('加载个人空间失败,' + res.data.message)
  }
}
onMounted(() => {
  checkUserSpace()
})
</script>

<style scoped>
#addSpacePage {
  max-width: 520px;
  margin: 0 auto;
}
</style>
