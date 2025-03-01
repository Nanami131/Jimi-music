<template>
  <!-- 轮播图 -->
  <el-carousel
      v-if="swiperList.length"
      class="swiper-container"
      type="card"
      height="20vw"
      :interval="5000"
      arrow="always"
      autoplay
      trigger="hover"
  >
    <el-carousel-item v-for="(item, index) in swiperList" :key="index">
      <img class="swiper-image" :src="HttpManager.attachImageUrl(item.pic)" />
    </el-carousel-item>
  </el-carousel>

  <!-- 热门歌单 -->
  <div class="section">
    <h2 class="section-title">热门歌单</h2>
    <play-list class="play-list-container" title="歌单" path="song-sheet-detail" :playList="songList"></play-list>
  </div>

  <!-- 热门歌手 -->
  <div class="section">
    <h2 class="section-title">热门歌手</h2>
    <play-list class="play-list-container" title="歌手" path="singer-detail" :playList="singerList"></play-list>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from "vue";

import PlayList from "@/components/PlayList.vue";
import {  NavName } from "@/enums";
import { HttpManager } from "@/api";
import mixin from "@/mixins/mixin";

const songList = ref([]); // 歌单列表
const singerList = ref([]); // 歌手列表
const swiperList = ref([]);// 轮播图 每次都在进行查询
const { changeIndex } = mixin();
try {

  HttpManager.getBannerList().then((res) => {
    swiperList.value = (res as ResponseBody).data.sort();
  });

  HttpManager.getSongList().then((res) => {
    songList.value = (res as ResponseBody).data.sort().slice(0, 10);
  });

  HttpManager.getAllSinger().then((res) => {
    singerList.value = (res as ResponseBody).data.sort().slice(0, 10);
  });

  onMounted(() => {
    changeIndex(NavName.Home);
  });
} catch (error) {
  console.error(error);
}
</script>

<style lang="scss" scoped>
@import "@/assets/css/var.scss";

/* 轮播图 */
.swiper-container {
  width: 90%;
  margin: 2rem auto;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);

  .swiper-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 10px;
  }

  :deep(.el-carousel__indicators--outside) {
    bottom: -10px;
    display: flex;
    justify-content: center;

    button {
      width: 12px;
      height: 12px;
      background-color: rgba(255, 255, 255, 0.8);
      border-radius: 50%;
      margin: 0 5px;
      transition: background-color 0.3s;

      &:hover {
        background-color: rgba(255, 255, 255, 1);
      }

      &.is-active {
        background-color: $color-blue-light;
      }
    }
  }
}

/* 热门歌单 & 热门歌手 */
.section {
  max-width: 1200px;
  margin: 2rem auto;

  .section-title {
    font-size: 1.8rem;
    font-weight: bold;
    color: $color-blue-deep;
    text-align: left;
    margin-bottom: 1rem;
  }

  .play-list-container {
    display: flex;
    flex-wrap: wrap;
    gap: 1.5rem;
    justify-content: space-between;

    @media screen and (max-width: 768px) {
      flex-direction: column;
      align-items: center;
    }
  }
}
</style>