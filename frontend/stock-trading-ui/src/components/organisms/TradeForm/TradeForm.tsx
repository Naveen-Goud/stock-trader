import { useState } from 'react';
import { Box, Button, ToggleButtonGroup, ToggleButton, Alert, Typography } from '@mui/material';
import { FormField } from '../../molecules/FormField/FormField';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { tradingApi } from '../../../api/tradingApi';
import type { TradeType } from '../../../types/trading.types';
import { v4 as uuidv4 } from 'uuid';

interface TradeFormProps {
  symbol: string;
  currentPrice: number;
}

export function TradeForm({ symbol, currentPrice }: TradeFormProps) {
  const [tradeType, setTradeType] = useState<TradeType>('BUY');
  const [quantity, setQuantity] = useState('');
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: () => {
      const idempotencyKey = uuidv4();
      const qty = Number(quantity);
      return tradeType === 'BUY'
        ? tradingApi.buy({ symbol, quantity: qty }, idempotencyKey)
        : tradingApi.sell({ symbol, quantity: qty }, idempotencyKey);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['portfolio'] });
      queryClient.invalidateQueries({ queryKey: ['tradeHistory'] });
      queryClient.invalidateQueries({ queryKey: ['profile'] });
      // Wallet debit/credit is applied asynchronously (outbox -> Kafka ->
      // user-service), typically within ~1s. Refetch once more shortly after
      // so the balance shown doesn't require a manual refresh to catch up.
      setTimeout(() => {
        queryClient.invalidateQueries({ queryKey: ['profile'] });
        queryClient.invalidateQueries({ queryKey: ['portfolio'] });
      }, 1500);
      setQuantity('');
    },
  });

  const total = quantity ? Number(quantity) * currentPrice : 0;
  const isValidQuantity = Number(quantity) > 0 && Number.isInteger(Number(quantity));

  return (
    <Box>
      <ToggleButtonGroup value={tradeType} exclusive onChange={(_, val) => val && setTradeType(val)} fullWidth sx={{ mb: 2 }}>
        <ToggleButton value="BUY" color="success">Buy</ToggleButton>
        <ToggleButton value="SELL" color="error">Sell</ToggleButton>
      </ToggleButtonGroup>

      <FormField
        label="Quantity"
        type="number"
        value={quantity}
        onChange={(e) => setQuantity(e.target.value)}
        inputProps={{ min: 1, step: 1 }}
      />

      <Typography variant="body2" color="text.secondary" mt={1}>
        Estimated Total: ${total.toFixed(2)}
      </Typography>

      {mutation.isError && (
        <Alert severity="error" sx={{ mt: 2 }}>
          {(mutation.error as any)?.response?.data?.message ?? 'Trade failed. Please try again.'}
        </Alert>
      )}

      {mutation.isSuccess && <Alert severity="success" sx={{ mt: 2 }}>Trade executed successfully!</Alert>}

      <Button
        fullWidth
        variant="contained"
        color={tradeType === 'BUY' ? 'success' : 'error'}
        sx={{ mt: 2 }}
        disabled={!isValidQuantity || mutation.isPending}
        onClick={() => mutation.mutate()}
      >
        {mutation.isPending ? 'Processing...' : `${tradeType} ${symbol}`}
      </Button>
    </Box>
  );
}
