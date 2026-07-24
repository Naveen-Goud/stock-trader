import { axiosClient } from './axiosClient';
import type { PortfolioSummary, Watchlist } from '../types/portfolio.types';

export const portfolioApi = {
  getPortfolio: () => axiosClient.get<PortfolioSummary>('/api/portfolio').then((r) => r.data),
  getWatchlists: () => axiosClient.get<Watchlist[]>('/api/portfolio/watchlists').then((r) => r.data),
  createWatchlist: (name: string) =>
    axiosClient.post<Watchlist>('/api/portfolio/watchlists', { name }).then((r) => r.data),
  addWatchlistItem: (watchlistId: number, symbol: string) =>
    axiosClient.post<Watchlist>(`/api/portfolio/watchlists/${watchlistId}/items`, { symbol }).then((r) => r.data),
  removeWatchlistItem: (watchlistId: number, symbol: string) =>
    axiosClient.delete(`/api/portfolio/watchlists/${watchlistId}/items/${symbol}`),
};
