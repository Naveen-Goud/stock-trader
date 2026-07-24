import { Box, Typography, Grid, Paper, Button } from '@mui/material';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import { useQuery } from '@tanstack/react-query';
import { useSelector } from 'react-redux';
import { portfolioApi } from '../../../api/portfolioApi';
import { marketApi } from '../../../api/marketApi';
import { PortfolioSummaryPanel } from '../../organisms/PortfolioSummaryPanel/PortfolioSummaryPanel';
import { StockCard } from '../../molecules/StockCard/StockCard';
import { LoadingSpinner } from '../../atoms/LoadingSpinner/LoadingSpinner';
import type { RootState } from '../../../store';
import { useNavigate } from 'react-router-dom';
import { formatCurrency } from '../../../utils/formatters';

export function DashboardPage() {
  const navigate = useNavigate();
  const user = useSelector((state: RootState) => state.auth.user);

  const { data: portfolio, isLoading: portfolioLoading } = useQuery({
    queryKey: ['portfolio'],
    queryFn: portfolioApi.getPortfolio,
  });

  const { data: stocksPage, isLoading: stocksLoading } = useQuery({
    queryKey: ['stocks', 0, 6],
    queryFn: () => marketApi.getStocks(0, 6),
  });

  if (portfolioLoading || stocksLoading) return <LoadingSpinner />;

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="flex-end" mb={3} flexWrap="wrap" gap={2}>
        <Box>
          <Typography variant="h5" mb={0.5}>
            Welcome back{user ? `, ${user.username}` : ''}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Here's what's happening with your portfolio today.
          </Typography>
        </Box>
        {user && (
          <Paper variant="outlined" sx={{ px: 2.5, py: 1.5, textAlign: 'right' }}>
            <Typography variant="caption" color="text.secondary" display="block">Wallet Balance</Typography>
            <Typography variant="h6" fontWeight={700}>{formatCurrency(user.walletBalance)}</Typography>
          </Paper>
        )}
      </Box>

      {portfolio && (
        <Box mb={4}>
          <PortfolioSummaryPanel summary={portfolio} />
        </Box>
      )}

      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h6">Market Movers</Typography>
        <Button endIcon={<ArrowForwardIcon />} onClick={() => navigate('/market')} size="small">
          View all
        </Button>
      </Box>
      <Grid container spacing={2}>
        {stocksPage?.stocks.map((stock) => (
          <Grid item xs={12} sm={6} md={4} key={stock.symbol}>
            <StockCard stock={stock} onClick={() => navigate(`/market/${stock.symbol}`)} />
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}
