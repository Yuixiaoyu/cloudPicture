<template>
  <div id="addPictureBatchPage">
    <h2 style="margin-bottom: 16px">批量添加图片</h2>

    <!-- 图片信息表单 -->
    <a-form name="formData" layout="vertical" :model="formData" @finish="handleSubmit">
      <a-form-item label="关键词" name="searchText">
        <a-input v-model:value="formData.searchText" placeholder="输入关键词" allow-cler />
      </a-form-item>
      <a-form-item label="抓取数量" name="count">
        <a-input-number
          v-model:value="formData.count"
          placeholder="输入简介"
          style="min-width: 180px"
          :min="1"
          :max="30"
          allow-cler
        />
      </a-form-item>
      <a-form-item label="名称前缀" name="namePrefix">
        <a-input
          v-model:value="formData.namePrefix"
          placeholder="输入名称前缀,将自动添加序号"
          allow-cler
        />
      </a-form-item>
      <a-form-item label="分类" name="category">
        <a-auto-complete
          v-model:value="formData.category"
          :options="categoryOptions"
          placeholder="输入分类"
          allow-cler
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" :loading="loading" html-type="submit" style="width: 100%"
          >执行任务</a-button
        >
      </a-form-item>
    </a-form>
  </div>
</template>
<script setup lang="ts">
import {
  editPictureUsingPost,
  getPictureVoByIdUsingGet,
  listPictureTagCategoryUsingGet,
  uploadPictureByBatchUsingPost,
} from '@/api/pictureController'
import PictureUpload from '@/components/PictureUpload.vue'
import UrlPictureUpload from '@/components/UrlPictureUpload.vue'
import { message } from 'ant-design-vue'
import { reactive, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const formData = reactive<API.PictureUploadByBatchRequest>({
  count: 10,
})
const loading = ref<boolean>(false)

const router = useRouter()
/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: API.PictureUploadByBatchRequest) => {
  loading.value = true
  const res = await uploadPictureByBatchUsingPost({
    ...formData,
  })
  if (res.data.code === 200 && res.data.data) {
    message.success('创建成功,共' + res.data.data + '张图片')
    //跳转到主页
    router.push({
      path: `/`,
    })
  } else {
    message.error('创建失败' + res.data.message)
  }
  loading.value = false
}

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

const route = useRoute()
//获取老数据
const getOldPicture = async () => {
  const id = route.query?.id
  if (id) {
    const res = await getPictureVoByIdUsingGet({ id })
    if (res.data.code === 200 && res.data.data) {
      const data = res.data.data
      picture.value = data
      formData.name = data.name
      formData.introduction = data.introduction
      formData.category = data.category
      formData.tags = data.tags
    } else {
      message.error('获取图片失败' + res.data.message)
    }
  }
}

onMounted(() => {
  getOldPicture()
})
</script>

<style scoped>
#addPictureBatchPage {
  max-width: 520px;
  margin: 0 auto;
}
</style>
