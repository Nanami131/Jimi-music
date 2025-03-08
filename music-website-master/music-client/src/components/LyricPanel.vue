<template>
  <div class="lyric-panel">
    <div class="lyric-header">
      <h2 class="title">歌词</h2>
      <div class="controls">
        <div class="lang-select">
          <span class="lang-btn" :class="{ active: selectedLang === 'all' }" @click="selectLanguage('all')">全部</span>
          <span class="lang-btn" :class="{ active: selectedLang === 'ja' }" @click="selectLanguage('ja')">日语</span>
          <span class="lang-btn" :class="{ active: selectedLang === 'zh' }" @click="selectLanguage('zh')">中文</span>
        </div>
        <div class="color-select">
          <span class="color-btn pink" :class="{ active: selectedColor === '#ff6b81' }" @click="selectColor('#ff6b81', 'rgba(255, 107, 129, 0.2)')"></span>
          <span class="color-btn blue" :class="{ active: selectedColor === '#3498db' }" @click="selectColor('#3498db', 'rgba(52, 152, 219, 0.2)')"></span>
          <span class="color-btn purple" :class="{ active: selectedColor === '#9b59b6' }" @click="selectColor('#9b59b6', 'rgba(155, 89, 182, 0.2)')"></span>
          <span class="color-btn green" :class="{ active: selectedColor === '#2ecc71' }" @click="selectColor('#2ecc71', 'rgba(46, 204, 113, 0.2)')"></span>
        </div>
      </div>
    </div>
    <div class="lyric-wrapper" ref="lyricWrapper">
      <ul class="has-lyric" v-if="hasLyrics">
        <li v-for="(item, index) in filteredLyricArr" :key="index" :class="{ active: index === activeIndex }">
          {{ item.text || '♪' }}
        </li>
      </ul>
      <div v-else class="no-lyric">
        <span>暂无歌词</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, watch, computed, onMounted, nextTick } from "vue";
import { useStore } from "vuex";

export default defineComponent({
  setup() {
    const store = useStore();

    const rawLyricArr = ref<{ time: number; text: string; lang?: string }[]>([]);
    const filteredLyricArr = ref<{ time: number; text: string }[]>([]);
    const activeIndex = ref(-1);
    const selectedLang = ref("all");
    const selectedColor = ref("#ff6b81");
    const lyricWrapper = ref<HTMLElement | null>(null); // 添加对 lyric-wrapper 的引用

    const curTime = computed(() => store.getters.curTime || 0);
    const currentPlayIndex = computed(() => store.getters.currentPlayIndex ?? -1);
    const currentPlayList = computed(() => store.getters.currentPlayList || []);
    const songId = computed(() => store.getters.songId);

    const currentLyric = computed(() => {
      const index = currentPlayIndex.value;
      const list = currentPlayList.value;
      const song = index >= 0 && list[index] ? list[index] : null;
      return song ? song.lyric : "";
    });

    const hasLyrics = computed(() => filteredLyricArr.value.length > 0);

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
      parseLyric(currentLyric.value);
      filteredLyricArr.value = rawLyricArr.value
          .filter((item) => !item.lang || item.lang === selectedLang.value || selectedLang.value === "all")
          .map((item) => ({ time: item.time, text: item.text }));
      updateHighlight();
    };

    const selectLanguage = (lang: string) => {
      selectedLang.value = lang;
      filterLyric();
    };

    const selectColor = (color: string, bgColor: string) => {
      selectedColor.value = color;
      document.documentElement.style.setProperty('--highlight-color', color);
      document.documentElement.style.setProperty('--highlight-bg', bgColor);
      updateHighlight();
    };

    const updateHighlight = () => {
      if (filteredLyricArr.value.length === 0) {
        activeIndex.value = -1;
        return;
      }

      const currentTime = curTime.value;
      let newIndex = -1;

      for (let i = 0; i < filteredLyricArr.value.length; i++) {
        if (currentTime >= filteredLyricArr.value[i].time) {
          newIndex = i;
        } else {
          break;
        }
      }

      if (newIndex !== activeIndex.value) {
        activeIndex.value = newIndex;
        scrollToActiveLyric();
      }
    };

    const scrollToActiveLyric = () => {
      nextTick(() => {
        if (lyricWrapper.value && activeIndex.value >= 0) {
          const activeElement = lyricWrapper.value.querySelector('.active') as HTMLElement;
          if (activeElement) {
            const wrapperHeight = lyricWrapper.value.clientHeight;
            const activeHeight = activeElement.offsetHeight;
            const activeTop = activeElement.offsetTop;
            const scrollPosition = activeTop - (wrapperHeight / 2) + (activeHeight / 2); // 将高亮歌词居中
            lyricWrapper.value.scrollTo({
              top: scrollPosition,
              behavior: 'smooth' // 平滑滚动
            });
          }
        }
      });
    };

    watch(curTime, () => {
      updateHighlight();
    });

    watch(songId, () => {
      filterLyric();
    });

    watch(currentPlayList, () => {
      filterLyric();
    });

    onMounted(() => {
      filterLyric();
    });

    return {
      filteredLyricArr,
      activeIndex,
      selectedLang,
      selectedColor,
      selectLanguage,
      selectColor,
      hasLyrics,
      lyricWrapper, // 返回 ref 以绑定到模板
    };
  },
});
</script>

<style lang="scss" scoped>
@import "@/assets/css/var.scss";

.lyric-panel {
  width: 100%;
  height: 100%;
  overflow: hidden;
  position: relative;
  background: rgba(255, 255, 255, 0.1);
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
  .controls {
    display: flex;
    gap: 20px;
    .lang-select,
    .color-select {
      display: flex;
      gap: 10px;
    }
    .lang-btn {
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
        background: var(--highlight-color);
        color: #fff;
        box-shadow: 0 0 8px rgba(0, 0, 0, 0.2);
        transform: scale(1.1);
      }
    }
    .color-btn {
      width: 24px;
      height: 24px;
      border-radius: 50%;
      cursor: pointer;
      transition: all 0.3s ease;
      border: 2px solid #fff;
      &.pink { background: #ff6b81; }
      &.blue { background: #3498db; }
      &.purple { background: #9b59b6; }
      &.green { background: #2ecc71; }
      &:hover {
        transform: scale(1.2);
      }
      &.active {
        box-shadow: 0 0 6px rgba(0, 0, 0, 0.3);
        transform: scale(1.2);
      }
    }
  }
}

.lyric-wrapper {
  position: relative;
  height: calc(100% - 60px);
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.has-lyric {
  width: 100%;
  padding: 20px 0; /* 增加上下内边距以避免歌词贴边 */
  li {
    width: 100%;
    height: 40px;
    text-align: center;
    font-size: 14px;
    line-height: 40px;
    color: #555;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
    &.active {
      color: var(--highlight-color);
      font-size: 18px;
      background: var(--highlight-bg);
    }
  }
}

.no-lyric {
  text-align: center;
  padding-top: 20px;
  span {
    font-size: 18px;
    color: #95a5a6;
    font-style: italic;
  }
}
</style>