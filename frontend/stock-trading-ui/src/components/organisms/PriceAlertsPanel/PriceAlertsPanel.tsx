import { useState } from 'react';
import {
  Box, Typography, Button, TextField, MenuItem, Stack, Chip, IconButton,
  Paper, Divider,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import NotificationsActiveIcon from '@mui/icons-material/NotificationsActive';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { notificationApi } from '../../../api/notificationApi';
import { formatCurrency, formatDateTime } from '../../../utils/formatters';

interface PriceAlertsPanelProps {
  /** Pre-fill the symbol field, e.g. when embedded on a stock detail page. */
  defaultSymbol?: string;
}

export function PriceAlertsPanel({ defaultSymbol }: PriceAlertsPanelProps) {
  const [symbol, setSymbol] = useState(defaultSymbol ?? '');
  const [alertType, setAlertType] = useState<'ABOVE' | 'BELOW'>('ABOVE');
  const [targetPrice, setTargetPrice] = useState('');
  const queryClient = useQueryClient();

  const { data: alerts, isLoading } = useQuery({
    queryKey: ['priceAlerts'],
    queryFn: notificationApi.getAlerts,
  });

  const createMutation = useMutation({
    mutationFn: () => notificationApi.createAlert(symbol.toUpperCase(), alertType, Number(targetPrice)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['priceAlerts'] });
      setTargetPrice('');
      if (!defaultSymbol) setSymbol('');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => notificationApi.deleteAlert(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['priceAlerts'] }),
  });

  const isValid = /^[A-Z]{1,10}$/.test(symbol.toUpperCase()) && Number(targetPrice) > 0;
  const activeAlerts = alerts?.filter((a) => a.isActive) ?? [];
  const triggeredAlerts = alerts?.filter((a) => !a.isActive) ?? [];

  return (
    <Paper variant="outlined" sx={{ p: 3 }}>
      <Box display="flex" alignItems="center" gap={1} mb={2}>
        <NotificationsActiveIcon color="secondary" fontSize="small" />
        <Typography variant="subtitle1">Price Alerts</Typography>
      </Box>

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5} mb={2}>
        {!defaultSymbol && (
          <TextField
            label="Symbol"
            size="small"
            value={symbol}
            onChange={(e) => setSymbol(e.target.value.toUpperCase())}
            sx={{ width: { xs: '100%', sm: 120 } }}
          />
        )}
        <TextField
          select
          label="When price is"
          size="small"
          value={alertType}
          onChange={(e) => setAlertType(e.target.value as 'ABOVE' | 'BELOW')}
          sx={{ width: { xs: '100%', sm: 160 } }}
        >
          <MenuItem value="ABOVE">Above</MenuItem>
          <MenuItem value="BELOW">Below</MenuItem>
        </TextField>
        <TextField
          label="Target price ($)"
          size="small"
          type="number"
          value={targetPrice}
          onChange={(e) => setTargetPrice(e.target.value)}
          sx={{ width: { xs: '100%', sm: 160 } }}
        />
        <Button
          variant="contained"
          color="secondary"
          disabled={!isValid || createMutation.isPending}
          onClick={() => createMutation.mutate()}
          sx={{ whiteSpace: 'nowrap' }}
        >
          Create Alert
        </Button>
      </Stack>

      <Divider sx={{ mb: 2 }} />

      {isLoading ? (
        <Typography variant="body2" color="text.secondary">Loading alerts…</Typography>
      ) : activeAlerts.length === 0 && triggeredAlerts.length === 0 ? (
        <Typography variant="body2" color="text.secondary">
          No price alerts yet. Create one above to get notified when a stock crosses your target.
        </Typography>
      ) : (
        <Stack spacing={1}>
          {activeAlerts.map((alert) => (
            <Box
              key={alert.id}
              display="flex"
              alignItems="center"
              justifyContent="space-between"
              sx={{ p: 1, borderRadius: 1, bgcolor: 'background.default' }}
            >
              <Box display="flex" alignItems="center" gap={1}>
                <Typography fontWeight={700}>{alert.symbol}</Typography>
                <Chip
                  label={alert.alertType === 'ABOVE' ? 'Above' : 'Below'}
                  size="small"
                  color={alert.alertType === 'ABOVE' ? 'success' : 'error'}
                  variant="outlined"
                />
                <Typography variant="body2" color="text.secondary">
                  {formatCurrency(alert.targetPrice)}
                </Typography>
              </Box>
              <IconButton size="small" onClick={() => deleteMutation.mutate(alert.id)}>
                <DeleteIcon fontSize="small" />
              </IconButton>
            </Box>
          ))}

          {triggeredAlerts.length > 0 && (
            <>
              <Typography variant="caption" color="text.secondary" mt={1}>
                Triggered
              </Typography>
              {triggeredAlerts.map((alert) => (
                <Box
                  key={alert.id}
                  display="flex"
                  alignItems="center"
                  justifyContent="space-between"
                  sx={{ p: 1, borderRadius: 1, opacity: 0.6 }}
                >
                  <Box display="flex" alignItems="center" gap={1}>
                    <Typography fontWeight={700}>{alert.symbol}</Typography>
                    <Typography variant="body2" color="text.secondary">
                      {alert.alertType === 'ABOVE' ? 'above' : 'below'} {formatCurrency(alert.targetPrice)}
                      {alert.triggeredAt ? ` · triggered ${formatDateTime(alert.triggeredAt)}` : ''}
                    </Typography>
                  </Box>
                  <IconButton size="small" onClick={() => deleteMutation.mutate(alert.id)}>
                    <DeleteIcon fontSize="small" />
                  </IconButton>
                </Box>
              ))}
            </>
          )}
        </Stack>
      )}
    </Paper>
  );
}
