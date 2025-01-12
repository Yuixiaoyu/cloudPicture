<template>
  <div class="picture-upload">
    <a-upload
      list-type="picture-card"
      :show-upload-list="false"
      :custom-request="handleUpload"
      :before-upload="beforeUpload"
    >
      <img v-if="picture?.url" :src="picture?.url" alt="avatar" />
      <div v-else>
        <loading-outlined v-if="loading"></loading-outlined>
        <plus-outlined v-else></plus-outlined>
        <div class="ant-upload-text">点击或拖拽上传图片</div>
      </div>
    </a-upload>
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue'
import { PlusOutlined, LoadingOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { UploadChangeParam, UploadProps } from 'ant-design-vue'
import { uploadPictureUsingPost } from '@/api/pictureController'

interface Props {
  picture?: API.PictureVO
  spaceId: number
  onSuccess?: (newPicture: API.PictureVO) => void
}
const props = defineProps<Props>()

const loading = ref<boolean>(false)

/**
 * 上传图片
 * @param file
 */
const handleUpload = async ({ file }: any) => {
  loading.value = true
  try {
    const params: API.PictureUploadRequest = props.picture ? { id: props.picture.id } : {}
    //追加spaceId属性
    params.spaceId = props.spaceId
    const res = await uploadPictureUsingPost(params, {}, file)
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
.picture-upload :deep(.ant-upload) {
  width: 100% !important;
  height: 100% !important;
  min-height: 152px;
  min-width: 152px;
  overflow: hidden;
}

.picture-upload img {
  max-height: 480px;
  max-width: 100%;
}

.ant-upload-select-picture-card i {
  font-size: 32px;
  color: #999;
}

.ant-upload-select-picture-card .ant-upload-text {
  margin-top: 8px;
  color: #666;
}
</style>
