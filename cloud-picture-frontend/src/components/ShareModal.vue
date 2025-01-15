<template>
  <div>
    <a-modal v-model:visible="visiable" title="分享图片" :footer="false" @cancel="closeModal">
      <div style="margin-top: 12px"></div>
      <a-space align="baseline">
        <h4>图片链接:</h4>
        <a-typography-link copyable>{{ link }} </a-typography-link>
      </a-space>
      <div style="margin-bottom: 16px"></div>
      <h4>手机扫码查看</h4>
      <a-qrcode
        :value="link"
        error-level="H"
        icon="https://cloud-picture-1317444877.cos.ap-chengdu.myqcloud.com/public/1867200121277091842/2025-01-13_90qyluie9xjgwzx4.logo.png"
      ></a-qrcode>
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue'
const open = ref<boolean>(false)

interface Props {
  link: string
}

const props = withDefaults(defineProps<Props>(), {
  link: 'https://www.fybreeze.cn',
})

const visiable = ref<boolean>(false)

//打开弹窗
const showModal = () => {
  visiable.value = true
}
//关闭弹窗
const closeModal = () => {
  visiable.value = false
}
//暴露函数给父组件调用
defineExpose({
  showModal,
})
</script>
