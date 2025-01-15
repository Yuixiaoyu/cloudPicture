<template>
  <div class="picture-search-form">
    <!-- 搜索框 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="关键词">
        <a-input
          v-model:value="searchParams.searchText"
          placeholder="从名称和简介中搜索"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="分类" name="category">
        <a-auto-complete
          v-model:value="searchParams.category"
          :options="categoryOptions"
          style="width: 200px"
          placeholder="输入分类"
          allow-cler
        />
      </a-form-item>
      <a-form-item label="标签" name="tags">
        <a-select
          v-model:value="searchParams.tags"
          mode="tags"
          placeholder="输入标签"
          style="width: 200px"
          :options="tagsOptions"
          allow-cler
        ></a-select>
      </a-form-item>
      <a-form-item label="日期" name="dateRange">
        <a-range-picker
          style="width: 400px"
          show-time
          :placeholder="['编辑开始日期', '编辑结束日期']"
          format="YYYY/MM/DD HH:mm:ss"
          v-model:value="dateRange"
          :presets="rangePresets"
          @change="onRangeChange"
        />
      </a-form-item>
      <a-form-item label="宽度" name="picWidth">
        <a-input-number v-model:value="searchParams.picWidth" />
      </a-form-item>
      <a-form-item label="高度" name="picHeight">
        <a-input-number v-model:value="searchParams.picHeight" />
      </a-form-item>
      <a-form-item label="格式" name="picFormat">
        <a-input v-model:value="searchParams.picFormat" placeholder="输入图片格式" allow-clear />
      </a-form-item>
      <a-form-item>
        <a-space>
          <a-button type="primary" html-type="submit">搜索</a-button>
          <a-button type="dashed" html-type="reset" @click="doClear">重置</a-button>
        </a-space>
      </a-form-item>
    </a-form>

    <div style="margin-bottom: 16px"></div>
  </div>
</template>
<script lang="ts" setup>
import { reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { onMounted } from 'vue'
import { listPictureTagCategoryUsingGet } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'

interface Props {
  onSearch?: (searchParams: API.PictureQueryRequest) => void
}

const props = defineProps<Props>()

const searchParams = reactive<API.PictureQueryRequest>({})

const doSearch = () => {
  props.onSearch?.(searchParams)
}

const dateRange = ref<[]>([])
/**
 * 日期范围更改时触发
 */
const onRangeChange = (dates: any[], dateStrings: string[]) => {
  if (dates?.length >= 2) {
    searchParams.startDateTime = dates[0].toDate()
    searchParams.endDateTime = dates[1].toDate()
  } else {
    searchParams.startDateTime = undefined
    searchParams.endDateTime = undefined
  }
}
const rangePresets = ref([
  { label: '过去 7 天', value: [dayjs().add(-7, 'd'), dayjs()] },
  { label: '过去 14 天', value: [dayjs().add(-14, 'd'), dayjs()] },
  { label: '过去 30 天', value: [dayjs().add(-30, 'd'), dayjs()] },
  { label: '过去 90 天', value: [dayjs().add(-90, 'd'), dayjs()] },
])

const categoryOptions = ref<String[]>()
const tagsOptions = ref<String[]>()

/**
 * 获取标签和分类选项
 * @param values
 */
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 200 && res.data.data) {
    categoryOptions.value = (res.data.data.categoryList ?? []).map((data: String) => {
      return {
        label: data,
        value: data,
      }
    })
    tagsOptions.value = (res.data.data.tagList ?? []).map((data: String) => {
      return {
        label: data,
        value: data,
      }
    })
  } else {
    message.error('获取标签和分类失败' + res.data.message)
  }
}

onMounted(() => {
  getTagCategoryOptions()
})

const doClear = () => {
  // 清空表单
  Object.keys(searchParams).forEach((key) => {
    searchParams[key] = undefined
  })
  //日期时间单独清空，必须设置为空数组
  dateRange.value = []
  // 清空后重新搜索
  props.onSearch?.(searchParams)
}
</script>

<style scoped>
.picture-search-form .ant-form-item {
  margin-top: 16px;
}
</style>
