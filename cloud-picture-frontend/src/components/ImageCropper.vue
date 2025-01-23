<template>
  <a-modal
    class="image-cropper"
    v-model:visible="visiable"
    title="编辑图片"
    :footer="false"
    @cancel="closeModal"
  >
    <vue-cropper
      ref="cropperRef"
      :img="imageUrl"
      :autoCrop="true"
      :fixedBox="false"
      :canMove="false"
      :centerBox="true"
      :canMoveBox="true"
      :info="true"
      outputType="png"
    />
    <div style="margin-bottom: 16px" />
    <!-- 图片协同编辑操作 -->
    <div class="image-edit-actions" v-if="isTeamSpace">
      <a-space>
        <a-button v-if="editingUser" disabled> {{ editingUser?.userName }}正在编辑 </a-button>
        <a-button v-if="canEnterEdit" type="primary" ghost @click="enterEdit">进入编辑</a-button>
        <a-button v-if="canExitEdit" danger ghost @click="exitEdit">退出编辑</a-button>
      </a-space>
    </div>
    <div style="margin-bottom: 16px" />
    <!-- 图片操作 -->
    <div class="image-cropper-actions">
      <a-space>
        <a-button @click="rotateLeft" :disabled="!canEdit">向左旋转</a-button>
        <a-button @click="rotateRight" :disabled="!canEdit">向右旋转</a-button>
        <a-button @click="changeScale(1)" :disabled="!canEdit">放大</a-button>
        <a-button @click="changeScale(-1)" :disabled="!canEdit">缩小</a-button>
        <a-button type="primary" :loading="loading" @click="handleConfirm" :disabled="!canEdit">
          确认
        </a-button>
      </a-space>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed, onUnmounted, ref, watchEffect } from 'vue'
import { message } from 'ant-design-vue'
import { uploadPictureUsingPost } from '@/api/pictureController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import PictureEditWebSocket from '@/utils/PictureEditWebSocket'
import { PICTURE_EDIT_ACTION_ENUM, PICTURE_EDIT_MESSAGE_TYPE_ENUM } from '@/constants/picture'
import { SPACE_TYPE_ENUM } from '@/constants/space'

interface Props {
  imageUrl?: string
  picture?: API.PictureVO
  space?: API.SpaceVO
  spaceId?: number
  onSuccess?: (newPicture: API.PictureVO) => void
}

const props = defineProps<Props>()

// 是否为团队空间
const isTeamSpace = computed(() => props.space?.spaceType === SPACE_TYPE_ENUM.TEAM)

// 编辑器组件的引用
const cropperRef = ref()

// 向左旋转
const rotateLeft = () => {
  cropperRef.value.rotateLeft()
  editAction(PICTURE_EDIT_ACTION_ENUM.ROTATE_LEFT)
}

// 向右旋转
const rotateRight = () => {
  cropperRef.value.rotateRight()
  editAction(PICTURE_EDIT_ACTION_ENUM.ROTATE_RIGHT)
}

// 缩放
const changeScale = (num: number) => {
  cropperRef.value.changeScale(num)
  if (num > 0) {
    editAction(PICTURE_EDIT_ACTION_ENUM.ZOOM_IN)
  } else {
    editAction(PICTURE_EDIT_ACTION_ENUM.ZOOM_OUT)
  }
}

const loading = ref<boolean>(false)

// 确认裁剪
const handleConfirm = () => {
  cropperRef.value.getCropBlob((blob: Blob) => {
    const fileName = (props.picture?.name || 'image') + '.png'
    const file = new File([blob], fileName, { type: blob.type })
    // 上传图片
    handleUpload({ file })
  })
}

/**
 * 上传
 * @param file
 */
const handleUpload = async ({ file }: any) => {
  loading.value = true
  try {
    const params: API.PictureUploadRequest = props.picture ? { id: props.picture.id } : {}
    params.spaceId = props.spaceId
    const res = await uploadPictureUsingPost(params, {}, file)
    if (res.data.code === 200 && res.data.data) {
      message.success('图片上传成功')
      // 将上传成功的图片信息传递给父组件
      props.onSuccess?.(res.data.data)
      closeModal()
    } else {
      message.error('图片上传失败，' + res.data.message)
    }
  } catch (error) {
    message.error('图片上传失败')
  } finally {
    loading.value = false
  }
}

const visiable = ref<boolean>(false)

