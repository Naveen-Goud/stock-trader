import { Box, Paper, Typography } from '@mui/material';
import CandlestickChartIcon from '@mui/icons-material/CandlestickChart';
import { Outlet } from 'react-router-dom';

export function AuthLayout() {
  return (
    <Box display="flex" minHeight="100vh">
      <Box
        flex={1}
        display={{ xs: 'none', md: 'flex' }}
        flexDirection="column"
        justifyContent="space-between"
        sx={{
          bgcolor: 'primary.dark',
          backgroundImage: 'linear-gradient(160deg, #0f172a 0%, #1e293b 100%)',
          color: '#fff',
          p: 6,
        }}
      >
        <Box display="flex" alignItems="center" gap={1.5}>
          <Box
            sx={{
              width: 38, height: 38, borderRadius: '10px', bgcolor: 'secondary.main',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
            }}
          >
            <CandlestickChartIcon sx={{ color: '#fff' }} />
          </Box>
          <Typography variant="h6" fontWeight={800} letterSpacing="-0.01em">StockSim</Typography>
        </Box>

        <Box maxWidth={420}>
          <Typography variant="h4" fontWeight={700} mb={2} letterSpacing="-0.02em">
            Practice trading with real-time market simulation.
          </Typography>
          <Typography variant="body1" sx={{ color: 'rgba(255,255,255,0.7)' }}>
            Track live prices, build a portfolio, and set price alerts — all with a
            risk-free virtual wallet.
          </Typography>
        </Box>

        <Typography variant="caption" sx={{ color: 'rgba(255,255,255,0.5)' }}>
          © {new Date().getFullYear()} StockSim. For educational use only.
        </Typography>
      </Box>

      <Box
        flex={1}
        display="flex"
        alignItems="center"
        justifyContent="center"
        bgcolor="background.default"
        p={3}
      >
        <Paper variant="outlined" sx={{ p: 4, borderRadius: 3, width: '100%', maxWidth: 400 }}>
          <Outlet />
        </Paper>
      </Box>
    </Box>
  );
}
