<template>
  <div id="spaceDetailPage">
    <!-- 空间信息 -->
    <a-flex justify="space-between" align="center" style="width: 97%">
      <h2>
        <a-space>
          <svg
            style="height: 42px; width: 40px; margin-bottom: 5px; vertical-align: middle"
            aria-hidden="true"
          >
            <!-- xlink:href执行用哪一个图标,属性值务必#icon-图标名字 -->
            <!-- use标签fill属性可以设置图标的颜色 -->
            <use
              v-if="space.spaceLevel === 0"
              xlink:href="#icon-common"
              fill="red"
              style="width: 100%; height: 100%"
            ></use>
            <use
              v-if="space.spaceLevel === 1"
              xlink:href="#icon-professional"
              fill="red"
              style="width: 100%; height: 100%"
            ></use>
            <use
              v-if="space.spaceLevel === 2"
              xlink:href="#icon-flagship"
              fill="red"
              style="width: 100%; height: 100%"
            ></use>
          </svg>
        </a-space>
        {{ space.spaceName }}({{ SPACE_TYPE_MAP[space.spaceType] }})
      </h2>
      <a-tooltip :title="`占用空间：${formatSize(space.totalSize)}/ ${formatSize(space.maxSize)}`">
        <a-space direction="vertical" style="max-width: 40%; min-width: 35%">
          当前空间已使用:<a-progress
            :percent="((space.totalSize * 100) / space.maxSize).toFixed(1)"
            status="active"
            :stroke-color="{
              '0%': '#108ee9',
              '100%': '#87d068',
            }"
          />
        </a-space>
      </a-tooltip>
      <a-space size="middle">
        <a-button v-if="canUploadPicture" type="primary" :href="`/add_picture?spaceId=${space.id}`">
          +创建图片
        </a-button>
        <a-button
          v-if="canManageSpaceUser"
          type="primary"
          ghost
          :icon="h(TeamOutlined)"
          :href="`/spaceUserManage/${id}`"
        >
          成员管理
        </a-button>

        <a-button
          v-if="canManageSpaceUser"
          :href="`/space_analyze?spaceId=${space.id}`"
          :icon="h(BarChartOutlined)"
        >
          空间分析
        </a-button>
        <a-button
          v-if="canEditPicture"
          type="primary"
          ghost
          :icon="h(EditOutlined)"
          @click="doBatchEdit"
        >
          批量编辑
        </a-button>
      </a-space>
    </a-flex>
    <!-- 图片搜索表单 -->
    <PictureSearchForm :onSearch="onSearch" />
    <!-- 按颜色搜索 -->
    <a-form-item label="按颜色搜索" style="margin-top: 16px">
      <color-picker format="hex" @pureColorChange="onColorChange" />
    </a-form-item>
    <!-- 图片列表 -->
    <PictureList
      :dataList="dataList"
      :loading="loading"
      :show-options="true"
      :onReload="handleReload"
      :canEdit="canEditPicture"
      :canDelete="canDeletePicture"
    />
    <!-- 底部加载触发器 -->
    <div ref="loadingTrigger" class="loading-trigger">
      <a-spin v-if="loading" />
      <span v-else-if="!hasMore && dataList.length > 0">没有更多了</span>
    </div>
    <BatchEditPictureModal
      ref="batchEditPictureModalRef"
      :spaceId="id"
      :pictureList="dataList"
      :onSuccess="onBatchEditPictureSuccess"
    />
  </div>
</template>
<script setup lang="ts">
import {
  ref,
  reactive,
  onMounted,
  nextTick,
  onActivated,
  onUnmounted,
  onDeactivated,
  h,
  watchEffect,
  watch,
  computed,
} from 'vue'
import { getSpaceVoByIdUsingGet } from '@/api/spaceController'
import { message, Modal } from 'ant-design-vue'
import {
  listPictureVoByPageUsingPost,
  listPictureTagCategoryUsingGet,
  searchPictureByColorUsingPost,
} from '@/api/pictureController'
import PictureList from '@/components/PictureList.vue'
import { formatSize } from '@/utils'
import PictureSearchForm from '@/components/PictureSearchForm.vue'
import { ColorPicker } from 'vue3-colorpicker'
import 'vue3-colorpicker/style.css'
import BatchEditPictureModal from '@/components/BatchEditPictureModal.vue'
import { EditOutlined, BarChartOutlined, TeamOutlined } from '@ant-design/icons-vue'
import { BarChart } from 'echarts/charts'
import { SPACE_PERMISSION_ENUM, SPACE_TYPE_MAP } from '@/constants/space'

