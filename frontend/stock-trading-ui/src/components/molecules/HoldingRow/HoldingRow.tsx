import { TableRow, TableCell, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import type { Holding } from '../../../types/portfolio.types';
import { formatCurrency, formatPercent } from '../../../utils/formatters';

export function HoldingRow({ holding }: { holding: Holding }) {
  const isProfit = holding.profitLoss >= 0;
  const navigate = useNavigate();

  return (
    <TableRow hover onClick={() => navigate(`/market/${holding.symbol}`)} sx={{ cursor: 'pointer' }}>
      <TableCell><Typography fontWeight={700}>{holding.symbol}</Typography></TableCell>
      <TableCell align="right">{holding.quantity}</TableCell>
      <TableCell align="right">{formatCurrency(holding.avgBuyPrice)}</TableCell>
      <TableCell align="right">{formatCurrency(holding.currentPrice)}</TableCell>
      <TableCell align="right">{formatCurrency(holding.currentValue)}</TableCell>
      <TableCell align="right">
        <Typography color={isProfit ? 'success.main' : 'error.main'} fontWeight={600}>
          {isProfit ? '+' : ''}{formatCurrency(holding.profitLoss)} ({formatPercent(holding.profitLossPercent)})
        </Typography>
      </TableCell>
    </TableRow>
  );
}
