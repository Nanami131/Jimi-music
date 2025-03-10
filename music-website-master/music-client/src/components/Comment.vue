<template>
  <div class="comment">
    <h2 class="comment-title">
      <span>评论</span>
      <span class="comment-desc">共 {{ total }} 条评论</span>
    </h2>
    <el-input class="comment-input" type="textarea" placeholder="期待您的精彩评论..." :rows="2" v-model="textarea" />
    <el-button class="sub-btn" type="primary" @click="submitComment()">发表评论</el-button>
  </div>
  <ul class="popular">
    <li v-for="(item, index) in commentList" :key="index">
      <el-image class="popular-img" fit="contain" :src="attachImageUrl(item.avator)" />
      <div class="popular-msg">
        <ul>
          <li class="name">{{ item.username }}</li>
          <li class="time">{{ formatDate(item.createTime) }}</li>
          <li class="content">{{ item.content }}</li>
        </ul>
      </div>
      <div ref="up" class="comment-ctr" @click="setSupport(item.id, item.up, userId)">
        <div><yin-icon :icon="iconList.Support"></yin-icon> {{ item.up }}</div>
        <el-icon v-if="item.userId === userId" @click="deleteComment(item.id, index)"><delete /></el-icon>
      </div>
    </li>
  </ul>
  <!-- 分页组件 -->
  <el-pagination
      class="pagination"
      background
      layout="prev, pager, next, total"
      :total="total"
      :page-size="pageSize"
      :current-page="currentPage"
      @current-change="handlePageChange"
  />
</template>

<script lang="ts" setup>
import { defineProps, getCurrentInstance, ref, toRefs, computed, watch, reactive, onMounted } from "vue";
import { useStore } from "vuex";
import { Delete } from "@element-plus/icons-vue";

import YinIcon from "@/components/layouts/YinIcon.vue";
import mixin from "@/mixins/mixin";
import { HttpManager } from "@/api";
import { Icon } from "@/enums";
import { formatDate } from "@/utils";

const { proxy } = getCurrentInstance();
const store = useStore();
const { checkStatus } = mixin();

const props = defineProps({
  playId: Number || String, // 歌曲ID 或 歌单ID
  type: Number, // 歌单 1 / 歌曲 0
});

const { playId, type } = toRefs(props);
const textarea = ref(""); // 存放输入内容
const commentList = ref([]); // 当前页评论内容
const total = ref(0); // 总评论数
const currentPage = ref(1); // 当前页码
const pageSize = 20; // 每页大小，固定为20
const iconList = reactive({
  Support: Icon.Support,
});

const userId = computed(() => store.getters.userId);
const songId = computed(() => store.getters.songId);

watch(songId, () => {
  currentPage.value = 1; // 重置页码
  getComment(songId.value);
});

onMounted(() => {
  getComment(playId.value);
});

// 获取分页评论
async function getComment(id) {
  try {
    // 先获取评论数据
    const commentResult = await HttpManager.getAllComment(type.value, id, currentPage.value, pageSize);
    commentList.value = commentResult.data.records;
    total.value = commentResult.data.total;

    // 只有当评论列表非空时才获取点赞信息
    if (commentList.value.length > 0) {
      const commentIds = commentList.value.map(c => c.id);
      const supportResult = await HttpManager.getCommentSupports(commentIds);
      const supportMap = new Map(supportResult.data.map(s => [s.id, s.up]));
      commentList.value.forEach(c => c.up = supportMap.get(c.id) || c.up);
    }
  } catch (error) {
    console.error('[获取评论或点赞信息失败]===>', error);
  }
}

// 提交评论
async function submitComment() {
  if (!checkStatus()) return;

  let songListId = null;
  let songId = null;
  let nowType = null;
  if (type.value === 1) {
    nowType = 1;
    songListId = `${playId.value}`;
  } else if (type.value === 0) {
    nowType = 0;
    songId = `${playId.value}`;
  }

  const content = textarea.value;
  const result = (await HttpManager.setComment({ userId: userId.value, content, songId, songListId, nowType })) as ResponseBody;
  (proxy as any).$message({
    message: result.message,
    type: result.type,
  });

  if (result.success) {
    textarea.value = "";
    await getComment(playId.value); // 刷新当前页评论
  }
}

// 删除评论
async function deleteComment(id, index) {
  const result = (await HttpManager.deleteComment(id)) as ResponseBody;
  (proxy as any).$message({
    message: result.message,
    type: result.type,
  });

  if (result.success) {
    commentList.value.splice(index, 1);
    total.value -= 1; // 更新总数
    // 如果当前页为空，自动跳转到上一页
    if (commentList.value.length === 0 && currentPage.value > 1) {
      currentPage.value -= 1;
      await getComment(playId.value);
    }
  }
}

// 点赞
async function setSupport(id, up, userId) {
  if (!checkStatus()) return;

  let result = null;
  let operatorR = null;
  const commentId = id;
  const r = (await HttpManager.testAlreadySupport({ commentId, userId })) as ResponseBody;
  (proxy as any).$message({
    message: r.message,
    type: r.type,
  });

  if (r.data) {
    up -= 1;
    operatorR = (await HttpManager.deleteUserSupport({ commentId, userId })) as ResponseBody;
    result = (await HttpManager.setSupport({ id, up })) as ResponseBody;
  } else {
    up += 1;
    operatorR = (await HttpManager.insertUserSupport({ commentId, userId })) as ResponseBody;
    result = (await HttpManager.setSupport({ id, up })) as ResponseBody;
  }

  if (result.success && operatorR.success) {
    await getComment(playId.value); // 刷新当前页
  }
}

// 页码改变时触发
function handlePageChange(newPage: number) {
  currentPage.value = newPage;
  getComment(playId.value);
}

const attachImageUrl = HttpManager.attachImageUrl;
</script>

<style lang="scss" scoped>
@import "@/assets/css/var.scss";
@import "@/assets/css/global.scss";

/*评论*/
.comment {
  position: relative;
  margin-bottom: 60px;

  .comment-title {
    height: 50px;
    line-height: 50px;

    .comment-desc {
      font-size: 14px;
      font-weight: 400;
      color: $color-grey;
      margin-left: 10px;
    }
  }

  .comment-input {
    display: flex;
    margin-bottom: 20px;
  }

  .sub-btn {
    position: absolute;
    right: 0;
  }
}

/*热门评论*/
.popular {
  width: 100%;
  > li {
    border-bottom: solid 1px rgba(0, 0, 0, 0.1);
    padding: 15px 0;
    display: flex;
    .popular-img {
      width: 50px;
    }

    .popular-msg {
      padding: 0 20px;
      flex: 1;
      li {
        width: 100%;
      }
      .time {
        font-size: 0.6rem;
        color: rgba(0, 0, 0, 0.5);
      }
      .name {
        color: rgba(0, 0, 0, 0.5);
      }
      .content {
        font-size: 1rem;
      }
    }

    .comment-ctr {
      display: flex;
      align-items: center;
      width: 80px;
      font-size: 1rem;
      cursor: pointer;

      .el-icon {
        margin: 0 10px;
      }

      &:hover,
      :deep(.icon):hover {
        color: $color-grey;
      }
    }
  }
}

/* 分页样式 */
.pagination {
  margin-top: 20px;
  text-align: center;
}

.icon {
  @include icon(1em);
}
</style>