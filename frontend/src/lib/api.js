import axios from "axios";

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

// ---- token storage helpers ----
const ACCESS_KEY = "blog.accessToken";
const REFRESH_KEY = "blog.refreshToken";

export const tokenStore = {
  get access() {
    return localStorage.getItem(ACCESS_KEY);
  },
  get refresh() {
    return localStorage.getItem(REFRESH_KEY);
  },
  set({ accessToken, refreshToken }) {
    if (accessToken) localStorage.setItem(ACCESS_KEY, accessToken);
    if (refreshToken) localStorage.setItem(REFRESH_KEY, refreshToken);
  },
  clear() {
    localStorage.removeItem(ACCESS_KEY);
    localStorage.removeItem(REFRESH_KEY);
  },
};

export const api = axios.create({ baseURL: BASE_URL });

// Attach the access token to every request
api.interceptors.request.use((config) => {
  const token = tokenStore.access;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// On 401, try a single refresh + retry, then give up (and log out)
let refreshing = null;

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config;
    const status = error.response?.status;
    const isAuthCall = original?.url?.includes("/api/auth/");

    if (status === 401 && !original._retry && !isAuthCall && tokenStore.refresh) {
      original._retry = true;
      try {
        refreshing =
          refreshing ||
          axios.post(`${BASE_URL}/api/auth/refresh`, {
            refreshToken: tokenStore.refresh,
          });
        const { data } = await refreshing;
        refreshing = null;
        tokenStore.set(data);
        original.headers.Authorization = `Bearer ${data.accessToken}`;
        return api(original);
      } catch (e) {
        refreshing = null;
        tokenStore.clear();
        window.dispatchEvent(new Event("auth:logout"));
        return Promise.reject(e);
      }
    }
    return Promise.reject(error);
  }
);

// ---- API surface ----
export const AuthApi = {
  register: (body) => api.post("/api/auth/register", body).then((r) => r.data),
  login: (body) => api.post("/api/auth/login", body).then((r) => r.data),
  refresh: (refreshToken) =>
    api.post("/api/auth/refresh", { refreshToken }).then((r) => r.data),
  logout: (refreshToken) =>
    api.post("/api/auth/logout", { refreshToken }).then((r) => r.data),
};

export const PostApi = {
  list: (params) => api.get("/api/posts", { params }).then((r) => r.data),
  get: (id) => api.get(`/api/posts/${id}`).then((r) => r.data),
  search: (kw) => api.get(`/api/posts/search/${encodeURIComponent(kw)}`).then((r) => r.data),
  byUser: (userId) => api.get(`/api/user/${userId}/posts`).then((r) => r.data),
  byCategory: (catId) => api.get(`/api/category/${catId}/posts`).then((r) => r.data),
  create: (userId, categoryId, body) =>
    api.post(`/api/user/${userId}/category/${categoryId}/posts`, body).then((r) => r.data),
  update: (id, body) => api.put(`/api/posts/${id}`, body).then((r) => r.data),
  remove: (id) => api.delete(`/api/posts/${id}`).then((r) => r.data),
  summarize: (id) => api.post(`/api/posts/${id}/ai/summarize`).then((r) => r.data),
  uploadImage: (id, file) => {
    const form = new FormData();
    form.append("image", file);
    return api
      .post(`/api/post/image/upload/${id}`, form, {
        headers: { "Content-Type": "multipart/form-data" },
      })
      .then((r) => r.data);
  },
  imageUrl: (name) => `${BASE_URL}/api/post/image/${name}`,
};

export const UserApi = {
  me: () => api.get("/api/users/me").then((r) => r.data),
  get: (id) => api.get(`/api/users/${id}`).then((r) => r.data),
};

export const CategoryApi = {
  list: () => api.get("/api/categories/").then((r) => r.data),
  get: (id) => api.get(`/api/categories/${id}`).then((r) => r.data),
  create: (body) => api.post("/api/categories/", body).then((r) => r.data),
};

export const CommentApi = {
  create: (postId, body) => api.post(`/api/post/${postId}/comments`, body).then((r) => r.data),
  remove: (commentId) => api.delete(`/api/comments/${commentId}`).then((r) => r.data),
};

export const AiApi = {
  status: () => api.get("/api/ai/status").then((r) => r.data),
};

export { BASE_URL };
