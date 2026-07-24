import { Client, type IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL || 'http://localhost:8080';

export class StompClientManager {
  private client: Client | null = null;

  connect(accessToken: string, onConnected: () => void) {
    this.client = new Client({
      webSocketFactory: () => new SockJS(`${WS_BASE_URL}/ws`) as any,
      connectHeaders: { Authorization: `Bearer ${accessToken}` },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: onConnected,
      onStompError: (frame) => console.error('STOMP error:', frame.headers.message),
    });
    this.client.activate();
  }

  subscribeUserNotifications(callback: (message: IMessage) => void) {
    return this.client?.subscribe('/user/queue/notifications', callback);
  }

  subscribeStockPrices(callback: (message: IMessage) => void) {
    return this.client?.subscribe('/topic/stock-prices', callback);
  }

  disconnect() {
    this.client?.deactivate();
  }
}

export const stompClientManager = new StompClientManager();
