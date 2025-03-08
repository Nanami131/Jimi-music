<!-- 文件: src/views/Lyric.vue -->
<template>
  <div class="song-container">
    <el-image class="song-pic" fit="contain" :src="attachImageUrl(songPic)" />
    <ul class="song-info">
      <li>歌手：{{ singerName }}</li>
      <li>歌曲：{{ songTitle }}</li>
    </ul>
  </div>
  <div class="container">
    <div class="lyric-container">
      <div class="lyric-header">
        <h2 class="title">歌词</h2>
        <div class="lang-select">
          <span class="lang-btn" :class="{ active: selectedLang === 'all' }" @click="selectLanguage('all')">全部</span>
          <span class="lang-btn" :class="{ active: selectedLang === 'ja' }" @click="selectLanguage('ja')">日语</span>
          <span class="lang-btn" :class="{ active: selectedLang === 'zh' }" @click="selectLanguage('zh')">中文</span>
        </div>
      </div>
      <div class="song-lyric">
        <ul :style="{ top: lrcTop }" class="has-lyric" v-if="filteredLyricArr.length" ref="lyricList">
          <li v-for="(item, index) in filteredLyricArr" :key="index">
            {{ item.text || '♪' }}
          </li>
        </ul>
        <div v-else class="no-lyric">
          <span>暂无歌词</span>
        </div>
      </div>
      <comment :playId="songId" :type="0"></comment>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, ref, watch } from "vue";
import { useStore } from "vuex";
import Comment from "@/components/Comment.vue";
import { HttpManager } from "@/api";

export default defineComponent({
  components: {
    Comment,
  },
  setup() {
    const store = useStore();

    const lrcTop = ref("80px");
    const rawLyricArr = ref<{ time: number; text: string; lang?: string }[]>([]);
    const filteredLyricArr = ref<{ time: number; text: string }[]>([]);
    const lyricList = ref<HTMLElement | null>(null);
    const selectedLang = ref("all");

    const songId = computed(() => store.getters.songId);
    const currentPlayList = computed(() => store.getters.currentPlayList);
    const currentPlayIndex = computed(() => store.getters.currentPlayIndex);
    const curTime = computed(() => store.getters.curTime);
    const songTitle = computed(() => store.getters.songTitle);
    const singerName = computed(() => store.getters.singerName);
    const songPic = computed(() => store.getters.songPic);

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
      parseLyric(currentLyric.value);
      filteredLyricArr.value = rawLyricArr.value
          .filter((item) => !item.lang || item.lang === selectedLang.value || selectedLang.value === "all")
          .map((item) => ({ time: item.time, text: item.text }));
    };

    const selectLanguage = (lang: string) => {
      selectedLang.value = lang;
      filterLyric();
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
              lrcTop.value = -i * 40 + 80 + "px"; // 调整为 Lyric.vue 的初始偏移
              lyricItems[i].style.color = "#ff6b81"; // 与 YinCurrentPlay.vue 一致
              lyricItems[i].style.fontSize = "18px";
            }
          }
        }
      }
    });

    watch(songId, () => {
      filterLyric();
    });

    filterLyric();

    return {
      songPic,
      singerName,
      songTitle,
      lrcTop,
      filteredLyricArr,
      lyricList,
      selectedLang,
      selectLanguage,
      songId,
      attachImageUrl: HttpManager.attachImageUrl,
    };
  },
});
</script>

<style lang="scss" scoped>
@import "@/assets/css/var.scss";

.song-container {
  position: fixed;
  top: 120px;
  left: 50px;
  display: flex;
  flex-direction: column;

  .song-pic {
    height: 300px;
    width: 300px;
    border: 4px solid white;
    border-radius: 12px;
  }

  .song-info {
    width: 300px;
    li {
      width: 100%;
      line-height: 40px;
      font-size: 18px;
      padding-left: 10%;
    }
  }
}

.lyric-container {
  font-family: $font-family;
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
  .song-lyric {
    position: relative;
    min-height: 300px;
    padding: 20px 0;
    overflow: auto;
    border-radius: 12px;
    background: linear-gradient(135deg, #f0f4f8 0%, #e4e9f0 100%);
    display: flex;
    justify-content: center;
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
      position: absolute;
      margin: 100px 0;
      span {
        font-size: 18px;
        color: #95a5a6;
        font-style: italic;
      }
    }
  }
}

@media screen and (min-width: $sm) {
  .container {
    padding-top: 30px;
  }
  .lyric-container {
    margin: 0 150px 0px 400px;
  }
}

@media screen and (max-width: $sm) {
  .container {
    padding: 20px;
  }
  .song-container {
    display: none;
  }
}
</style>