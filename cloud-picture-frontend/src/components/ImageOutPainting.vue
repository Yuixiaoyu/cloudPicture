<template>
  <a-modal
    class="image-out-painting"
    v-model:visible="visiable"
    title="AI扩图"
    :footer="false"
    @cancel="closeModal"
  >
    <a-row gutter="16">
      <a-col span="12">
        <h4>原始图片</h4>
        <img :src="picture?.url" :alt="picture?.name" style="max-width: 100%" />
      </a-col>
      <a-col span="12">
        <h4>扩图结果</h4>
        <img
          v-if="resultImageUrl"
          :src="resultImageUrl"
          :alt="picture?.name"
          style="max-width: 100%"
        />
      </a-col>
    </a-row>
    <div style="margin-bottom: 16px" />
    <a-flex gap="16" justify="center">
      <a-button type="primary" :loading="!!taskId" ghost @click="createTask">生成图片</a-button>
      <a-button v-if="resultImageUrl" type="primary" :loading="uploadLoading" @click="handleUpload"
        >应用结果</a-button
      >
    </a-flex>
  </a-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  createPictureOutPaintingTaskUsingPost,
  getPictureOutPaintingTaskUsingGet,
  uploadPictureByUrlUsingPost,
  uploadPictureUsingPost,
} from '@/api/pictureController'

interface Props {
  picture?: API.PictureVO
  spaceId?: number
  onSuccess?: (newPicture: API.PictureVO) => void
}

const props = defineProps<Props>()

const resultImageUrl = ref<string>()

const taskId = ref<string>()

//创建任务
const createTask = async () => {
  if (!props.picture?.id) {
    return
  }
  const res = await createPictureOutPaintingTaskUsingPost({
    pictureId: props.picture.id,
    //根据需要设置扩图参数
    parameters: {
      xScale: 2,
      yScale: 2,
    },
  })
  if (res.data.code === 200 && res.data.data) {
    message.success('任务创建成功，请耐心等待，不要退出界面')
    console.log(res.data.data.output?.taskId)
    taskId.value = res.data.data.output?.taskId
    //开启轮询
    startPolling()
  } else {
    message.error('图片上传失败，' + res.data.message)
  }
}
//轮询定时器
let pollingTimer: NodeJS.Timeout = null

//开启轮询
const startPolling = () => {
  if (!taskId.value) {
    return
  }
  pollingTimer = setInterval(async () => {
    try {
      const res = await getPictureOutPaintingTaskUsingGet({
        taskId: taskId.value,
      })
      if (res.data.code === 200 && res.data.data) {
        const taskResult = res.data.data.output
        if (taskResult?.taskStatus === 'SUCCEEDED') {
          message.success('扩图任务执行成功')
          resultImageUrl.value = taskResult.outputImageUrl
          //清理轮询
          clearPolling()
        } else if (taskResult?.taskStatus === 'FAILED') {
          message.error('扩图任务执行失败')
          //清理轮询
          clearPolling()
        }
      }
    } catch (error) {
      message.error('扩图任务轮询失败' + error.message)
      //清理轮询
      clearPolling()
    }
  }, 3000)
}

//关闭轮询
const clearPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
    taskId.value = null
  }
}
const uploadLoading = ref(false)
/**
 * 上传图片
 * @param file
 */
const handleUpload = async () => {
  uploadLoading.value = true
  try {
    const params: API.PictureUploadRequest = {
      fileUrl: resultImageUrl.value,
      spaceId: props.spaceId,
    }
    if (props.picture) {
      params.id = props.picture.id
    }
    const res = await uploadPictureByUrlUsingPost(params)
    console.log(res)
    if (res.data.code === 200 && res.data.data) {
      message.success('上传成功')
      //将上传成功的图片信息传递给父组件
      props.onSuccess?.(res.data.data)
      //关闭弹窗
      closeModal()
    } else {
      message.error('上传失败')
    }
  } catch (error) {
    console.log('图片上传失败', error)
    message.error('上传失败' + error.message)
  }
  uploadLoading.value = false
}

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

<style>
.image-out-painting {
  text-align: center;
}
</style>
