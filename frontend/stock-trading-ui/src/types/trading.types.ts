export type TradeType = 'BUY' | 'SELL';

export interface TradeRequest {
  symbol: string;
  quantity: number;
}

export interface TradeResponse {
  id: number;
  symbol: string;
  tradeType: TradeType;
  quantity: number;
  price: number;
  totalAmount: number;
  status: string;
  executedAt: string;
}

export interface PagedTrades {
  content: TradeResponse[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
