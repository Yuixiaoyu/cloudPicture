<template>
  <div id="globalSider">
    <a-layout-sider
      v-if="loginUserStore.loginUser.id"
      width="200"
      breakpoint="lg"
      collapsed-width="0"
    >
      <a-menu
        v-model:selectedKeys="current"
        mode="inline"
        :items="menuItems"
        @click="doMenuclick"
      />
    </a-layout-sider>
  </div>
</template>
<script lang="ts" setup>
import { computed, h, ref } from 'vue'
import { PictureOutlined, UserOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore'

const loginUserStore = useLoginUserStore()

//未经过滤的菜单项
const menuItems = [
  {
    key: '/',
    icon: () => h(PictureOutlined),
    label: '公共图库',
    title: '公共图库',
  },
  {
    key: '/my_space',
    icon: () => h(UserOutlined),
    label: '我的空间',
    title: '我的空间',
  },
]

const router = useRouter()

//路由跳转函数
const doMenuclick = ({ key }) => {
  router.push({
    path: key,
  })
}
//当前要高亮的菜单项
const current = ref<string[]>(['/'])
//监听路由变化，更新当前要高亮的菜单项
router.afterEach((to, from, next) => {
  current.value = [to.path]
})
</script>

<style scoped>
#globalSider .title-bar {
  display: flex;
  align-items: center;
  /* justify-content: center; */
  height: 60px;
  margin-left: 20px;
}

#globalSider .logo {
  width: 40px;
  height: 40px;
  margin-right: 10px;
}

#globalSider .title {
  font-size: 20px;
  font-weight: bold;
  color: black;
}

.userName {
  display: block;
  height: 60px;
  width: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
