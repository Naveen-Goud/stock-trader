import axios, { type InternalAxiosRequestConfig } from 'axios';
import { store } from '../store';
import { logout, setTokens } from '../store/authSlice';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const axiosClient = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

axiosClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = store.getState().auth.accessToken;
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let isRefreshing = false;
let refreshQueue: Array<() => void> = [];

axiosClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve) => {
          refreshQueue.push(() => resolve(axiosClient(originalRequest)));
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const refreshToken = store.getState().auth.refreshToken;
        const { data } = await axios.post(`${API_BASE_URL}/api/users/refresh`, { refreshToken });
        // Refresh tokens rotate server-side: this call revokes the old token
        // and issues a new one. We must store the new refreshToken from the
        // response, not the old one -- reusing a revoked token on the next
        // refresh trips reuse-detection and revokes every session.
        store.dispatch(setTokens({ accessToken: data.accessToken, refreshToken: data.refreshToken }));
        refreshQueue.forEach((cb) => cb());
        refreshQueue = [];
        return axiosClient(originalRequest);
      } catch (refreshError) {
        store.dispatch(logout());
        window.location.href = '/login';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);
