import axios from "axios";
import router from "@/router";
import { ElMessageBox } from "element-plus";

const BASE_URL = process.env.NODE_HOST;

axios.defaults.timeout = 5000;
axios.defaults.withCredentials = true;
axios.defaults.baseURL = BASE_URL;
axios.defaults.headers.post["Content-Type"] = "application/x-www-form-urlencoded;charset=UTF-8";

// 用于跟踪 401 弹窗状态
let isShowing401 = false;

axios.interceptors.response.use(
    (response) => {
      if (response.status === 200) {
        return Promise.resolve(response);
      } else {
        return Promise.reject(response);
      }
    },
    async (error) => {
      if (error.response && error.response.status) {
        switch (error.response.status) {
          case 401:
            if (!isShowing401) { // 只在未显示弹窗时触发
              isShowing401 = true;
              try {
                await ElMessageBox.confirm("登录已到期，请重新登录", "提示", {
                  confirmButtonText: "确认",
                  type: "warning",
                  showCancelButton: false,
                });
                router.replace({ path: "/sign-in" });
              } catch (e) {
                // 用户关闭弹窗，不做额外处理
              } finally {
                isShowing401 = false; // 重置标志
              }
            }
            return Promise.resolve({ success: false, message: "登录已到期", data: null }); // 添加 data: null
          case 403:
            setTimeout(() => {
              router.replace({
                path: "/",
                query: {
                  // redirect: router.currentRoute.fullPath
                },
              });
            }, 1000);
            return Promise.reject(error.response);
          case 404:
            break;
        }
        return Promise.reject(error.response);
      } else {
        console.error("请求失败:", error);
        return Promise.reject(error);
      }
    }
);

export function getBaseURL() {
  return BASE_URL;
}

export function get(url, params?: object) {
  return new Promise((resolve, reject) => {
    axios.get(url, params).then(
        (response) => resolve(response.data),
        (error) => reject(error)
    );
  });
}

export function post(url, data = {}) {
  return new Promise((resolve, reject) => {
    axios.post(url, data).then(
        (response) => resolve(response.data),
        (error) => reject(error)
    );
  });
}

export function deletes(url, data = {}) {
  return new Promise((resolve, reject) => {
    axios.delete(url, data).then(
        (response) => resolve(response.data),
        (error) => reject(error)
    );
  });
}

export function put(url, data = {}) {
  return new Promise((resolve, reject) => {
    axios.put(url, data).then(
        (response) => resolve(response.data),
        (error) => reject(error)
    );
  });
}