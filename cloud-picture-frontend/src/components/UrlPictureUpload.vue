<template>
  <div class="url-picture-upload">
    <a-input-group compact>
      <a-input
        v-model:value="fileUrl"
        style="width: calc(100% - 100px)"
        placeholder="请输入图片URL地址"
      />
      <a-button type="primary" style="width: 100px" :loading="loading" @click="handleUpload"
        >提交</a-button
      >
    </a-input-group>
    <img v-if="picture?.url" :src="picture?.url" alt="avatar" />
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue'
import { PlusOutlined, LoadingOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { UploadChangeParam, UploadProps } from 'ant-design-vue'
import { uploadPictureByUrlUsingPost, uploadPictureUsingPost } from '@/api/pictureController'

interface Props {
  picture?: API.PictureVO
  spaceId: number
  onSuccess?: (newPicture: API.PictureVO) => void
}
const props = defineProps<Props>()
const fileUrl = ref<string>('')
const loading = ref<boolean>(false)

/**
 * 上传图片
 * @param file
 */
const handleUpload = async () => {
  loading.value = true
  try {
    const params: API.PictureUploadRequest = { fileUrl: fileUrl.value }
    params.spaceId = props.spaceId
    if (props.picture) {
      params.id = props.picture.id
    }
    const res = await uploadPictureByUrlUsingPost(params)
    console.log(res)
    if (res.data.code === 200 && res.data.data) {
      message.success('上传成功')
      props.onSuccess?.(res.data.data)
    } else {
      message.error('上传失败')
    }
  } catch (error) {
    console.log('图片上传失败', error)
    message.error('上传失败' + error.message)
  }
  loading.value = false
}

/**
 * 上传前校验
 * @param file
 */
const beforeUpload = (file: UploadProps['fileList'][number]) => {
  //校验图片类型
  const isJpgOrPng =
    file.type === 'image/jpeg' ||
    file.type === 'image/png' ||
    file.type === 'image/jpg' ||
    file.type === 'image/webp'
  if (!isJpgOrPng) {
    message.error('不支持的文件类型，只支持jpg、jpeg、png、webp')
  }
  //校验图片大小
  const isLt2M = file.size / 1024 / 1024 < 3
  if (!isLt2M) {
    message.error('图片大小不能超过3MB')
  }
  return isJpgOrPng && isLt2M
}
</script>
<style scoped>
.url-picture-upload :deep(.ant-upload) {
  width: 100% !important;
  height: 100% !important;
  min-height: 152px;
  min-width: 152px;
}

.url-picture-upload img {
  max-height: 480px;
  max-width: 100%;
  display: block;
  margin: 10px auto;
  border-radius: 4px;
}
</style>
