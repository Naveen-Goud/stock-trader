import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
import type { Notification } from '../types/notification.types';

interface NotificationState {
  liveNotifications: Notification[];
  unreadCount: number;
}

const initialState: NotificationState = { liveNotifications: [], unreadCount: 0 };

const notificationSlice = createSlice({
  name: 'notifications',
  initialState,
  reducers: {
    addLiveNotification: (state, action: PayloadAction<Notification>) => {
      state.liveNotifications.unshift(action.payload);
      state.unreadCount += 1;
    },
    setUnreadCount: (state, action: PayloadAction<number>) => {
      state.unreadCount = action.payload;
    },
    clearUnread: (state) => {
      state.unreadCount = 0;
    },
  },
});

export const { addLiveNotification, setUnreadCount, clearUnread } = notificationSlice.actions;
export default notificationSlice.reducer;
