export interface Stock {
  symbol: string;
  companyName: string;
  sector: string;
  currentPrice: number;
  previousClose: number;
  changeAmount: number;
  changePercent: number;
  marketCap: number;
  lastUpdated: string;
}

export interface StockPage {
  stocks: Stock[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface PriceHistoryPoint {
  price: number;
  changePercent: number;
  recordedAt: string;
}

export interface StockDetail extends Stock {
  priceHistory: PriceHistoryPoint[];
}

export interface PriceUpdateMessage {
  symbol: string;
  currentPrice: number;
  changePercent: number;
}
