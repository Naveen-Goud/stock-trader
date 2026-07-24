import { Box, Typography } from '@mui/material';
import { formatCurrency, formatPercent } from '../../../utils/formatters';

interface PriceTagProps {
  price: number;
  changePercent?: number;
  size?: 'small' | 'medium' | 'large';
}

export function PriceTag({ price, changePercent, size = 'medium' }: PriceTagProps) {
  const isPositive = (changePercent ?? 0) >= 0;
  const fontSize = size === 'large' ? '1.5rem' : size === 'small' ? '0.875rem' : '1rem';

  return (
    <Box display="flex" alignItems="baseline" gap={1}>
      <Typography fontWeight={600} fontSize={fontSize}>
        {formatCurrency(price)}
      </Typography>
      {changePercent !== undefined && (
        <Typography fontSize="0.875rem" color={isPositive ? 'success.main' : 'error.main'} fontWeight={500}>
          {isPositive ? '▲' : '▼'} {formatPercent(Math.abs(changePercent))}
        </Typography>
      )}
    </Box>
  );
}
