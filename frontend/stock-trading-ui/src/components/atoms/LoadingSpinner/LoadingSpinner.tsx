import { Box, CircularProgress } from '@mui/material';

export function LoadingSpinner({ size = 40 }: { size?: number }) {
  return (
    <Box display="flex" justifyContent="center" alignItems="center" py={4}>
      <CircularProgress size={size} />
    </Box>
  );
}
