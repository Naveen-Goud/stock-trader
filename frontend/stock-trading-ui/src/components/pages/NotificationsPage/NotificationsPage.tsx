import { useEffect, useState } from 'react';
import { Box, Typography, List, Button, Tabs, Tab, Paper } from '@mui/material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useDispatch } from 'react-redux';
import { notificationApi } from '../../../api/notificationApi';
import { NotificationItem } from '../../molecules/NotificationItem/NotificationItem';
import { LoadingSpinner } from '../../atoms/LoadingSpinner/LoadingSpinner';
import { PriceAlertsPanel } from '../../organisms/PriceAlertsPanel/PriceAlertsPanel';
import { clearUnread } from '../../../store/notificationSlice';

export function NotificationsPage() {
  const [tab, setTab] = useState(0);
  const dispatch = useDispatch();
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ['notifications'],
    queryFn: () => notificationApi.getNotifications(0, 50),
  });

  const markAllReadMutation = useMutation({
    mutationFn: notificationApi.markAllRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      dispatch(clearUnread());
    },
  });

  const markOneReadMutation = useMutation({
    mutationFn: (id: number) => notificationApi.markOneRead(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['notifications'] }),
  });

  useEffect(() => {
    dispatch(clearUnread());
  }, [dispatch]);

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h5" fontWeight={700}>Notifications</Typography>
        {tab === 0 && (
          <Button onClick={() => markAllReadMutation.mutate()} disabled={markAllReadMutation.isPending}>
            Mark all as read
          </Button>
        )}
      </Box>

      <Paper variant="outlined" sx={{ mb: 3 }}>
        <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ px: 2 }}>
          <Tab label="Activity" />
          <Tab label="Price Alerts" />
        </Tabs>
      </Paper>

      {tab === 0 ? (
        isLoading ? (
          <LoadingSpinner />
        ) : data && data.notifications.length > 0 ? (
          <Paper variant="outlined">
            <List disablePadding>
              {data.notifications.map((n) => (
                <NotificationItem
                  key={n.id}
                  notification={n}
                  onClick={() => !n.isRead && markOneReadMutation.mutate(n.id)}
                />
              ))}
            </List>
          </Paper>
        ) : (
          <Paper variant="outlined" sx={{ p: 4, textAlign: 'center' }}>
            <Typography color="text.secondary">
              No notifications yet. Trades, price alerts, and portfolio milestones will show up here.
            </Typography>
          </Paper>
        )
      ) : (
        <PriceAlertsPanel />
      )}
    </Box>
  );
}
