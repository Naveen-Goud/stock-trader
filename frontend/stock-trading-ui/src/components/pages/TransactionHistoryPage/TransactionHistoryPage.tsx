import { useState } from 'react';
import { Box, Typography, Table, TableHead, TableRow, TableCell, TableBody, Paper, TableContainer, TablePagination } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { tradingApi } from '../../../api/tradingApi';
import { StatusBadge } from '../../atoms/StatusBadge/StatusBadge';
import { LoadingSpinner } from '../../atoms/LoadingSpinner/LoadingSpinner';
import { formatCurrency, formatDateTime } from '../../../utils/formatters';

export function TransactionHistoryPage() {
  const [page, setPage] = useState(0);
  const size = 20;

  const { data, isLoading } = useQuery({
    queryKey: ['tradeHistory', page],
    queryFn: () => tradingApi.getHistory(page, size),
  });

  if (isLoading) return <LoadingSpinner />;

  return (
    <Box>
      <Typography variant="h5" mb={3}>Transaction History</Typography>

      <TableContainer component={Paper} variant="outlined">
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Date</TableCell>
              <TableCell>Symbol</TableCell>
              <TableCell>Type</TableCell>
              <TableCell align="right">Qty</TableCell>
              <TableCell align="right">Price</TableCell>
              <TableCell align="right">Total</TableCell>
              <TableCell>Status</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {data?.content.map((trade) => (
              <TableRow key={trade.id} hover>
                <TableCell>{formatDateTime(trade.executedAt)}</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>{trade.symbol}</TableCell>
                <TableCell><StatusBadge status={trade.tradeType} /></TableCell>
                <TableCell align="right">{trade.quantity}</TableCell>
                <TableCell align="right">{formatCurrency(trade.price)}</TableCell>
                <TableCell align="right">{formatCurrency(trade.totalAmount)}</TableCell>
                <TableCell><StatusBadge status={trade.status} /></TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        <TablePagination
          component="div"
          count={data?.totalElements ?? 0}
          page={page}
          rowsPerPage={size}
          rowsPerPageOptions={[size]}
          onPageChange={(_, newPage) => setPage(newPage)}
        />
      </TableContainer>
    </Box>
  );
}
