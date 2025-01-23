<template>
  <div id="spaceManagePage">
    <a-flex justify="space-between">
      <h2>空间管理</h2>
      <a-space>
        <a-button type="primary" href="/add_space">+创建空间</a-button>
        <a-button type="primary" ghost href="/space_analyze?queryPublic=1">分析公共图库</a-button>
        <a-button type="dashed" href="/space_analyze?queryAll=1">分析全部空间</a-button>
      </a-space>
    </a-flex>
    <!-- 搜索框 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="空间名称">
        <a-input v-model:value="searchParams.spaceName" placeholder="请输入空间名称" allow-clear />
      </a-form-item>
      <a-form-item label="空间级别" name="spaceLevel">
        <a-select
          v-model:value="searchParams.spaceLevel"
          placeholder="请选择空间级别"
          :options="SPACE_LEVEL_OPTIONS"
          style="min-width: 160px"
          allow-clear
        >
        </a-select>
      </a-form-item>
      <a-form-item label="空间类型" name="spaceType">
        <a-select
          v-model:value="searchParams.spaceType"
          placeholder="请选择空间类型"
          :options="SPACE_TYPE_OPTIONS"
          style="min-width: 160px"
          allow-clear
        >
        </a-select>
      </a-form-item>
      <a-form-item label="用户id">
        <a-input v-model:value="searchParams.userId" placeholder="请输入用户id" allow-clear />
      </a-form-item>

      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <div style="margin-bottom: 16px"></div>
    <!-- 表格 -->
    <a-table :columns="columns" :data-source="dataList" :pagination="paginaction">
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'spaceLevel'">
          <div>{{ SPACE_LEVEL_MAP[record.spaceLevel] }}</div>
        </template>
        <template v-if="column.dataIndex === 'spaceType'">
          <a-tag>{{ SPACE_TYPE_MAP[record.spaceType] }}</a-tag>
        </template>
        <template v-if="column.dataIndex === 'spaceUseInfo'">
          <div>大小：{{ formatSize(record.totalSize) }}/{{ formatSize(record.maxSize) }}</div>
          <div>数量：{{ record.totalCount }} / {{ record.maxCount }}</div>
        </template>

        <template v-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-if="column.dataIndex === 'editTime'">
          {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space wrap>
            <a-button type="link" :href="`/space_analyze?spaceId=${record.id}`"> 分析 </a-button>
            <a-button type="link" :href="`/add_space?id=${record.id}`"> 编辑 </a-button>
            <a-button danger @click="doDelete(record.id)">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { deleteSpaceUsingPost, listSpaceByPageUsingPost } from '@/api/spaceController'
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import router from '@/router'
import {
  SPACE_LEVEL_MAP,
  SPACE_LEVEL_OPTIONS,
  SPACE_TYPE_MAP,
  SPACE_TYPE_OPTIONS,
} from '@/constants/space'
import { formatSize } from '@/utils'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '空间名称',
    dataIndex: 'spaceName',
  },
  {
    title: '空间级别',
    dataIndex: 'spaceLevel',
  },
  {
    title: '空间类型',
    dataIndex: 'spaceType',
  },
  {
    title: '使用情况',
    dataIndex: 'spaceUseInfo',
  },
  {
    title: '用户 id',
    dataIndex: 'userId',
    width: 80,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '编辑时间',
    dataIndex: 'editTime',
  },
  {
    title: '操作',
    key: 'action',
    width: 80,
  },
]

//定义数据
const dataList = ref<API.Space[]>([])
const total = ref<number>(0)

const searchParams = reactive<API.SpaceQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})
const paginaction = computed(() => {
  return {
    current: searchParams.current,
    pageSize: searchParams.pageSize,
    total: total.value,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total: number) => `共${total}条`,
    pageSizeOptions: ['10', '20', '30', '40'],
    onShowSizeChange: (current: number, pageSize: number) => {
      searchParams.current = current
      searchParams.pageSize = pageSize
      fetchData()
    },
    onChange: (current: number) => {
      searchParams.current = current
      fetchData()
    },
  }
})
const doSearch = () => {
  searchParams.current = 1
  fetchData()
}

const fetchData = async () => {
  const res = await listSpaceByPageUsingPost({
    ...searchParams,
  })
  if (res.data.code === 200 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = Number(res.data.data.total) ?? 0
  } else {
    message.error(res.data.message)
  }
}
//页面加载时请求数据
onMounted(() => {
  fetchData()
})

const doDelete = (id: number) => {
  Modal.confirm({
    title: '删除确认',
    content: '确定删除该空间吗？',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      const res = await deleteSpaceUsingPost({ id })
      if (res.data.code === 200) {
        message.success('删除成功')
        fetchData()
      } else {
        message.error(res.data.message)
      }
    },
  })
}
</script>
