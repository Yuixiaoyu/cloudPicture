<template>
  <div id="pictureDetailPage">
    <a-row :gutter="[16, 16]">
      <!-- 图片 预览 -->
      <a-col :sm="24" :md="16" :xl="18">
        <a-card title="图片预览" bodyStyle="text-align:center">
          <a-image
            :src="picture.url"
            style="max-height: 500px; text-align: center; object-fit: contain"
          />
        </a-card>
      </a-col>
      <!-- 图片信息区域 -->
      <a-col :sm="24" :md="8" :xl="6">
        <a-card title="图片信息">
          <a-descriptions :column="1">
            <a-descriptions-item label="图片名称">
              {{ picture.name ?? '未命名' }}
            </a-descriptions-item>
            <a-descriptions-item label="上传者">
              <a-space>
                <a-avatar :size="24" :src="picture.user?.userAvatar" />
                <div>{{ picture.user?.userName }}</div>
              </a-space>
            </a-descriptions-item>

            <a-descriptions-item label="图片分类">
              {{ picture.category ?? '默认' }}
            </a-descriptions-item>
            <a-descriptions-item label="图片标签">
              <a-tag v-for="tag in picture.tags" :key="tag">{{ tag }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="图片格式">
              {{ picture.picFormat ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="图片大小">
              {{ formatSize(picture.picSize) }}
            </a-descriptions-item>
            <a-descriptions-item label="图片宽度">
              {{ picture.picWidth ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="图片高度">
              {{ picture.picHeight ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="图片宽高比">
              {{ picture.picScale ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="主色调">
              <a-space>
                <div
                  v-if="picture.picColor"
                  :style="{
                    width: '16px',
                    height: '16px',
                    backgroundColor: toHexColor(picture.picColor),
                  }"
                />
                {{ picture.picColor ?? '-' }}
              </a-space>
            </a-descriptions-item>
            <a-descriptions-item label="上传时间">
              <!-- 将这里的日期进行格式化 -->
              {{ dayjs(picture.createTime).format('YYYY-MM-DD HH:mm:ss') }}
            </a-descriptions-item>
            <a-descriptions-item label="图片描述">{{ picture.introduction }}</a-descriptions-item>
          </a-descriptions>
          <a-space>
            <a-button type="primary" @click="doDownload">
              免费下载
              <template #icon>
                <DownloadOutlined />
              </template>
            </a-button>
            <a-button
              v-if="canEdit"
              :icon="h(ShareAltOutlined)"
              ghost
              type="primary"
              @click="doShare"
            >
              分享
            </a-button>
            <a-button v-if="canEdit" :icon="h(EditOutlined)" type="default" @click="doEdit">
              编辑
            </a-button>
            <a-button v-if="canEdit" :icon="h(DeleteOutlined)" danger @click="doDelete">
              删除
            </a-button>
          </a-space>
        </a-card>
      </a-col>
    </a-row>
    <ShareModal ref="shareModalRef" :link="shareLink" />
  </div>
</template>
<script setup lang="ts">
import { ref, computed, onMounted, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getPictureVoByIdUsingGet, deletePictureUsingPost } from '@/api/pictureController'
import { message, Modal } from 'ant-design-vue'
import { downloadImage, formatSize, toHexColor } from '@/utils'
import dayjs from 'dayjs'
import {
  EditOutlined,
  DeleteOutlined,
  DownloadOutlined,
  ShareAltOutlined,
} from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import ShareModal from '@/components/ShareModal.vue'

interface Props {
  id: string | number
}

const props = defineProps<Props>()
const picture = ref<API.PictureVO>({})

const loginUserStore = useLoginUserStore()

//是否有编辑权限
const canEdit = computed(() => {
  const loginUser = loginUserStore.loginUser
  //未登录不可编辑
  if (!loginUser.id) {
    return false
  }
  //仅本人或管理员可编辑
  const user = picture.value.user || {}
  return loginUser.id === user.id || loginUser.userRole === 'admin'
})
const router = useRouter()
//编辑操作
const doEdit = () => {
  //携带spaceId
  router.push({
    path: '/add_picture/',
    query: {
      id: picture.value.id,
      spaceId: picture.value.spaceId,
    },
  })
}
//删除操作
const doDelete = () => {
  const id = picture.value.id
  Modal.confirm({
    title: '删除确认',
    content: '确定删除该图片吗？',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      const res = await deletePictureUsingPost({ id })
      if (res.data.code === 200) {
        message.success('删除成功')
        router.push('/')
      } else {
        message.error(res.data.message)
      }
    },
  })
}
const doDownload = () => {
  downloadImage(picture.value.originalUrl)
}

const route = useRoute()
//获取图片详情
const fetchPictureDetail = async () => {
  try {
    const res = await getPictureVoByIdUsingGet({
      id: props.id,
    })
    if (res.data.code === 200 && res.data.data) {
      picture.value = res.data.data
      console.log('picture :>> ', picture)
    } else {
      message.error('获取图片详情失败' + res.data.message)
    }
  } catch (error) {
    message.error('获取图片详情失败' + error)
  }
}

onMounted(() => {
  fetchPictureDetail()
})

// 分享弹窗引用
const shareModalRef = ref()
// 分享链接
const shareLink = ref<string>()

// 分享
const doShare = () => {
  shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.value.id}`
  if (shareModalRef.value) {
    shareModalRef.value.showModal()
  }
}
</script>
<style scoped>
#pictureDetailPage {
}
</style>
