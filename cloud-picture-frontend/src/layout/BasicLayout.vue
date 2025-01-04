<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <div class="header-placeholder" v-if="isHeaderFixed"></div>
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout-content class="content">
        <router-view></router-view>
      </a-layout-content>
      <a-layout-footer class="footer">
        <a href="https://fybreeze.cn/" target="_blank"> 云图床 by xiaoyu </a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
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

.header-placeholder {
  height: 64px;
  width: 100%;
}

#basicLayout .content {
  padding: 20px 20px 0px 20px;
  background: linear-gradient(to right, #f4f2f2, #fff, #fff, #f4f2f2);
  min-height: calc(100vh - 64px);
}

#basicLayout .footer {
  background: linear-gradient(to right, #f4f2f2, #fff, #fff, #f4f2f2);
  text-align: center;
  padding: 10px 0;
}
</style>
