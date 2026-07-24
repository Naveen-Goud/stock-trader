import { Table, TableHead, TableBody, TableRow, TableCell, TableContainer, Paper, TablePagination } from '@mui/material';
import { PriceTag } from '../../atoms/PriceTag/PriceTag';
import { useSelector } from 'react-redux';
import type { RootState } from '../../../store';
import type { Stock } from '../../../types/market.types';

interface StockTableProps {
  stocks: Stock[];
  totalElements: number;
  page: number;
  size: number;
  onPageChange: (page: number) => void;
  onRowClick: (symbol: string) => void;
}

export function StockTable({ stocks, totalElements, page, size, onPageChange, onRowClick }: StockTableProps) {
  const livePrices = useSelector((state: RootState) => state.ui.livePrices);

  return (
    <TableContainer component={Paper} variant="outlined">
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Symbol</TableCell>
            <TableCell>Company</TableCell>
            <TableCell>Sector</TableCell>
            <TableCell align="right">Price</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {stocks.map((stock) => {
            const live = livePrices[stock.symbol];
            const displayPrice = live?.price ?? stock.currentPrice;
            const displayChange = live?.changePercent ?? stock.changePercent;

            return (
              <TableRow key={stock.symbol} hover onClick={() => onRowClick(stock.symbol)} sx={{ cursor: 'pointer' }}>
                <TableCell sx={{ fontWeight: 600 }}>{stock.symbol}</TableCell>
                <TableCell>{stock.companyName}</TableCell>
                <TableCell>{stock.sector}</TableCell>
                <TableCell align="right">
                  <PriceTag price={displayPrice} changePercent={displayChange} size="small" />
                </TableCell>
              </TableRow>
            );
          })}
        </TableBody>
      </Table>
      <TablePagination
        component="div"
        count={totalElements}
        page={page}
        rowsPerPage={size}
        rowsPerPageOptions={[size]}
        onPageChange={(_, newPage) => onPageChange(newPage)}
      />
    </TableContainer>
  );
}
