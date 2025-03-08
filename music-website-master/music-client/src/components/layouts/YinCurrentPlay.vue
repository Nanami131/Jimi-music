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
        <div class="lyric-header">
          <h2 class="title">歌词</h2>
          <div class="lang-select">
            <span class="lang-btn" :class="{ active: selectedLang === 'all' }" @click="selectLanguage('all')">全部</span>
            <span class="lang-btn" :class="{ active: selectedLang === 'ja' }" @click="selectLanguage('ja')">日语</span>
            <span class="lang-btn" :class="{ active: selectedLang === 'zh' }" @click="selectLanguage('zh')">中文</span>
          </div>
        </div>
        <div class="lyric-wrapper">
          <ul :style="{ top: lrcTop }" class="has-lyric" v-if="filteredLyricArr.length" ref="lyricList">
            <li v-for="(item, index) in filteredLyricArr" :key="index">
              {{ item.text || '♪' }}
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

    const lrcTop = ref("50px");
    const rawLyricArr = ref<{ time: number; text: string; lang?: string }[]>([]);
    const filteredLyricArr = ref<{ time: number; text: string }[]>([]);
    const lyricList = ref<HTMLElement | null>(null);
    const selectedLang = ref("all");

    const currentLyric = computed(() => {
      const song = currentPlayList.value[currentPlayIndex.value];
      return song ? song.lyric : "";
    });

    const parseLyric = (lyric: string) => {
      if (!lyric) {
        rawLyricArr.value = [];
        return;
      }
      const lines = lyric.split('\n');
      const timeReg = /\[(\d{2}):(\d{2})\.(\d{2,3})\]/;
      const langReg = /\[([a-z]{2})\]/g;
      rawLyricArr.value = lines
          .map((line) => {
            const timeMatch = line.match(timeReg);
            if (!timeMatch) return null;

            const minutes = parseInt(timeMatch[1]) * 60;
            const seconds = parseInt(timeMatch[2]);
            const milliseconds = parseInt(timeMatch[3].padEnd(3, '0')) / 1000;
            const time = minutes + seconds + milliseconds;

            let text = line.replace(timeReg, '').trim();
            let lang: string | undefined;
            const langMatches = [...text.matchAll(langReg)];
            if (langMatches.length > 0) {
              lang = langMatches[0][1];
              text = text.replace(langReg, '').trim();
            }

            return { time, text, lang };
          })
          .filter((item) => item !== null) as { time: number; text: string; lang?: string }[];
    };

    const filterLyric = () => {
      parseLyric(currentLyric.value); // 每次过滤前重新解析
      filteredLyricArr.value = rawLyricArr.value
          .filter((item) => !item.lang || item.lang === selectedLang.value || selectedLang.value === "all")
          .map((item) => ({ time: item.time, text: item.text }));
    };

    const selectLanguage = (lang: string) => {
      selectedLang.value = lang;
      filterLyric(); // 切换语言时重新解析和过滤
    };

    watch(curTime, () => {
      if (filteredLyricArr.value.length !== 0 && lyricList.value) {
        const lyricItems = lyricList.value.querySelectorAll("li") as NodeListOf<HTMLElement>;
        for (let i = 0; i < filteredLyricArr.value.length; i++) {
          if (curTime.value >= filteredLyricArr.value[i].time) {
            lyricItems.forEach((item) => {
              item.style.color = "#555";
              item.style.fontSize = "14px";
            });
            if (i >= 0) {
              lrcTop.value = -i * 40 + 50 + "px";
              lyricItems[i].style.color = "#ff6b81";
              lyricItems[i].style.fontSize = "18px";
            }
          }
        }
      }
    });

    watch(songId, () => {
      filterLyric(); // 歌曲切换时重新解析和过滤
    });

    filterLyric(); // 初始解析

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
      filteredLyricArr,
      lyricList,
      selectedLang,
      selectLanguage,
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
  width: 100%;
  position: fixed;
  left: 0;
  bottom: $play-bar-height;
  height: 300px;
  z-index: 101;
  background: linear-gradient(135deg, #f0f4f8 0%, #e4e9f0 100%); // 渐变背景
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  display: flex;
  border-radius: 12px 12px 0 0;
  font-family: 'Arial', sans-serif;
}

.play-list {
  width: 40%;
  height: 100%;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.95);
  border-right: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 3px 0 10px rgba(0, 0, 0, 0.08);

  .title {
    margin: 15px 0;
    padding-left: 20px;
    font-size: 20px;
    color: #2c3e50;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 1px;
  }

  .control {
    margin: 5px 0 15px;
    padding-left: 20px;
    color: #7f8c8d;
    font-size: 13px;
    font-style: italic;
  }

  .menus {
    width: 100%;
    height: calc(100% - 75px);
    cursor: pointer;
    overflow-y: auto;
    li {
      display: block;
      width: 100%;
      height: 40px;
      line-height: 40px;
      padding-left: 20px;
      color: #34495e;
      transition: all 0.3s ease;
      &:hover {
        background: rgba(240, 245, 250, 0.8);
        color: #2980b9;
      }
    }
  }

  .is-play {
    color: #2980b9;
    font-weight: bold;
    background: linear-gradient(90deg, #e8f1ff, #f5faff);
    border-left: 4px solid #3498db;
    box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.05);
  }
}

.lyric-panel {
  width: 60%;
  height: 100%;
  overflow: hidden;
  position: relative;
  background: rgba(255, 255, 255, 0.1); // 微透明效果
  padding: 0 25px;
}

.lyric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  .title {
    font-size: 20px;
    color: #2c3e50;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 1px;
  }
  .lang-select {
    display: flex;
    gap: 10px;
    .lang-btn {
      display: inline-block;
      width: 36px;
      height: 36px;
      line-height: 36px;
      text-align: center;
      border-radius: 50%;
      background: #dfe6e9;
      color: #636e72;
      font-size: 14px;
      cursor: pointer;
      transition: all 0.3s ease;
      &:hover {
        background: #b2bec3;
        color: #fff;
      }
      &.active {
        background: #ff6b81;
        color: #fff;
        box-shadow: 0 0 8px rgba(255, 107, 129, 0.5);
        transform: scale(1.1);
      }
    }
  }
}

.lyric-wrapper {
  position: relative;
  height: calc(100% - 60px);
  display: flex;
  justify-content: center;
  align-items: center;
}

.has-lyric {
  position: absolute;
  transition: all 1s;
  width: 100%;
  li {
    width: 100%;
    height: 40px;
    text-align: center;
    font-size: 14px;
    line-height: 40px;
    color: #555;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  }
}

.no-lyric {
  text-align: center;
  span {
    font-size: 18px;
    color: #95a5a6;
    font-style: italic;
  }
}
</style>