//打开弹窗
const showModal = () => {
  visiable.value = true
}
//关闭弹窗
const closeModal = () => {
  visiable.value = false
  webSocket?.disconnect()
  editingUser.value = undefined
}
//暴露函数给父组件调用
defineExpose({
  showModal,
})

// -------------实时编辑----------------
const loginUserStore = useLoginUserStore()
const loginUser = loginUserStore.loginUser
//正在编辑的用户
const editingUser = ref<API.UserVO>()
//当前用户是否可进入编辑状态
const canEnterEdit = computed(() => {
  return !editingUser.value
})
// 当前用户是否可退出编辑状态
const canExitEdit = computed(() => {
  console.log(editingUser.value?.id === loginUser.id)
  return editingUser.value?.id === loginUser.id
})
// 可以点击编辑按钮
const canEdit = computed(() => {
  // 不是团队空间，默认就可以编辑
  if (!isTeamSpace.value) return true
  //团队空间，只有编辑者可以编辑
  return editingUser.value?.id === loginUser?.id
})
// 编写webSocket
let webSocket: PictureEditWebSocket | null

// 初始化webSocket连接，绑定监听事件
const initWebSocket = () => {
  const pictureId = props.picture?.id
  if (!pictureId || !visiable.value) return
  // 防止之前的连接未释放
  if (webSocket) {
    webSocket.disconnect()
  }
  // 创建webSocket实例
  webSocket = new PictureEditWebSocket(pictureId)
  // 建立连接
  webSocket.connect()

  webSocket.on('open', (msg) => {
    console.log('连接成功')
    // 同步初始编辑的用户
    if (msg.user) {
      editingUser.value = msg.user
    }
  })

  // 监听一系列的事件
  webSocket.on(PICTURE_EDIT_MESSAGE_TYPE_ENUM.INFO, (msg) => {
    console.log('收到通知消息:', msg)
    message.info(msg.message)
  })
  webSocket.on(PICTURE_EDIT_MESSAGE_TYPE_ENUM.ERROR, (msg) => {
    console.log('收到错误通知:', msg)
    message.info(msg.message)
  })
  webSocket.on(PICTURE_EDIT_MESSAGE_TYPE_ENUM.ENTER_EDIT, (msg) => {
    console.log('收到进入编辑通知:', msg)
    message.info(msg.message)
    editingUser.value = msg.user
  })
  webSocket.on(PICTURE_EDIT_MESSAGE_TYPE_ENUM.EDIT_ACTION, (msg) => {
    console.log('收到编辑动作通知:', msg)
    message.info(msg.message)
    // 根据消息类型进行相应的操作
    switch (msg.editAction) {
      case PICTURE_EDIT_ACTION_ENUM.ROTATE_LEFT:
        rotateLeft()
        break
      case PICTURE_EDIT_ACTION_ENUM.ROTATE_RIGHT:
        rotateRight()
        break
      case PICTURE_EDIT_ACTION_ENUM.ZOOM_IN:
        changeScale(1)
        break
      case PICTURE_EDIT_ACTION_ENUM.ZOOM_OUT:
        changeScale(-1)
        break
      default:
        break
    }
  })

  webSocket.on(PICTURE_EDIT_MESSAGE_TYPE_ENUM.EXIT_EDIT, (msg) => {
    console.log('收到退出编辑通知:', msg)
    message.info(msg.message)
    editingUser.value = undefined
  })
}
//监听visible和属性变化，初始化websocket连接
watchEffect(() => {
  // 只有团队空间才需要初始化websocket
  if (isTeamSpace.value) {
    initWebSocket()
  }
})
// 组件销毁时断开websocket连接
onUnmounted(() => {
  webSocket?.disconnect()
  editingUser.value = undefined
})
// 进入编辑
const enterEdit = () => {
  if (webSocket) {
    webSocket.sendMessage({
      type: PICTURE_EDIT_MESSAGE_TYPE_ENUM.ENTER_EDIT,
    })
  }
}

// 退出编辑
const exitEdit = () => {
  if (webSocket) {
    webSocket.sendMessage({
      type: PICTURE_EDIT_MESSAGE_TYPE_ENUM.EXIT_EDIT,
    })
  }
}

// 编辑操作
const editAction = (action: string) => {
  if (webSocket) {
    // 发送编辑操作请求
    webSocket.sendMessage({
      type: PICTURE_EDIT_MESSAGE_TYPE_ENUM.EDIT_ACTION,
      editAction: action,
    })
  }
}
</script>

<style>
.image-cropper {
  text-align: center;
}

.image-cropper .vue-cropper {
  height: 400px;
}
</style>
