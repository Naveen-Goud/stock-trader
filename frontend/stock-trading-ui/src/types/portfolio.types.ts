export interface Holding {
  symbol: string;
  quantity: number;
  avgBuyPrice: number;
  currentPrice: number;
  currentValue: number;
  profitLoss: number;
  profitLossPercent: number;
}

export interface PortfolioSummary {
  totalInvested: number;
  currentValue: number;
  totalProfitLoss: number;
  totalProfitLossPercent: number;
  holdings: Holding[];
}

export interface WatchlistItem {
  symbol: string;
  currentPrice: number;
  addedAt: string;
}

export interface Watchlist {
  id: number;
  name: string;
  createdAt: string;
  items: WatchlistItem[];
}
