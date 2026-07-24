import { axiosClient } from './axiosClient';
import type { StockPage, StockDetail } from '../types/market.types';

export const marketApi = {
  getStocks: (page: number, size: number, sector?: string, sortBy?: string) =>
    axiosClient
      .get<StockPage>('/api/market/stocks', { params: { page, size, sector, sortBy } })
      .then((r) => r.data),
  getStock: (symbol: string) =>
    axiosClient.get<StockDetail>(`/api/market/stocks/${symbol}`).then((r) => r.data),
  search: (query: string, page = 0, size = 10) =>
    axiosClient
      .get<StockPage>('/api/market/stocks/search', { params: { q: query, page, size } })
      .then((r) => r.data),
  getSectors: () => axiosClient.get<string[]>('/api/market/sectors').then((r) => r.data),
};
