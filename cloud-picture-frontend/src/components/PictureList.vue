<template>
  <div class="picture-list">
    <!-- 图片列表 -->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 4, lg: 4, xl: 5, xxl: 6 }"
      :data-source="props.dataList"
      :loading="props.loading"
    >
      <template #renderItem="{ item: picture }">
        <a-list-item style="padding: 0">
          <a-card hoverable :bordered="false" :bodyStyle="{ padding: 0 }">
            <div class="picture-card" @click="doClickPicture(picture)">
              <img
                :alt="picture.name"
                :src="picture.thumbnailUrl ? picture.thumbnailUrl : picture.url"
                class="picture-image"
                loading="lazy"
              />

              <div class="action-overlay" v-if="showOptions">
                <div class="action-buttons">
                  <a-space class="action-button" @click.stop="doSharePicture(picture)">
                    <a-tooltip>
                      <template #title>分享图片</template>
                      <ShareAltOutlined />
                    </a-tooltip>
                  </a-space>
                  <a-space class="action-button" @click.stop="doSearchPicture(picture)">
                    <a-tooltip>
                      <template #title>以图搜图</template>
                      <SearchOutlined />
                    </a-tooltip>
                  </a-space>
                  <a-space class="action-button" @click.stop="doEditPicture(picture)">
                    <a-tooltip>
                      <template #title>编辑图片</template>
                      <EditOutlined />
                    </a-tooltip>
                  </a-space>
                  <a-space class="action-button" @click.stop="doDeletePicture(picture)">
                    <a-tooltip>
                      <template #title>删除图片</template>
                      <DeleteOutlined />
                    </a-tooltip>
                  </a-space>
                </div>
              </div>

              <div class="picture-overlay">
                <div class="picture-info">
                  <h3>{{ picture.name }}</h3>
                  <div class="picture-tags">
                    <a-tag color="blue">{{ picture.category ?? '默认' }}</a-tag>
                    <a-tag v-for="tag in picture.tags" :key="tag" :color="randomColor()">
                      {{ tag }}
                    </a-tag>
                  </div>
                </div>
              </div>
            </div>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
    <ShareModal ref="shareModalRef" :link="shareLink" />
  </div>
</template>
<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ShareAltOutlined,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import { deletePictureUsingPost } from '@/api/pictureController'
import ShareModal from './ShareModal.vue'
import { ref } from 'vue'

interface Props {
  dataList: API.PictureVO[]
  loading: boolean
  showOptions?: boolean
  onReload?: () => void
}

const props = defineProps<Props>()

// 分享弹窗引用
const shareModalRef = ref()
// 分享链接
const shareLink = ref<string>()

// 分享
const doSharePicture = (picture: API.PictureVO, e: Event) => {
  shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.id}`
  if (shareModalRef.value) {
    shareModalRef.value.showModal()
  }
}

const randomColor = () => {
  const colors = ['#FF6347', '#FFD700', '#008000', '#0000FF', '#800080']
  return colors[Math.floor(Math.random() * colors.length)]
}
const router = useRouter()
//跳转到图片详情页
const doClickPicture = (picture: API.PictureVO) => {
  router.push({
    path: `/picture/${picture.id}`,
  })
}

const doEditPicture = (picture: API.PictureVO) => {
  // 处理编辑逻辑
  console.log('编辑图片', picture)
  //跳转时携带SpaceId
  router.push({
    path: '/add_picture/',
    query: {
      id: picture.id,
      spaceId: picture.spaceId,
    },
  })
}

const doDeletePicture = (picture: API.PictureVO) => {
  // 处理删除逻辑
  console.log('删除图片', picture)
  const id = picture.id
  if (!id) return
  Modal.confirm({
    title: '删除确认',
    content: '确定删除该图片吗？',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      const res = await deletePictureUsingPost({ id })
      if (res.data.code === 200) {
        message.success('删除成功')
        props.onReload?.()
      } else {
        message.error(res.data.message)
      }
    },
  })
}
const doSearchPicture = (picture: API.PictureVO) => {
  //打开新的页面
  window.open(`/search_picture?pictureId=${picture.id}`)
}
</script>
<style scoped>
.picture-card {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  cursor: pointer;
  border-radius: 8px;
}

.picture-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.picture-card:hover .picture-image {
  transform: scale(1.05);
}

.picture-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 16px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.3));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.picture-card:hover .picture-overlay {
  opacity: 1;
}

.picture-info {
  color: white;
  width: 100%;
}

.picture-info h3 {
  margin: 0 0 8px 0;
  color: white;
  font-size: 16px;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
}

.picture-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.picture-list .search-bar {
  max-width: 480px;
  margin: 0 auto 20px auto;
}

.picture-list .tag-bar {
  margin: 0 auto 20px auto;
}

.loading-trigger {
  height: 150px;
  margin: 20px auto;
  text-align: center;
  color: #999;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

:deep(.ant-card) {
  background: transparent;
  border-radius: 8px;
  overflow: hidden;
}

:deep(.ant-card-body) {
  padding: 0;
}

.action-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  padding: 16px;
  background: linear-gradient(rgba(0, 0, 0, 0.5), transparent);
  transform: translateY(-100%);
  transition: transform 0.3s ease;
  display: flex;
  justify-content: center;
  gap: 16px;
}

.picture-card:hover .action-overlay {
  transform: translateY(0);
}

.action-buttons {
  display: flex;
  gap: 16px;
}

.action-button {
  padding: 4px 8px;
  font-size: 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-button:hover {
  background-color: rgba(255, 255, 255, 0.2);
  /* font-weight: bold; */
  transform: scale(1.15);
}

.action-button :deep(.anticon) {
  color: white;
  transition: color 0.3s ease;
}

.action-button span {
  color: white;
  transition: color 0.3s ease;
}

.action-button:hover :deep(.anticon),
.action-button:hover span {
  color: #1890ff;
}

:deep(.ant-card-actions) {
  display: none;
}
</style>
