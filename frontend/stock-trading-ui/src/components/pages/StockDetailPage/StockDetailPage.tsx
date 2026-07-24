import { useParams, useNavigate } from 'react-router-dom';
import { Box, Typography, Grid, Paper, IconButton, Chip } from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { useQuery } from '@tanstack/react-query';
import { useSelector } from 'react-redux';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import { marketApi } from '../../../api/marketApi';
import { LoadingSpinner } from '../../atoms/LoadingSpinner/LoadingSpinner';
import { PriceTag } from '../../atoms/PriceTag/PriceTag';
import { TradeForm } from '../../organisms/TradeForm/TradeForm';
import { PriceAlertsPanel } from '../../organisms/PriceAlertsPanel/PriceAlertsPanel';
import { formatCurrency } from '../../../utils/formatters';
import type { RootState } from '../../../store';

export function StockDetailPage() {
  const { symbol = '' } = useParams<{ symbol: string }>();
  const navigate = useNavigate();
  const livePrices = useSelector((state: RootState) => state.ui.livePrices);

  const { data: stock, isLoading } = useQuery({
    queryKey: ['stock', symbol],
    queryFn: () => marketApi.getStock(symbol),
    enabled: !!symbol,
  });

  if (isLoading) return <LoadingSpinner />;
  if (!stock) {
    return (
      <Box>
        <Typography variant="h6">Stock not found.</Typography>
      </Box>
    );
  }

  const live = livePrices[stock.symbol];
  const displayPrice = live?.price ?? stock.currentPrice;
  const displayChange = live?.changePercent ?? stock.changePercent;

  const chartData = stock.priceHistory.map((p) => ({
    time: new Date(p.recordedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    price: p.price,
  }));

  return (
    <Box>
      <Box display="flex" alignItems="center" gap={1} mb={3}>
        <IconButton onClick={() => navigate(-1)} size="small">
          <ArrowBackIcon />
        </IconButton>
        <Box>
          <Box display="flex" alignItems="center" gap={1.5}>
            <Typography variant="h5" fontWeight={700}>{stock.symbol}</Typography>
            <Chip label={stock.sector} size="small" variant="outlined" />
          </Box>
          <Typography variant="body2" color="text.secondary">{stock.companyName}</Typography>
        </Box>
      </Box>

      <Grid container spacing={3}>
        <Grid item xs={12} md={8}>
          <Paper variant="outlined" sx={{ p: 3, mb: 3 }}>
            <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
              <PriceTag price={displayPrice} changePercent={displayChange} size="large" />
              <Box textAlign="right">
                <Typography variant="caption" color="text.secondary" display="block">Previous Close</Typography>
                <Typography fontWeight={600}>{formatCurrency(stock.previousClose)}</Typography>
              </Box>
            </Box>

            <Box height={280}>
              {chartData.length > 1 ? (
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={chartData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                    <XAxis dataKey="time" tick={{ fontSize: 11 }} minTickGap={30} />
                    <YAxis domain={['auto', 'auto']} tick={{ fontSize: 11 }} width={60} />
                    <Tooltip formatter={(value: number) => formatCurrency(value)} />
                    <Line type="monotone" dataKey="price" stroke="#0d7a71" strokeWidth={2} dot={false} />
                  </LineChart>
                </ResponsiveContainer>
              ) : (
                <Box display="flex" alignItems="center" justifyContent="center" height="100%">
                  <Typography variant="body2" color="text.secondary">
                    Not enough price history yet — check back shortly.
                  </Typography>
                </Box>
              )}
            </Box>
          </Paper>

          <Grid container spacing={2}>
            <Grid item xs={6} sm={3}>
              <Paper variant="outlined" sx={{ p: 2 }}>
                <Typography variant="caption" color="text.secondary">Market Cap</Typography>
                <Typography fontWeight={700}>{formatCurrency(stock.marketCap)}</Typography>
              </Paper>
            </Grid>
            <Grid item xs={6} sm={3}>
              <Paper variant="outlined" sx={{ p: 2 }}>
                <Typography variant="caption" color="text.secondary">Change ($)</Typography>
                <Typography fontWeight={700} color={stock.changeAmount >= 0 ? 'success.main' : 'error.main'}>
                  {stock.changeAmount >= 0 ? '+' : ''}{formatCurrency(stock.changeAmount)}
                </Typography>
              </Paper>
            </Grid>
          </Grid>
        </Grid>

        <Grid item xs={12} md={4}>
          <Paper variant="outlined" sx={{ p: 3, mb: 3 }}>
            <Typography variant="subtitle1" mb={2}>Trade {stock.symbol}</Typography>
            <TradeForm symbol={stock.symbol} currentPrice={displayPrice} />
          </Paper>

          <PriceAlertsPanel defaultSymbol={stock.symbol} />
        </Grid>
      </Grid>
    </Box>
  );
}
