<template>
  <div id="userManagePage">
    <!-- 搜索框 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" allow-clear />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="输入用户名" allow-clear />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <div style="margin-bottom: 16px"></div>
    <!-- 表格 -->
    <a-table :columns="columns" :data-source="dataList" :pagination="paginaction">
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-image :src="record.userAvatar" width="50px" />
        </template>
        <template v-if="column.dataIndex === 'userRole'">
          <!-- 修改颜色 -->
          <a-tag :color="record.userRole === 'admin' ? 'purple' : 'blue'">
            {{ record.userRole === 'admin' ? '管理员' : '普通用户' }}
          </a-tag>
        </template>
        <template v-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-button danger @click="doDelete(record.id)">删除</a-button>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { deleteUserUsingPost, listUserVoByPageUsingPost } from '@/api/userController'
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]
//定义数据
const dataList = ref<API.UserVO[]>([])
const total = ref<number>(0)

const searchParams = reactive<API.UserQueryRequest>({
  current: 1,
  pageSize: 10,
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
  const res = await listUserVoByPageUsingPost({
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
    content: '确定删除该用户吗？',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      const res = await deleteUserUsingPost({ id })
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
