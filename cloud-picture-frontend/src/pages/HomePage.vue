<template>
  <div id="homePage">
    <div class="search-bar">
      <!-- 搜索框 -->
      <a-input-search
        v-model:value="searchParams.searchText"
        placeholder="从海量图片中搜索"
        enter-button="搜索"
        size="large"
        @search="onSearch"
      />
    </div>
    <a-tabs v-model:active-key="selectedCategory" @change="doSearch">
      <a-tab-pane key="all" tab="全部" />
      <a-tab-pane v-for="category in categoryList" :key="category" :tab="category" />
    </a-tabs>

    <div class="tag-bar">
      <span style="margin-right: 8px">标签:</span>
      <a-space :size="[0, 8]" wrap>
        <a-checkable-tag
          v-for="(tag, index) in tagList"
          :key="tag"
          v-model:checked="selectedTagList[index]"
          @change="doSearch"
        >
          {{ tag }}
        </a-checkable-tag>
      </a-space>
    </div>
    <!-- 图片列表 -->
    <PictureList :dataList="dataList" :loading="loading" />
    <!-- 底部加载触发器 -->
    <div ref="loadingTrigger" class="loading-trigger">
      <a-spin v-if="loading" />
      <span v-else-if="!hasMore && dataList.length > 0">没有更多了</span>
    </div>
  </div>
</template>
<script setup lang="ts">
import {
  ref,
  reactive,
  computed,
  onMounted,
  onUnmounted,
  onActivated,
  onDeactivated,
  nextTick,
} from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import {
  listPictureVoByPageUsingPost,
  listPictureTagCategoryUsingGet,
  listPictureVoByPageWitchCacheUsingPost,
} from '@/api/pictureController'
import { message } from 'ant-design-vue'
import PictureList from '@/components/PictureList.vue'

//定义数据
const dataList = ref<API.PictureVO[]>([])
const total = ref<number>(0)
const loading = ref(false)

const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

const doSearch = async () => {
  // 重置状态
  searchParams.current = 1
  hasMore.value = true
  dataList.value = []
  loading.value = false // 确保重置 loading 状态

  // 先获取数据
  await fetchData()

  // 等待 DOM 更新后重新初始化观察器
  nextTick(() => {
    initObserver()
  })
}

const hasMore = ref(true)
const loadingTrigger = ref<HTMLElement | null>(null)
let observer: IntersectionObserver | null = null

const fetchData = async () => {
  if (loading.value || !hasMore.value) {
    console.log('跳过加载', {
      loading: loading.value,
      hasMore: hasMore.value,
      current: searchParams.current,
    })
    return
  }

  loading.value = true
  console.log('开始加载数据', searchParams.current)

  const params = {
    ...searchParams,
    tags: [] as string[],
  }

  // 修改分类处理逻辑
  if (selectedCategory.value !== 'all') {
    params.category = selectedCategory.value
  } else {
    delete params.category // 使用 delete 而不是设置为 undefined
  }

  selectedTagList.value.forEach((useTag, index) => {
    if (useTag) {
      params.tags.push(tagList.value[index])
    }
  })

  try {
    const res = await listPictureVoByPageUsingPost(params)
    if (res.data.code === 200 && res.data.data) {
      const newData = res.data.data.records ?? []
      if (searchParams.current === 1) {
        dataList.value = newData
        console.log('dataList>>>', dataList.value)
      } else {
        dataList.value = [...dataList.value, ...newData]
      }
      total.value = Number(res.data.data.total) ?? 0
      hasMore.value = dataList.value.length < total.value
      if (hasMore.value) {
        searchParams.current += 1
      }
      console.log('加载完成', {
        current: searchParams.current,
        total: total.value,
        hasMore: hasMore.value,
        dataLength: dataList.value.length,
      })
    } else {
      message.error(res.data.message)
      hasMore.value = false
    }
  } catch (error) {
    message.error('加载失败')
    hasMore.value = false
  } finally {
    loading.value = false
  }
}

// 修改初始化观察器的函数
const initObserver = () => {
  // 先清理旧的观察器
  if (observer) {
    observer.disconnect()
    observer = null
  }

  // 创建新的观察器
  observer = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting && !loading.value && hasMore.value) {
        console.log('触发加载')
        fetchData()
      }
    },
    {
      root: null,
      rootMargin: '200px',
      threshold: 0.1,
    },
  )

  // 确保元素存在后再观察
  if (loadingTrigger.value) {
    observer.observe(loadingTrigger.value)
    console.log('开始观察', {
      category: selectedCategory.value,
      hasMore: hasMore.value,
      loading: loading.value,
    })
  }
}

// 修改生命周期钩子
onMounted(async () => {
  // 初始加载数据
  await fetchData()
  // 先获取标签和分类
  await getTagCategoryOptions()
  // 等待 DOM 更新后初始化观察器
  nextTick(() => {
    initObserver()
  })
})

// 修改 onActivated 钩子
onActivated(() => {
  // 如果没有观察器，重新初始化
  if (!observer) {
    nextTick(() => {
      initObserver()
    })
  }
})

// 添加 onDeactivated 钩子，处理页面失活时的清理
onDeactivated(() => {
  if (observer) {
    observer.disconnect()
    observer = null
  }
})

// 修改 onUnmounted 钩子
onUnmounted(() => {
  if (observer) {
    observer.disconnect()
    observer = null
  }
})

const onSearch = async () => {
  // 重置状态
  searchParams.current = 1
  hasMore.value = true
  dataList.value = []
  loading.value = false

  // 先获取数据
  await fetchData()

  // 等待 DOM 更新后重新初始化观察器
  nextTick(() => {
    initObserver()
  })
}
const categoryList = ref<string[]>()
const selectedCategory = ref<string>('all')
const tagList = ref<string[]>([])
const selectedTagList = ref<boolean[]>([])

/**
 * 获取标签和分类选项
 * @param values
 */
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 200 && res.data.data) {
    categoryList.value = res.data.data.categoryList ?? []
    tagList.value = res.data.data.tagList ?? []
  } else {
    message.error('获取标签和分类失败' + res.data.message)
  }
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

#homePage .search-bar {
  max-width: 480px;
  margin: 0 auto 20px auto;
}

#homePage .tag-bar {
  margin: 0 auto 20px auto;
}

.loading-trigger {
  height: 50px;
  margin: 20px auto;
  text-align: center;
  color: #999;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
