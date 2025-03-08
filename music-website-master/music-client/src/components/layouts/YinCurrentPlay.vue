<template>
  <transition name="aside-fade">
    <div class="yin-current-play" v-if="showAside">
      <div class="play-list">
        <h2 class="title">当前播放</h2>
        <div class="control">共 {{ (currentPlayList && currentPlayList.length) || 0 }} 首</div>
        <ul class="menus">
          <li
              v-for="(item, index) in currentPlayList"
              :class="{ 'is-play': songId === item.id }"
              :key="index"
              @click="playMusic({
              id: item.id,
              url: item.url,
              pic: item.pic,
              index: index,
              name: item.name,
              lyric: item.lyric,
              currentSongList: currentPlayList,
            })">
            {{ getSongTitle(item.name) }}
          </li>
        </ul>
      </div>
      <div class="lyric-panel">
        <div class="lyric-wrapper">
          <ul :style="{ top: lrcTop }" class="has-lyric" v-if="lyricArr.length" ref="lyricList">
            <li v-for="(item, index) in lyricArr" :key="index">
              {{ item[1] || '♪' }}
            </li>
          </ul>
          <div v-else class="no-lyric">
            <span>暂无歌词</span>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, computed, onMounted, onUnmounted, ref, watch } from "vue";
import { useStore } from "vuex";
import mixin from "@/mixins/mixin";
import { parseLyric } from "@/utils"; // 复用 Lyric.vue 的解析函数

export default defineComponent({
  setup() {
    const { proxy } = getCurrentInstance();
    const store = useStore();
    const { getSongTitle, playMusic } = mixin();

    const songId = computed(() => store.getters.songId);
    const currentPlayList = computed(() => store.getters.currentPlayList);
    const showAside = computed(() => store.getters.showAside);
    const curTime = computed(() => store.getters.curTime || 0);
    const currentPlayIndex = computed(() => store.getters.currentPlayIndex);

    // 歌词相关
    const lrcTop = ref("50px"); // 初始 top 值，与 Lyric.vue 调整为适应新布局
    const lyricArr = ref<any[]>([]); // 歌词数组，格式与 Lyric.vue 一致 [time, text]
    const lyricList = ref<HTMLElement | null>(null); // 用于 DOM 操作

    // 获取当前歌词
    const currentLyric = computed(() => {
      const song = currentPlayList.value[currentPlayIndex.value];
      return song ? song.lyric : "";
    });

    // 解析歌词
    watch(songId, () => {
      lyricArr.value = parseLyric(currentLyric.value);
    });

    // 同步歌词高亮和滚动
    watch(curTime, () => {
      if (lyricArr.value.length !== 0 && lyricList.value) {
        const lyricItems = lyricList.value.querySelectorAll("li") as NodeListOf<HTMLElement>;
        for (let i = 0; i < lyricArr.value.length; i++) {
          if (curTime.value >= lyricArr.value[i][0]) {
            // 重置所有歌词样式
            lyricItems.forEach((item) => {
              item.style.color = "#000";
              item.style.fontSize = "14px";
            });
            // 高亮当前行并调整位置
            if (i >= 0) {
              lrcTop.value = -i * 40 + 50 + "px"; // 每行 40px，偏移 50px 居中
              lyricItems[i].style.color = "#95d2f6";
              lyricItems[i].style.fontSize = "18px";
            }
          }
        }
      }
    });

    // 初始化歌词
    lyricArr.value = currentLyric.value ? parseLyric(currentLyric.value) : [];

    // 点击外部关闭
    const handleClickOutside = (e: MouseEvent) => {
      const aside = (proxy?.$el as HTMLElement)?.querySelector('.yin-current-play');
      const playBar = document.querySelector('.play-bar');
      if (aside && playBar && !aside.contains(e.target as Node) && !playBar.contains(e.target as Node)) {
        proxy.$store.commit('setShowAside', false);
      }
    };

    onMounted(() => {
      document.addEventListener('click', handleClickOutside, true);
    });

    onUnmounted(() => {
      document.removeEventListener('click', handleClickOutside, true);
    });

    return {
      songId,
      currentPlayList,
      showAside,
      lrcTop,
      lyricArr,
      lyricList,
      getSongTitle,
      playMusic,
    };
  },
});
</script>

<style lang="scss" scoped>
@import "@/assets/css/var.scss";
@import "@/assets/css/global.scss";

.aside-fade-enter-active {
  transition: all 0.3s ease;
}

.aside-fade-leave-active {
  transition: all 0.2s ease;
}

.aside-fade-enter,
.aside-fade-leave-to {
  transform: translateY(100%);
  opacity: 0;
}

.yin-current-play {
  font-size: 14px;
  width: 100%;
  position: fixed;
  left: 0;
  bottom: $play-bar-height;
  height: 300px;
  z-index: 101;
  background-color: $color-white;
  box-shadow: 0 -1px 10px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  display: flex;
}

.play-list {
  width: 40%;
  height: 100%;
  overflow: hidden;
  border-right: 1px solid $color-light-grey;

  .title,
  .control,
  .menus li {
    padding-left: 20px;
    box-sizing: border-box;
  }

  .title {
    margin: 10px 0;
  }

  .control {
    margin: 3px 0;
    color: $color-grey;
  }

  .menus {
    width: 100%;
    height: calc(100% - 60px);
    cursor: pointer;
    overflow-y: auto;
    white-space: nowrap;
    li {
      display: block;
      width: 100%;
      height: 40px;
      line-height: 40px;
      &:hover {
        background-color: $color-light-grey;
      }
    }
  }

  .is-play {
    color: $color-black;
    font-weight: bold;
  }
}

.lyric-panel {
  width: 60%;
  height: 100%;
  overflow: hidden;
  position: relative;
  background-color: $color-light-grey; // 与 Lyric.vue 一致
}

.lyric-wrapper {
  position: relative;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.has-lyric {
  position: absolute;
  transition: all 1s; // 与 Lyric.vue 一致
  width: 100%;
  li {
    width: 100%;
    height: 40px;
    text-align: center;
    font-size: 14px;
    line-height: 40px;
  }
}

.no-lyric {
  text-align: center;
  span {
    font-size: 18px;
  }
}
</style>