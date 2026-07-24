import { AppBar, Toolbar, Typography, Box, IconButton, Badge, Avatar, Menu, MenuItem, Chip, Divider } from '@mui/material';
import NotificationsIcon from '@mui/icons-material/Notifications';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';
import { useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { useState } from 'react';
import type { RootState } from '../../../store';
import { logout } from '../../../store/authSlice';
import { useProfile } from '../../../hooks/useProfile';
import { formatCurrency } from '../../../utils/formatters';

export function NavigationBar() {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const user = useSelector((state: RootState) => state.auth.user);
  const unreadCount = useSelector((state: RootState) => state.notifications.unreadCount);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

  // Keeps user.walletBalance current: refetches on mount/refresh and again
  // shortly after every trade (see TradeForm), instead of freezing at the
  // value returned at login.
  useProfile();

  return (
    <AppBar position="sticky" elevation={0} color="default">
      <Toolbar sx={{ justifyContent: 'flex-end', gap: 1, minHeight: 64 }}>
        {user && (
          <Chip
            icon={<AccountBalanceWalletIcon sx={{ fontSize: 18 }} />}
            label={formatCurrency(user.walletBalance)}
            variant="outlined"
            sx={{ fontWeight: 700, borderColor: 'divider', mr: 1 }}
          />
        )}

        <IconButton onClick={() => navigate('/notifications')}>
          <Badge badgeContent={unreadCount} color="error">
            <NotificationsIcon />
          </Badge>
        </IconButton>

        <IconButton onClick={(e) => setAnchorEl(e.currentTarget)}>
          <Avatar sx={{ width: 34, height: 34, bgcolor: 'primary.main', fontSize: '0.9rem', fontWeight: 700 }}>
            {user?.username?.charAt(0).toUpperCase()}
          </Avatar>
        </IconButton>

        <Menu anchorEl={anchorEl} open={!!anchorEl} onClose={() => setAnchorEl(null)}>
          <MenuItem disabled sx={{ opacity: '1 !important' }}>
            <Box>
              <Typography variant="body2" fontWeight={700}>{user?.username}</Typography>
              <Typography variant="caption" color="text.secondary">{user?.email}</Typography>
            </Box>
          </MenuItem>
          <Divider />
          <MenuItem onClick={() => { dispatch(logout()); navigate('/login'); }}>Log out</MenuItem>
        </Menu>
      </Toolbar>
    </AppBar>
  );
}
