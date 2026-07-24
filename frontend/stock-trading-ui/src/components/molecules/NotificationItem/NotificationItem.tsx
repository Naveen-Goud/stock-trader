import { ListItem, ListItemText, ListItemIcon, Box, Typography } from '@mui/material';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import NotificationsActiveIcon from '@mui/icons-material/NotificationsActive';
import AccountBalanceIcon from '@mui/icons-material/AccountBalance';
import type { Notification } from '../../../types/notification.types';
import { formatRelativeTime } from '../../../utils/formatters';

interface NotificationItemProps {
  notification: Notification;
  onClick?: () => void;
}

const iconMap = {
  TRADE_EXECUTED: <TrendingUpIcon color="primary" />,
  PRICE_ALERT: <NotificationsActiveIcon color="warning" />,
  PORTFOLIO_MILESTONE: <AccountBalanceIcon color="success" />,
  SYSTEM: <NotificationsActiveIcon color="action" />,
};

export function NotificationItem({ notification, onClick }: NotificationItemProps) {
  return (
    <ListItem onClick={onClick} sx={{ cursor: 'pointer', bgcolor: notification.isRead ? 'transparent' : 'action.hover', borderRadius: 1 }}>
      <ListItemIcon>{iconMap[notification.type]}</ListItemIcon>
      <ListItemText
        primary={notification.message}
        secondary={
          <Box component="span" display="flex" gap={1}>
            <Typography variant="caption" color="text.secondary">
              {formatRelativeTime(notification.createdAt)}
            </Typography>
          </Box>
        }
      />
    </ListItem>
  );
}
