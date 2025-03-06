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
    <el-carousel-item v-for="(item, index) in swiperList" :key="index" @click="goToSongSheetDetail(item)">
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
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import PlayList from "@/components/PlayList.vue";
import { NavName } from "@/enums";
import { HttpManager } from "@/api";
import mixin from "@/mixins/mixin";

// 类型定义
interface ResponseBody<T> {
  code: number;
  message: string | null;
  type?: string;
  success?: boolean;
  data: T;
}

interface SongDetails {
  id: number;
  title: string;
  pic: string;
  introduction?: string;
  [key: string]: any;
}

// 响应式变量
const songList = ref<any[]>([]);
const singerList = ref<any[]>([]);
const swiperList = ref<any[]>([]);

// 获取路由和状态管理实例
const router = useRouter();
const store = useStore();
const { changeIndex } = mixin();

// 点击轮播图跳转到歌单详情
async function goToSongSheetDetail(banner: { id: number; pic: string; url: string }) {
  try {
    // 从 banner.url 提取歌单 ID
    const songListId = extractSongListId(banner.url);
    if (!songListId) {
      console.error("Invalid song list ID from banner URL:", banner.url);
      return;
    }

    // 获取歌单详情，显式声明类型为数组
    const result = await HttpManager.getSongListDetail(songListId) as ResponseBody<SongDetails[]>;
    const songDetails = result.data[0]; // 取数组第一个元素

    if (!songDetails || !songDetails.id) {
      console.error("Invalid song details for songListId:", songListId, "Response:", result);
      return;
    }

    // 设置 songDetails 到 Vuex
    store.commit("setSongDetails", songDetails);
    console.log("songDetails set to Vuex:", songDetails);

    // 验证路由跳转
    const targetPath = `/song-sheet-detail/${songListId}`;
    console.log("Attempting to navigate to:", targetPath);
    await router.push(targetPath);

    // 检查跳转结果
    const currentPath = router.currentRoute.value.path;
    console.log("Current route after push:", currentPath);
    if (currentPath !== targetPath) {
      console.error("Navigation failed, current path does not match target:", currentPath);
    }
  } catch (error) {
    console.error("Error in goToSongSheetDetail:", error);
  }
}

// 从 Banner.url 中提取歌单 ID
function extractSongListId(url: string): number | null {
  if (!url) {
    console.error("Banner URL is null or undefined");
    return null;
  }
  const match = url.match(/\/song-sheet-detail\/(\d+)$/);
  if (!match) {
    console.error("Failed to extract song list ID from URL:", url);
    return null;
  }
  return parseInt(match[1], 10);
}

// 初始化数据
async function fetchData() {
  try {
    const bannerRes = await HttpManager.getBannerList() as ResponseBody<any[]>;
    swiperList.value = bannerRes.data.sort();

    const songRes = await HttpManager.getSongList() as ResponseBody<any[]>;
    songList.value = songRes.data.sort().slice(0, 10);

    const singerRes = await HttpManager.getAllSinger() as ResponseBody<any[]>;
    singerList.value = singerRes.data.sort().slice(0, 10);
  } catch (error) {
    console.error("Failed to fetch initial data:", error);
  }
}

// 页面挂载时加载数据和设置导航
onMounted(() => {
  fetchData();
  changeIndex(NavName.Home);
});
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
    cursor: pointer;
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