interface Props {
  id: string | number
}

const props = defineProps<Props>()
const space = ref<API.SpaceVO>({})

//----------------获取空间详情----------------
const fetchSpaceDetail = async () => {
  try {
    const res = await getSpaceVoByIdUsingGet({
      id: props.id,
    })
    if (res.data.code === 200 && res.data.data) {
      space.value = res.data.data
    } else {
      message.error('获取空间详情失败' + res.data.message)
    }
  } catch (error) {
    message.error('获取空间详情失败' + error)
  }
}

onMounted(() => {
  fetchSpaceDetail()
})
//按颜色搜索
const onColorChange = async (color: string) => {
  loading.value = true
  const res = await searchPictureByColorUsingPost({
    picColor: color,
    spaceId: props.id,
  })
  if (res.data.code === 200 && res.data.data) {
    dataList.value = res.data.data ?? []
    total.value = res.data.data.length ?? 0
  } else {
    message.error('按颜色搜索失败' + res.data.message)
  }
  loading.value = false
}

//批量编辑图片表单

const batchEditPictureModalRef = ref()

const onBatchEditPictureSuccess = () => {
  fetchData()
}

const doBatchEdit = () => {
  if (batchEditPictureModalRef.value) {
    batchEditPictureModalRef.value.showModal()
  }
}

// 通用权限检查函数
function createPermissionChecker(permission: string) {
  return computed(() => {
    return (space.value.permissionList ?? []).includes(permission)
  })
}

// 定义权限检查
const canManageSpaceUser = createPermissionChecker(SPACE_PERMISSION_ENUM.SPACE_USER_MANAGE)
const canUploadPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_UPLOAD)
const canEditPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_EDIT)
const canDeletePicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_DELETE)

//----------------获取图片列表----------------
//定义数据
const dataList = ref<API.PictureVO[]>([])
const total = ref<number>(0)
const loading = ref(false)

const searchParams = ref<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

//搜索
const onSearch = async (newSearchParams: API.PictureQueryRequest) => {
  // 重置分页参数
  searchParams.value = {
    ...searchParams.value,
    ...newSearchParams,
    current: 1, // 重置到第一页
  }
  // 重置状态
  hasMore.value = true
  dataList.value = []

  // 重新加载数据
  await fetchData()

  // 重新初始化观察器
  nextTick(() => {
    initObserver()
  })
}

const hasMore = ref(true)
const loadingTrigger = ref<HTMLElement | null>(null)
let observer: IntersectionObserver | null = null

const fetchData = async () => {
  console.log('调用fetchdata:>>')
  if (loading.value || !hasMore.value) {
    console.log('跳过加载', {
      loading: loading.value,
      hasMore: hasMore.value,
      current: searchParams.value.current,
    })
    return
  }

  loading.value = true
  console.log('开始加载数据', searchParams.value.current)

  const params = {
    spaceId: props.id,
    ...searchParams.value,
  }

  try {
    const res = await listPictureVoByPageUsingPost(params)
    if (res.data.code === 200 && res.data.data) {
      const newData = res.data.data.records ?? []
      if (searchParams.value.current === 1) {
        dataList.value = newData
      } else {
        dataList.value = [...dataList.value, ...newData]
      }
      total.value = Number(res.data.data.total) ?? 0
      hasMore.value = dataList.value.length < total.value
      if (hasMore.value) {
        searchParams.value.current += 1
      }
      console.log('加载完成', {
        current: searchParams.value.current,
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

// 添加重新加载的处理函数
const handleReload = async () => {
  // 重置分页参数
  searchParams.value.current = 1
  hasMore.value = true
  dataList.value = []

  // 重新加载数据
  await fetchData()

  // 重新初始化观察器
  nextTick(() => {
    initObserver()
  })
}
//空间id改变时，重新加载数据
watch(
  () => props.id,
  (newSpaceId) => {
    fetchSpaceDetail()
    handleReload()
  },
)
</script>
<style scoped>
#spaceDetailPage .loading-trigger {
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
