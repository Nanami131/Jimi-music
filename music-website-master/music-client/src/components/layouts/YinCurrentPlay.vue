<!-- 文件: src/components/layouts/YinCurrentPlay.vue -->
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
      <lyric-panel initial-top="50px" />
    </div>
  </transition>
</template>

<script lang="ts">
import { defineComponent, computed, onMounted, onUnmounted } from "vue";
import { useStore } from "vuex";
import mixin from "@/mixins/mixin";
import LyricPanel from "@/components/LyricPanel.vue";

export default defineComponent({
  components: {
    LyricPanel,
  },
  setup() {
    const store = useStore();
    const { getSongTitle, playMusic } = mixin();

    const songId = computed(() => store.getters.songId);
    const currentPlayList = computed(() => store.getters.currentPlayList);
    const showAside = computed(() => store.getters.showAside);

    const handleClickOutside = (e: MouseEvent) => {
      const aside = document.querySelector('.yin-current-play') as HTMLElement;
      const playBar = document.querySelector('.play-bar') as HTMLElement;
      if (aside && playBar && !aside.contains(e.target as Node) && !playBar.contains(e.target as Node)) {
        store.commit('setShowAside', false);
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
  background: linear-gradient(135deg, #f0f4f8 0%, #e4e9f0 100%);
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
</style>