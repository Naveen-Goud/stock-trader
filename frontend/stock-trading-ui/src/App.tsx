import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { ThemeProvider, CssBaseline } from '@mui/material';
import { theme } from './theme';
import type { RootState } from './store';
import { useWebSocket } from './hooks/useWebSocket';

import { AuthLayout } from './components/templates/AuthLayout/AuthLayout';
import { DashboardLayout } from './components/templates/DashboardLayout/DashboardLayout';
import { LoginPage } from './components/pages/LoginPage/LoginPage';
import { RegisterPage } from './components/pages/RegisterPage/RegisterPage';
import { DashboardPage } from './components/pages/DashboardPage/DashboardPage';
import { StockMarketPage } from './components/pages/StockMarketPage/StockMarketPage';
import { StockDetailPage } from './components/pages/StockDetailPage/StockDetailPage';
import { PortfolioPage } from './components/pages/PortfolioPage/PortfolioPage';
import { WatchlistPage } from './components/pages/WatchlistPage/WatchlistPage';
import { TransactionHistoryPage } from './components/pages/TransactionHistoryPage/TransactionHistoryPage';
import { NotificationsPage } from './components/pages/NotificationsPage/NotificationsPage';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useSelector((state: RootState) => state.auth.isAuthenticated);
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
}

export default function App() {
  useWebSocket();

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <Routes>
          <Route element={<AuthLayout />}>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
          </Route>

          <Route
            element={
              <ProtectedRoute>
                <DashboardLayout />
              </ProtectedRoute>
            }
          >
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/market" element={<StockMarketPage />} />
            <Route path="/market/:symbol" element={<StockDetailPage />} />
            <Route path="/portfolio" element={<PortfolioPage />} />
            <Route path="/watchlist" element={<WatchlistPage />} />
            <Route path="/history" element={<TransactionHistoryPage />} />
            <Route path="/notifications" element={<NotificationsPage />} />
          </Route>

          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
}
