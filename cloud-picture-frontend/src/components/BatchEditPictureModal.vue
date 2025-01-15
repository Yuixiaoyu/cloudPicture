<template>
  <div class="batch-edit-picture-modal">
    <a-modal v-model:visible="visiable" title="批量编辑图片" :footer="false" @cancel="closeModal">
      <a-typography-paragraph type="secondary">* 只对当前页面中的图片生效</a-typography-paragraph>
      <!-- 批量编辑图片表单 -->
      <a-form name="formData" layout="vertical" :model="formData" @finish="handleSubmit">
        <a-form-item label="分类" name="category">
          <a-auto-complete
            v-model:value="formData.category"
            :options="categoryOptions"
            placeholder="输入分类"
            allow-cler
          />
        </a-form-item>
        <a-form-item label="标签" name="tags">
          <a-select
            v-model:value="formData.tags"
            mode="tags"
            placeholder="输入标签"
            :options="tagsOptions"
            allow-cler
          ></a-select>
        </a-form-item>
        <a-form-item label="命名规则" name="nameRule">
          <a-input
            v-model:value="formData.nameRule"
            placeholder="请输入命名规则，输入{序号}可自动填充"
            allow-cler
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" style="width: 100%">提交</a-button>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import {
  editPictureByBatchUsingPost,
  listPictureTagCategoryUsingGet,
} from '@/api/pictureController'
import { message } from 'ant-design-vue'
import { ref, onMounted, reactive } from 'vue'
const open = ref<boolean>(false)

interface Props {
  pictureList: API.PictureVO[]
  spaceId: number
  onSuccess: () => void
}

const props = withDefaults(defineProps<Props>(), {})

const visiable = ref<boolean>(false)

const formData = reactive<API.PictureEditByBatchRequest>({
  category: '',
  tags: [],
  nameRule: '',
})

//打开弹窗
const showModal = () => {
  visiable.value = true
}
//关闭弹窗
const closeModal = () => {
  visiable.value = false
}
//暴露函数给父组件调用
defineExpose({
  showModal,
})

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  if (props.pictureList.length <= 0) {
    return
  }
  const res = await editPictureByBatchUsingPost({
    pictureIdList: props.pictureList.map((picture) => picture.id),
    spaceId: props.spaceId,
    ...values,
  })
  if (res.data.code === 200 && res.data.data) {
    message.success('操作成功')
    closeModal()
    props.onSuccess?.()
  } else {
    message.error('操作失败' + res.data.message)
  }
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
</script>
