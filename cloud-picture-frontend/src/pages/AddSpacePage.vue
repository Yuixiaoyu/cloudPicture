<template>
  <div id="addSpacePage">
    <h2 style="margin-bottom: 16px">
      {{ route.query?.id ? '编辑' : '添加' }}{{ SPACE_TYPE_MAP[spaceType] }}
    </h2>

    <!-- 空间信息表单 -->
    <a-form name="spaceForm" layout="vertical" :model="spaceForm" @finish="handleSubmit">
      <a-form-item label="空间名称" name="name">
        <a-input v-model:value="spaceForm.spaceName" placeholder="输入空间名称" allow-cler />
      </a-form-item>
      <a-form-item label="空间级别" name="spaceLevel">
        <a-select
          v-model:value="spaceForm.spaceLevel"
          placeholder="请选择空间级别"
          :options="SPACE_LEVEL_OPTIONS"
          style="min-width: 160px"
          allow-clear
        >
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" :loading="loading" style="width: 100%">
          提交
        </a-button>
      </a-form-item>
    </a-form>
    <!-- 空间级别介绍 -->
    <a-card title="空间级别介绍">
      <a-typography-paragraph>
        *目前仅能开通普通版，如需升级，请联系
        <a href="https://www.fybreeze.cn" target="_blank">xiaoyu</a>
      </a-typography-paragraph>

      <a-typography-paragraph v-for="spaceLevel in spaceLevelList" :key="spaceLevel.value">
        <svg
          style="height: 22px; width: 20px; margin-bottom: 5px; vertical-align: middle"
          aria-hidden="true"
        >
          <!-- xlink:href执行用哪一个图标,属性值务必#icon-图标名字 -->
          <!-- use标签fill属性可以设置图标的颜色 -->
          <use
            v-if="spaceLevel.value === 0"
            xlink:href="#icon-common"
            fill="red"
            style="width: 100%; height: 100%"
          ></use>
          <use
            v-if="spaceLevel.value === 1"
            xlink:href="#icon-professional"
            fill="red"
            style="width: 100%; height: 100%"
          ></use>
          <use
            v-if="spaceLevel.value === 2"
            xlink:href="#icon-flagship"
            fill="red"
            style="width: 100%; height: 100%"
          ></use>
        </svg>

        {{ spaceLevel.text }}: 大小{{ formatSize(spaceLevel.maxSize) }}, 数量：{{
          spaceLevel.maxCount
        }}
      </a-typography-paragraph>
    </a-card>
  </div>
</template>
<script setup lang="ts">
import {
  addSpaceUsingPost,
  editSpaceUsingPost,
  getSpaceVoByIdUsingGet,
  listSpaceLevelUsingGet,
  updateSpaceUsingPost,
} from '@/api/spaceController'
import { SPACE_LEVEL_OPTIONS, SPACE_TYPE_ENUM, SPACE_TYPE_MAP } from '@/constants/space'
import { formatSize } from '@/utils'
import { message } from 'ant-design-vue'
import { reactive, ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const space = ref<API.SpaceVO>()
const spaceForm = reactive<API.SpaceAddRequest | API.SpaceEditRequest>({})
const loading = ref(false)

const router = useRouter()

const spaceLevelList = ref<API.SpaceLevel[]>([])

const route = useRoute()
// 空间类别,默认为私有空间
const spaceType = computed(() => {
  if (route.query?.type) {
    return Number(route.query.type)
  }
  return SPACE_TYPE_ENUM.PRIVATE
})

const fetchSpaceLevelList = async () => {
  const res = await listSpaceLevelUsingGet()
  if (res.data.code === 200 && res.data.data) {
    spaceLevelList.value = res.data.data
  } else {
    message.error('获取空间级别失败,' + res.data.message)
  }
}

onMounted(() => {
  fetchSpaceLevelList()
})

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: API.SpaceAddRequest) => {
  const spaceId = space.value?.id
  loading.value = true
  let res
  if (spaceId) {
    // 编辑
    res = await updateSpaceUsingPost({
      id: spaceId,
      ...spaceForm,
    })
  } else {
    // 新增
    res = await addSpaceUsingPost({
      ...spaceForm,
      spaceType: spaceType.value,
    })
  }
  if (res.data.code === 200 && res.data.data) {
    message.success('操作成功')
    //跳转到空间详情页
    router.push({
      path: `/space/${res.data.data}`,
    })
  } else {
    message.error('操作失败' + res.data.message)
  }
  loading.value = false
}

//获取老数据
const getOldSpace = async () => {
  const id = route.query?.id
  if (id) {
    const res = await getSpaceVoByIdUsingGet({ id })
    if (res.data.code === 200 && res.data.data) {
      const data = res.data.data
      space.value = data
      spaceForm.spaceName = data.spaceName
      spaceForm.spaceLevel = data.spaceLevel
    } else {
      message.error('获取空间失败' + res.data.message)
    }
  }
}

onMounted(() => {
  getOldSpace()
})
</script>

<style scoped>
#addSpacePage {
  max-width: 520px;
  margin: 0 auto;
}
</style>
