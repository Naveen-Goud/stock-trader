import { axiosClient } from './axiosClient';
import type { TradeRequest, TradeResponse, PagedTrades } from '../types/trading.types';

export const tradingApi = {
  buy: (data: TradeRequest, idempotencyKey: string) =>
    axiosClient
      .post<TradeResponse>('/api/trading/buy', data, { headers: { 'Idempotency-Key': idempotencyKey } })
      .then((r) => r.data),
  sell: (data: TradeRequest, idempotencyKey: string) =>
    axiosClient
      .post<TradeResponse>('/api/trading/sell', data, { headers: { 'Idempotency-Key': idempotencyKey } })
      .then((r) => r.data),
  getHistory: (page: number, size: number) =>
    axiosClient.get<PagedTrades>('/api/trading/history', { params: { page, size } }).then((r) => r.data),
};
