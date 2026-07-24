import { Box, Drawer, List, ListItemButton, ListItemIcon, ListItemText, Toolbar, Typography, Divider } from '@mui/material';
import DashboardIcon from '@mui/icons-material/Dashboard';
import ShowChartIcon from '@mui/icons-material/ShowChart';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';
import StarIcon from '@mui/icons-material/Star';
import HistoryIcon from '@mui/icons-material/History';
import CandlestickChartIcon from '@mui/icons-material/CandlestickChart';
import { NavigationBar } from '../../organisms/NavigationBar/NavigationBar';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';

const DRAWER_WIDTH = 232;

const navItems = [
  { label: 'Dashboard', path: '/dashboard', icon: <DashboardIcon /> },
  { label: 'Market', path: '/market', icon: <ShowChartIcon /> },
  { label: 'Portfolio', path: '/portfolio', icon: <AccountBalanceWalletIcon /> },
  { label: 'Watchlist', path: '/watchlist', icon: <StarIcon /> },
  { label: 'History', path: '/history', icon: <HistoryIcon /> },
];

export function DashboardLayout() {
  const navigate = useNavigate();
  const location = useLocation();

  return (
    <Box display="flex" minHeight="100vh">
      <Drawer
        variant="permanent"
        sx={{ width: DRAWER_WIDTH, flexShrink: 0, ['& .MuiDrawer-paper']: { width: DRAWER_WIDTH, border: 'none' } }}
      >
        <Toolbar sx={{ px: 3, minHeight: '72px !important' }}>
          <Box display="flex" alignItems="center" gap={1.25}>
            <Box
              sx={{
                width: 34, height: 34, borderRadius: '9px',
                bgcolor: 'secondary.main', display: 'flex', alignItems: 'center', justifyContent: 'center',
              }}
            >
              <CandlestickChartIcon sx={{ fontSize: 20, color: '#fff' }} />
            </Box>
            <Typography variant="subtitle1" fontWeight={800} color="#fff" letterSpacing="-0.01em">
              StockSim
            </Typography>
          </Box>
        </Toolbar>

        <Divider sx={{ borderColor: 'rgba(255,255,255,0.08)', mx: 2, mb: 1 }} />

        <List sx={{ px: 0 }}>
          {navItems.map((item) => {
            const selected = location.pathname === item.path || location.pathname.startsWith(item.path + '/');
            return (
              <ListItemButton key={item.path} selected={selected} onClick={() => navigate(item.path)}>
                <ListItemIcon>{item.icon}</ListItemIcon>
                <ListItemText primary={item.label} primaryTypographyProps={{ fontSize: '0.9rem', fontWeight: 600 }} />
              </ListItemButton>
            );
          })}
        </List>
      </Drawer>

      <Box flexGrow={1} display="flex" flexDirection="column" minWidth={0}>
        <NavigationBar />
        <Box p={{ xs: 2, md: 4 }} flexGrow={1}>
          <Outlet />
        </Box>
      </Box>
    </Box>
  );
}
