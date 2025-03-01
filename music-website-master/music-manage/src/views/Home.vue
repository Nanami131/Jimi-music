<template>
  <yin-header></yin-header>
  <yin-aside></yin-aside>
  <div class="content-box" :class="{ 'content-collapse': collapse }">
    <router-view></router-view>
  </div>
  <yin-audio></yin-audio>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import YinHeader from "@/components/layouts/YinHeader.vue";
import YinAudio from "@/components/layouts/YinAudio.vue";
import YinAside from "@/components/layouts/YinAside.vue";
import emitter from "@/utils/emitter";

const collapse = ref(false);
emitter.on("collapse", (msg) => {
  collapse.value = msg as boolean;
});
</script>

<style scoped>
/* 优化整体布局 */
.content-box {
  position: absolute;
  left: 150px;
  right: 0;
  top: 60px;
  bottom: 0;
  overflow-y: auto;
  transition: left 0.3s ease-in-out, background-color 0.3s ease-in-out;
  padding: 20px;
  background-color: #f3f4f6; /* 更改背景色为更柔和的灰白色 */
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); /* 外部阴影，增强层次感 */
  border-radius: 12px; /* 增加圆角 */
  font-family: 'Roboto', Arial, sans-serif; /* 使用现代字体 */
  font-size: 16px; /* 设置默认字体大小 */
  color: var(--text-color);
}

/* 收缩状态调整 */
.content-collapse {
  left: 65px;
}

/* 全局字体与颜色调整 */
:root {
  --primary-color: #007bff; /* 更新为蓝色主色调 */
  --secondary-color: #ffffff; /* 次要色调 */
  --text-color: #2c3e50; /* 深色文本颜色 */
}

/* 添加过渡效果 */
.content-box, .content-collapse {
  transition: all 0.3s ease-in-out;
}

/* 提升交互体验的细节 */
.content-box::-webkit-scrollbar {
  width: 10px;
}

.content-box::-webkit-scrollbar-thumb {
  background-color: var(--primary-color);
  border-radius: 5px;
}

.content-box::-webkit-scrollbar-track {
  background-color: #f0f0f0;
  border-radius: 5px;
}

/* 添加内容间距和边框样式 */
.content-box {
  padding: 24px;
  border: 1px solid #e0e0e0; /* 添加边框 */
}

/* 提升导航栏的层次感 */
yin-header {
  background-color: var(--primary-color);
  color: var(--secondary-color);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

yin-aside {
  background-color: #f8f9fa;
  border-right: 1px solid #e0e0e0;
}
</style>
