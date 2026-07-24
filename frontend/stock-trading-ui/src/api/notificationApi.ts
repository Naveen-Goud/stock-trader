import { axiosClient } from './axiosClient';
import type { NotificationPage, PriceAlert } from '../types/notification.types';

export const notificationApi = {
  getNotifications: (page: number, size: number) =>
    axiosClient.get<NotificationPage>('/api/notifications', { params: { page, size } }).then((r) => r.data),
  markAllRead: () => axiosClient.put('/api/notifications/read-all'),
  markOneRead: (id: number) => axiosClient.put(`/api/notifications/${id}/read`),
  createAlert: (symbol: string, alertType: 'ABOVE' | 'BELOW', targetPrice: number) =>
    axiosClient
      .post<PriceAlert>('/api/notifications/alerts', { symbol, alertType, targetPrice })
      .then((r) => r.data),
  getAlerts: () => axiosClient.get<PriceAlert[]>('/api/notifications/alerts').then((r) => r.data),
  deleteAlert: (id: number) => axiosClient.delete(`/api/notifications/alerts/${id}`),
};
