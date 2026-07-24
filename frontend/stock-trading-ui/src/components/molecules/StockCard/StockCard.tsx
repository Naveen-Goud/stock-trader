import { Card, CardContent, Typography, Box } from '@mui/material';
import { PriceTag } from '../../atoms/PriceTag/PriceTag';
import type { Stock } from '../../../types/market.types';

interface StockCardProps {
  stock: Stock;
  onClick?: () => void;
}

export function StockCard({ stock, onClick }: StockCardProps) {
  return (
    <Card
      variant="outlined"
      onClick={onClick}
      sx={{
        cursor: onClick ? 'pointer' : 'default',
        transition: 'transform 0.15s ease, box-shadow 0.15s ease, border-color 0.15s ease',
        '&:hover': onClick ? {
          boxShadow: 3,
          borderColor: 'secondary.main',
          transform: 'translateY(-2px)',
        } : undefined,
      }}
    >
      <CardContent>
        <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={1}>
          <Box minWidth={0}>
            <Typography variant="subtitle1" fontWeight={700}>{stock.symbol}</Typography>
            <Typography variant="body2" color="text.secondary" noWrap maxWidth={160}>
              {stock.companyName}
            </Typography>
          </Box>
        </Box>
        <PriceTag price={stock.currentPrice} changePercent={stock.changePercent} />
      </CardContent>
    </Card>
  );
}
