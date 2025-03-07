<template>
  <el-breadcrumb class="crumbs" separator="/">
    <el-breadcrumb-item v-for="item in breadcrumbList" :key="item.name" :to="{ path: item.path, query: item.query }">
      {{ item.name }}
    </el-breadcrumb-item>
  </el-breadcrumb>

  <div class="container">
    <div class="handle-box">
      <el-button @click="deleteAll">批量删除</el-button>
      <el-input v-model="searchWord" placeholder="筛选关键词"></el-input>
      <el-button type="primary" @click="centerDialogVisible = true">添加歌曲</el-button>
    </div>
    <el-table height="550px" border size="small" :data="data" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="40"></el-table-column>
      <el-table-column label="ID" prop="id" width="50" align="center"></el-table-column>
      <el-table-column label="歌手图片" width="110" align="center">
        <template v-slot="scope">
          <div style="width: 80px; height: 80px; overflow: hidden">
            <img :src="attachImageUrl(scope.row.pic)" style="width: 100%" />
          </div>
          <div class="play" @click="setSongUrl(scope.row)">
            <svg class="icon" aria-hidden="true">
              <use :xlink:href="toggle === scope.row.name ? playIcon : BOFANG"></use>
            </svg>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="歌名" prop="name" width="150"></el-table-column>
      <el-table-column label="专辑" prop="introduction" width="150"></el-table-column>
      <el-table-column label="歌词" align="center">
        <template v-slot="scope">
          <ul style="height: 100px; overflow: scroll">
            <li v-for="(item, index) in parseLyric(scope.row.lyric)" :key="index">
              {{ item }}
            </li>
          </ul>
        </template>
      </el-table-column>
      <el-table-column label="资源更新" width="120" align="center">
        <template v-slot="scope">
          <el-upload
              :action="updateSongImg(scope.row.id)"
              :show-file-list="false"
              :on-success="handleImgSuccess"
              :before-upload="beforeImgUpload"
              :http-request="customUploadImg"
          >
            <el-button>更新图片</el-button>
          </el-upload>
          <br />
          <el-upload
              :action="updateSongUrl(scope.row.id)"
              :show-file-list="false"
              :on-success="handleSongSuccess"
              :before-upload="beforeSongUpload"
              :http-request="customUploadSong"
          >
            <el-button>更新歌曲</el-button>
          </el-upload>
          <br />
          <el-upload
              :action="updateSongLrc(scope.row.id)"
              :show-file-list="false"
              :on-success="handleSongSuccess"
              :before-upload="beforeSongUpload"
              :http-request="customUploadLrc"
          >
            <el-button>更新歌词</el-button>
          </el-upload>
        </template>
      </el-table-column>
      <el-table-column label="评论" width="90" align="center">
        <template v-slot="scope">
          <el-button @click="goCommentPage(scope.row.id)">评论</el-button>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center">
        <template v-slot="scope">
          <el-button @click="editRow(scope.row)">编辑</el-button>
          <el-button type="danger" @click="deleteRow(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
        class="pagination"
        background
        layout="total, prev, pager, next"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="tableData.length"
        @current-change="handleCurrentChange"
    >
    </el-pagination>
  </div>

  <!-- 添加歌曲 -->
  <el-dialog title="添加歌曲" v-model="centerDialogVisible">
    <el-form id="add-song" label-width="120px" :model="registerForm">
      <el-form-item label="歌曲名">
        <el-input type="text" name="name" v-model="registerForm.name"></el-input>
      </el-form-item>
      <el-form-item label="专辑">
        <el-input type="text" name="introduction" v-model="registerForm.introduction"></el-input>
      </el-form-item>
      <el-form-item label="歌词（有歌词lrc可以直接上传）">
        <el-input type="textarea" name="lyric" v-model="registerForm.lyric"></el-input>
      </el-form-item>
      <el-form-item label="歌词lrc上传">
        <input type="file" name="lrcfile" />
      </el-form-item>
      <el-form-item label="歌曲上传">
        <input type="file" name="file" />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="centerDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="addSong">确 定</el-button>
      </span>
    </template>
  </el-dialog>

  <!-- 编辑弹出框 -->
  <el-dialog title="编辑" v-model="editVisible">
    <el-form :model="editForm">
      <el-form-item label="歌曲">
        <el-input v-model="editForm.name"></el-input>
      </el-form-item>
      <el-form-item label="简介">
        <el-input v-model="editForm.introduction"></el-input>
      </el-form-item>
      <el-form-item label="歌词">
        <el-input type="textarea" v-model="editForm.lyric"></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="editVisible = false">取 消</el-button>
        <el-button type="primary" @click="saveEdit">确 定</el-button>
      </span>
    </template>
  </el-dialog>

  <!-- 删除提示框 -->
  <yin-del-dialog :delVisible="delVisible" @confirm="confirm" @cancelRow="delVisible = $event"></yin-del-dialog>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, watch, ref, reactive, computed } from "vue";
