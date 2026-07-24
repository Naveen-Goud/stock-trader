export type NotificationType = 'TRADE_EXECUTED' | 'PRICE_ALERT' | 'PORTFOLIO_MILESTONE' | 'SYSTEM';

export interface Notification {
  id: number;
  type: NotificationType;
  message: string;
  relatedSymbol: string | null;
  isRead: boolean;
  createdAt: string;
}

export interface NotificationPage {
  notifications: Notification[];
  totalElements: number;
  totalPages: number;
  unreadCount: number;
}

export interface PriceAlert {
  id: number;
  symbol: string;
  alertType: 'ABOVE' | 'BELOW';
  targetPrice: number;
  isActive: boolean;
  triggeredAt: string | null;
  createdAt: string;
}
