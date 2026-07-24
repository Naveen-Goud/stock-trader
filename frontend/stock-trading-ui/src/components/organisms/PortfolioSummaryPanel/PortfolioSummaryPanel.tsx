import { Grid, Paper, Typography, Box } from '@mui/material';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';
import ShowChartIcon from '@mui/icons-material/ShowChart';
import { formatCurrency, formatPercent } from '../../../utils/formatters';
import type { PortfolioSummary } from '../../../types/portfolio.types';

export function PortfolioSummaryPanel({ summary }: { summary: PortfolioSummary }) {
  const isProfit = summary.totalProfitLoss >= 0;

  const cards = [
    { label: 'Total Invested', value: formatCurrency(summary.totalInvested), icon: <AccountBalanceWalletIcon /> },
    { label: 'Current Value', value: formatCurrency(summary.currentValue), icon: <ShowChartIcon /> },
    {
      label: 'Total P&L',
      value: `${isProfit ? '+' : ''}${formatCurrency(summary.totalProfitLoss)}`,
      color: isProfit ? 'success.main' : 'error.main',
      icon: isProfit ? <TrendingUpIcon /> : <TrendingDownIcon />,
    },
    {
      label: 'P&L %',
      value: formatPercent(summary.totalProfitLossPercent),
      color: isProfit ? 'success.main' : 'error.main',
      icon: isProfit ? <TrendingUpIcon /> : <TrendingDownIcon />,
    },
  ];

  return (
    <Grid container spacing={2}>
      {cards.map((card) => (
        <Grid item xs={6} md={3} key={card.label}>
          <Paper variant="outlined" sx={{ p: 2.5 }}>
            <Box display="flex" alignItems="center" justifyContent="space-between" mb={1}>
              <Typography variant="body2" color="text.secondary">{card.label}</Typography>
              <Box sx={{ color: card.color ?? 'text.secondary', display: 'flex', opacity: 0.7 }}>
                {card.icon}
              </Box>
            </Box>
            <Typography variant="h6" fontWeight={700} color={card.color ?? 'text.primary'}>
              {card.value}
            </Typography>
          </Paper>
        </Grid>
      ))}
    </Grid>
  );
}