import { useStore } from "vuex";
import mixin from "@/mixins/mixin";
import { Icon, RouterName } from "@/enums";
import { HttpManager } from "@/api";
import { parseLyric } from "@/utils";
import YinDelDialog from "@/components/dialog/YinDelDialog.vue";
import { post, getBaseURL } from "@/api/request"; // 正确导入

interface ResponseBody {
  code: number;
  message: string;
  type?: string;
  success?: boolean;
  data: any;
}

export default defineComponent({
  name: "Song",
  components: {
    YinDelDialog,
  },
  setup() {
    const { proxy } = getCurrentInstance();
    const { routerManager, beforeImgUpload, beforeSongUpload } = mixin();
    const store = useStore();

    const tableData = ref<any[]>([]);
    const tempDate = ref<any[]>([]);
    const pageSize = ref(5);
    const currentPage = ref(1);
    const singerId = ref<string>("");
    const singerName = ref<string>("");
    const toggle = ref<string | boolean>(false);
    const BOFANG = ref(Icon.BOFANG);
    const ZANTING = ref(Icon.ZANTING);
    const breadcrumbList = computed(() => store.getters.breadcrumbList);

    const isPlay = computed(() => store.getters.isPlay);
    const playIcon = computed(() => (isPlay.value ? ZANTING.value : BOFANG.value));
    const data = computed(() => {
      return tableData.value.slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value);
    });

    const searchWord = ref("");
    watch(searchWord, () => {
      if (searchWord.value === "") {
        tableData.value = tempDate.value;
      } else {
        tableData.value = tempDate.value.filter(item => item.name.includes(searchWord.value));
      }
    });

    singerId.value = proxy.$route.query.id as string;
    singerName.value = proxy.$route.query.name as string;
    proxy.$store.commit("setIsPlay", false);
    getData();

    async function getData() {
      try {
        tableData.value = [];
        tempDate.value = [];
        const result = (await HttpManager.getSongOfSingerId(singerId.value)) as ResponseBody;
        tableData.value = result.data;
        tempDate.value = result.data;
        currentPage.value = 1;
      } catch (error) {
        console.error("Failed to fetch song data:", error);
      }
    }

    function setSongUrl(row: any) {
      proxy.$store.commit("setUrl", row.url);
      toggle.value = row.name;
      proxy.$store.commit("setIsPlay", !isPlay.value);
    }

    function updateSongImg(id: string) {
      return HttpManager.updateSongImg(id);
    }
    function updateSongUrl(id: string) {
      return HttpManager.updateSongUrl(id);
    }
    function updateSongLrc(id: string) {
      return HttpManager.updateSongLrc(id);
    }

    function handleCurrentChange(val: number) {
      currentPage.value = val;
    }

    function handleSongSuccess(res: ResponseBody) {
      (proxy as any).$message({
        message: res.message,
        type: res.type,
      });
      if (res.success) getData();
    }
    function handleImgSuccess(res: ResponseBody) {
      (proxy as any).$message({
        message: res.message,
        type: res.type,
      });
      if (res.success) getData();
    }

    async function customUploadImg(options: any) {
      const formData = new FormData();
      formData.append("file", options.file);
      try {
        console.log("Uploading image to:", options.action);
        const relativeUrl = options.action.replace(getBaseURL(), ""); // 修正为 getBaseURL
        const response = await post(relativeUrl, formData);
        console.log("Image upload success:", response);
        options.onSuccess(response);
      } catch (error) {
        console.error("Image upload error:", error);
        if (error.status === 401) {
          (proxy as any).$message.error("未授权，请重新登录！");
        }
        options.onError(error);
      }
    }

    async function customUploadSong(options: any) {
      const formData = new FormData();
      formData.append("file", options.file);
      try {
        console.log("Uploading song to:", options.action);
        const relativeUrl = options.action.replace(getBaseURL(), "");
        const response = await post(relativeUrl, formData);
        console.log("Song upload success:", response);
        options.onSuccess(response);
      } catch (error) {
        console.error("Song upload error:", error);
        if (error.status === 401) {
          (proxy as any).$message.error("未授权，请重新登录！");
        }
        options.onError(error);
      }
    }

    async function customUploadLrc(options: any) {
      const formData = new FormData();
      formData.append("file", options.file);
      try {
        console.log("Uploading lyric to:", options.action);
        const relativeUrl = options.action.replace(getBaseURL(), "");
        const response = await post(relativeUrl, formData);
        console.log("Lyric upload success:", response);
        options.onSuccess(response);
      } catch (error) {
        console.error("Lyric upload error:", error);
        if (error.status === 401) {
          (proxy as any).$message.error("未授权，请重新登录！");
        }
        options.onError(error);
      }
    }

    function goCommentPage(id: string) {
      const breadcrumbList = reactive([
        { path: RouterName.Singer, name: "歌手管理" },
        { path: RouterName.Song, query: { id: singerId.value, name: singerName.value }, name: "歌曲信息" },
        { path: "", name: "评论详情" },
      ]);
      proxy.$store.commit("setBreadcrumbList", breadcrumbList);
      routerManager(RouterName.Comment, { path: RouterName.Comment, query: { id, type: 0 } });
    }

    const centerDialogVisible = ref(false);
    const registerForm = reactive({
      name: "",
      singerName: "",
      introduction: "",
      lyric: "",
    });

    async function addSong() {
      const addSongForm = new FormData(document.getElementById("add-song") as HTMLFormElement);
      addSongForm.append("singerId", singerId.value);
      addSongForm.set("name", addSongForm.get("name"));
      if (!addSongForm.get("lyric")) addSongForm.set("lyric", "[00:00:00]暂无歌词");

      try {
        const res = await post(`/song/add`, addSongForm);
        (proxy as any).$message({
          message: res.message,
          type: res.type,
        });
        if (res.success) {
          getData();
          registerForm.name = "";
          registerForm.singerName = "";
          registerForm.introduction = "";
          registerForm.lyric = "";
        }
      } catch (error) {
        console.error("Add song error:", error);
        if (error.status === 401) {
          (proxy as any).$message.error("未授权，请重新登录！");
        }
      }
      centerDialogVisible.value = false;
    }

    const editVisible = ref(false);
    const editForm = reactive({
      id: "",
      singerId: "",
      name: "",
      introduction: "",
      createTime: "",
      updateTime: "",
      pic: "",
      lyric: "",
      url: "",
    });

    function editRow(row: any) {
      idx.value = row.id;
      Object.assign(editForm, row);
      editVisible.value = true;
    }

    async function saveEdit() {
      const { id, singerId, name, introduction, lyric } = editForm;
      try {
        const result = (await HttpManager.updateSongMsg({ id, singerId, name, introduction, lyric })) as ResponseBody;
        (proxy as any).$message({
          message: result.message,
          type: result.type,
        });
        if (result.success) getData();
        editVisible.value = false;
      } catch (error) {
        console.error("Failed to update song:", error);
      }
    }

    const idx = ref(-1);
    const multipleSelection = ref<any[]>([]);
    const delVisible = ref(false);

    async function confirm() {
      try {
        const result = (await HttpManager.deleteSong(idx.value)) as ResponseBody;
        (proxy as any).$message({
          message: result.message,
          type: result.type,
        });
        if (result.success) getData();
        delVisible.value = false;
      } catch (error) {
        console.error("Failed to delete song:", error);
      }
    }

    function deleteRow(id: number) {
      idx.value = id;
      delVisible.value = true;
    }

    function handleSelectionChange(val: any[]) {
      multipleSelection.value = val;
    }

    function deleteAll() {
      for (const item of multipleSelection.value) {
        deleteRow(item.id);
        confirm();
      }
      multipleSelection.value = [];
    }

    return {
      playIcon,
      toggle,
      searchWord,
      data,
      editForm,
      registerForm,
      tableData,
      centerDialogVisible,
      editVisible,
      delVisible,
      pageSize,
      currentPage,
      ZANTING,
      BOFANG,
      breadcrumbList,
      deleteAll,
      handleSelectionChange,
      handleCurrentChange,
      handleImgSuccess,
      beforeImgUpload,
      parseLyric,
      saveEdit,
      updateSongImg,
      updateSongUrl,
      updateSongLrc,
      deleteRow,
      confirm,
      attachImageUrl: HttpManager.attachImageUrl,
      addSong,
      editRow,
      handleSongSuccess,
      setSongUrl,
      goCommentPage,
      customUploadImg,
      customUploadSong,
      customUploadLrc,
    };
  },
});
</script>

<style scoped>
.play {
  position: absolute;
  z-index: 100;
  width: 80px;
  height: 80px;
  top: 18px;
  left: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.icon {
  width: 2em;
  height: 2em;
  color: white;
  fill: currentColor;
  overflow: hidden;
}
</style>