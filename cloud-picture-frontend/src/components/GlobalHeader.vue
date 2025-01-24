<template>
  <div id="globalHeader">
    <a-row :wrap="false">
      <a-col flex="200px">
        <router-link to="/">
          <div class="title-bar">
            <img class="logo" src="../assets/logo.png" alt="logo" />
            <div class="title">智能图库</div>
          </div>
        </router-link>
      </a-col>
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          @click="doMenuclick"
        />
      </a-col>
      <a-col flex="100px">
        <div class="user-login-status">
          <div v-if="userLoginStore.loginUser.id">
            <a-dropdown>
              <a-space :wrap="false">
                <a-avatar :size="40" :src="userAvatar"></a-avatar>
                <a-tooltip placement="left" :title="userLoginStore.loginUser.userName ?? '无名'">
                  <a-span class="userName">{{
                    userLoginStore.loginUser.userName ?? '无名'
                  }}</a-span>
                </a-tooltip>
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item>
                    <router-link to="/my_space">
                      <UserOutlined />
                      我的空间
                    </router-link>
                  </a-menu-item>

                  <a-menu-item @click="doLoginOut">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import { computed, h, ref } from 'vue'
import { HomeOutlined, LogoutOutlined, UserOutlined } from '@ant-design/icons-vue'
import { MenuProps, message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { userLogoutUsingGet } from '@/api/userController'

// const items = ref<MenuProps['items']>()

//未经过滤的菜单项
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/add_picture',
    label: '创建图片',
    title: '创建图片',
  },
  {
    key: '/admin/pictureManage',
    label: '图片管理',
    title: '图片管理',
  },
  {
    key: '/admin/spaceManage',
    label: '空间管理',
    title: '空间管理',
  },
  {
    key: 'other',
    label: h('a', { href: 'https://www.fybreeze.cn', target: '_blank' }, '博客'),
    title: '博客',
  },
]

const router = useRouter()

const userLoginStore = useLoginUserStore()
const userAvatar = ref<string>('')
//判断当前用户是否有头像，如果有则显示头像，没有则显示默认的头像
if (userLoginStore.loginUser && userLoginStore.loginUser.userAvatar) {
  userAvatar.value = userLoginStore.loginUser.userAvatar
} else {
  userAvatar.value =
    'https://cloud-picture-1317444877.cos.ap-chengdu.myqcloud.com/public/1867200121277091842/2025-01-11_dm6t2rpf._5jgAUzO8FRcGQPcTFWsEgAAAA.webp'
}
//暂未处理，页面刷新后，用户短暂消失，自动获取到用户之后，头像会显示默认头像
//easy处理，定时器，1.2s后获取用户头像
setTimeout(() => {
  if (userLoginStore.loginUser && userLoginStore.loginUser.userAvatar) {
    userAvatar.value = userLoginStore.loginUser.userAvatar
  }
}, 1200)

//根据权限过滤菜单项
const filterMenu = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    //只有管理员才能看到/admin开头的菜单
    if (menu?.key?.startsWith('/admin')) {
      const loginUser = userLoginStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

//展示在菜单的路由数组
const items = computed(() => filterMenu(originItems))

console.log(userLoginStore.loginUser)

//路由跳转函数
const doMenuclick = ({ key }) => {
  router.push({
    path: key,
  })
}
//当前要高亮的菜单项
const current = ref<string[]>(['/'])
router.afterEach((to, from, next) => {
  current.value = [to.path]
})

//退出登录
const doLoginOut = async () => {
  const res = await userLogoutUsingGet()
  if (res.data.code === 200) {
    userLoginStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出成功')
    router.push('/user/login')
  } else {
    message.error('退出登录失败:' + res.data.message)
  }
}
</script>

<style scoped>
#globalHeader .title-bar {
  display: flex;
  align-items: center;
  /* justify-content: center; */
  height: 60px;
  margin-left: 20px;
}

#globalHeader .logo {
  width: 40px;
  height: 40px;
  margin-right: 10px;
}

#globalHeader .title {
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
