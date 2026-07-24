import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { stompClientManager } from '../websocket/stompClient';
import { addLiveNotification } from '../store/notificationSlice';
import { updateLivePrice } from '../store/uiSlice';
import type { RootState } from '../store';
import type { Notification } from '../types/notification.types';
import type { PriceUpdateMessage } from '../types/market.types';

export function useWebSocket() {
  const dispatch = useDispatch();
  const accessToken = useSelector((state: RootState) => state.auth.accessToken);
  const isAuthenticated = useSelector((state: RootState) => state.auth.isAuthenticated);

  useEffect(() => {
    if (!isAuthenticated || !accessToken) return;

    stompClientManager.connect(accessToken, () => {
      stompClientManager.subscribeUserNotifications((message) => {
        const payload: Notification = JSON.parse(message.body);
        dispatch(addLiveNotification(payload));
      });

      stompClientManager.subscribeStockPrices((message) => {
        const payload: PriceUpdateMessage = JSON.parse(message.body);
        dispatch(updateLivePrice({
          symbol: payload.symbol,
          price: payload.currentPrice,
          changePercent: payload.changePercent,
        }));
      });
    });

    return () => stompClientManager.disconnect();
  }, [isAuthenticated, accessToken, dispatch]);
}
