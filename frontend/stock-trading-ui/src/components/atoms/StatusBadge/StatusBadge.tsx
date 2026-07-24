import { Chip } from '@mui/material';

interface StatusBadgeProps {
  status: string;
}

const statusColorMap: Record<string, 'success' | 'error' | 'warning' | 'default'> = {
  EXECUTED: 'success',
  FAILED: 'error',
  PENDING: 'warning',
  BUY: 'success',
  SELL: 'error',
};

export function StatusBadge({ status }: StatusBadgeProps) {
  return <Chip label={status} size="small" color={statusColorMap[status] ?? 'default'} variant="outlined" />;
}
