<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <div class="header-placeholder" v-if="isHeaderFixed"></div>
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout>
        <GlobalSider class="sider" />
        <a-layout-content class="content">
          <router-view></router-view>
        </a-layout-content>
      </a-layout>

      <a-layout-footer class="footer">
        <a href="https://fybreeze.cn/" target="_blank"> 智能图库 by xiaoyu </a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalSider from '@/components/GlobalSider.vue'
import { ref, onMounted, onUnmounted } from 'vue'

const isHeaderFixed = ref(false)
const scrollThreshold = 100 // 滚动多少像素后固定header

const handleScroll = () => {
  isHeaderFixed.value = window.scrollY > scrollThreshold
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
#basicLayout .header {
  background: white;
  padding-inline: 20px;
  height: 64px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

#basicLayout .sider {
  background: #fff;
  border: 1px solid #eee;
  padding-top: 10px;
}

#basicLayout :deep(.ant-menu-root) {
  border: none !important;
  border-bottom: none !important;
  border-inline-end: none !important;
}
#basicLayout :deep(.ant-layout-sider) {
  background: none;
}

.header-placeholder {
  height: 64px;
  width: 100%;
}

#basicLayout .content {
  padding: 20px 20px 0px 28px;
  background: linear-gradient(to right, #fdfcfc, #fff, #fff, #fdfcfc);
  min-height: calc(100vh - 64px);
}

#basicLayout .footer {
  background: linear-gradient(to right, #fdfcfc, #fff, #fff, #fdfcfc);
  text-align: center;
  padding: 10px 0;
}
</style>
