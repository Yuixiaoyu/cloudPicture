<template>
  <div id="SearchPicturePage">
    <div style="font-size: 22px; color: #000; font-weight: 600; margin-bottom: 16px">以图搜图</div>
    <a-card hoverable style="width: 270px; overflow: hidden">
      <template #cover>
        <img :alt="picture.name" :src="picture.url" class="picture-image" loading="lazy" />
      </template>
    </a-card>
    <br />
    <!-- <hr /> -->
    <h3 style="margin: 16px 5px">搜索结果</h3>
    <a-spin :spinning="loading">
      <!-- 瀑布流展示图片列表 -->
      <div class="masonry-container">
        <div
          v-for="(item, index) in dataList"
          :key="index"
          class="masonry-item"
          @click="handleImageClick(item)"
        >
          <a-card hoverable :bordered="false" :bodyStyle="{ padding: 0 }">
            <div class="picture-card">
              <img :src="item.thumbUrl" class="picture-image" :style="{ height: 'auto' }" />
              <div class="picture-overlay">
                <div class="picture-info">
                  <h3>相似度: {{ (item.score * 100).toFixed(2) }}%</h3>
                </div>
              </div>
            </div>
          </a-card>
        </div>
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  listPictureVoByPageUsingPost,
  listPictureTagCategoryUsingGet,
  getPictureVoByIdUsingGet,
  deletePictureUsingPost,
  searchPictureByPictureUsingPost,
} from '@/api/pictureController'
import { message, Modal } from 'ant-design-vue'
import { downloadImage, formatSize } from '@/utils'
import dayjs from 'dayjs'
import { EditOutlined, DeleteOutlined, DownloadOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore'

const picture = ref<API.PictureVO>({})
const route = useRoute()
const pictureId = computed(() => {
  return route.query.pictureId
})
const loading = ref(true)

//获取图片详情
const fetchPictureDetail = async () => {
  try {
    const res = await getPictureVoByIdUsingGet({
      id: pictureId.value,
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

const dataList = ref<API.ImageSearchResult[]>([])

//获取搜图结果
const fetchResultData = async () => {
  loading.value = true
  try {
    const res = await searchPictureByPictureUsingPost({
      pictureId: pictureId.value,
    })
    if (res.data.code === 200 && res.data.data) {
      dataList.value = res.data.data ?? []
      console.log('searchPicture :>> ', dataList.value)
    } else {
      message.error('获取数据失败' + res.data.message)
    }
  } catch (error) {
    message.error('获取数据失败' + error)
  }
  loading.value = false
}

onMounted(() => {
  fetchResultData()
})

const randomColor = () => {
  const colors = ['#FF6347', '#FFD700', '#008000', '#0000FF', '#800080']
  return colors[Math.floor(Math.random() * colors.length)]
}

const handleImageClick = (item: API.ImageSearchResult) => {
  window.open(`/picture/${item.id}`, '_blank')
}
</script>

<style scoped>
#SearchPicturePage {
  padding: 5px 20px;
}

/* 瀑布流容器样式 */
.masonry-container {
  column-count: 5;
  column-gap: 10px;
  padding: 10px;
}

@media screen and (max-width: 1600px) {
  .masonry-container {
    column-count: 4;
  }
}

@media screen and (max-width: 1200px) {
  .masonry-container {
    column-count: 3;
  }
}

@media screen and (max-width: 768px) {
  .masonry-container {
    column-count: 2;
  }
}

.masonry-item {
  break-inside: avoid;
  margin-bottom: 10px;
}

.picture-card {
  position: relative;
  width: 100%;
  overflow: hidden;
  cursor: pointer;
  border-radius: 8px;
}

.picture-image {
  width: 100%;
  display: block;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.picture-card:hover .picture-image {
  transform: scale(1.05);
}

.picture-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.3), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
  display: flex;
  align-items: flex-start;
  padding: 12px;
}

.picture-card:hover .picture-overlay {
  opacity: 1;
}

.picture-info {
  color: white;
  width: 100%;
}

.picture-info h3 {
  margin: 0;
  color: white;
  font-size: 14px;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
}

:deep(.ant-card) {
  background: transparent;
  border-radius: 8px;
  overflow: hidden;
}

:deep(.ant-card-body) {
  padding: 0;
}
</style>
