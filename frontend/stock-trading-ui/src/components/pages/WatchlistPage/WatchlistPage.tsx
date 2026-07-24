import { useState } from 'react';
import {
  Box, Typography, Button, Card, CardContent, CardHeader, IconButton,
  Dialog, DialogTitle, DialogContent, DialogActions, TextField, Chip, Stack, Paper,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { portfolioApi } from '../../../api/portfolioApi';
import { LoadingSpinner } from '../../atoms/LoadingSpinner/LoadingSpinner';
import { PriceTag } from '../../atoms/PriceTag/PriceTag';
import type { RootState } from '../../../store';

export function WatchlistPage() {
  const [createOpen, setCreateOpen] = useState(false);
  const [newName, setNewName] = useState('');
  const [addItemOpen, setAddItemOpen] = useState<number | null>(null);
  const [newSymbol, setNewSymbol] = useState('');
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const livePrices = useSelector((state: RootState) => state.ui.livePrices);

  const { data: watchlists, isLoading } = useQuery({
    queryKey: ['watchlists'],
    queryFn: portfolioApi.getWatchlists,
  });

  const createMutation = useMutation({
    mutationFn: (name: string) => portfolioApi.createWatchlist(name),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['watchlists'] });
      setCreateOpen(false);
      setNewName('');
    },
  });

  const addItemMutation = useMutation({
    mutationFn: ({ watchlistId, symbol }: { watchlistId: number; symbol: string }) =>
      portfolioApi.addWatchlistItem(watchlistId, symbol.toUpperCase()),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['watchlists'] });
      setAddItemOpen(null);
      setNewSymbol('');
    },
  });

  const removeItemMutation = useMutation({
    mutationFn: ({ watchlistId, symbol }: { watchlistId: number; symbol: string }) =>
      portfolioApi.removeWatchlistItem(watchlistId, symbol),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['watchlists'] }),
  });

  if (isLoading) return <LoadingSpinner />;

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5">Watchlists</Typography>
        <Button startIcon={<AddIcon />} variant="contained" onClick={() => setCreateOpen(true)}>
          New Watchlist
        </Button>
      </Box>

      {watchlists && watchlists.length === 0 ? (
        <Paper variant="outlined" sx={{ p: 4, textAlign: 'center' }}>
          <Typography color="text.secondary" mb={2}>
            You don't have any watchlists yet.
          </Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => setCreateOpen(true)}>
            Create your first watchlist
          </Button>
        </Paper>
      ) : (
        <Stack spacing={2}>
          {watchlists?.map((wl) => (
            <Card key={wl.id} variant="outlined">
              <CardHeader
                title={wl.name}
                titleTypographyProps={{ variant: 'subtitle1' }}
                action={
                  <IconButton onClick={() => setAddItemOpen(wl.id)} size="small">
                    <AddIcon fontSize="small" />
                  </IconButton>
                }
              />
              <CardContent sx={{ pt: 0 }}>
                <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                  {wl.items.length === 0 && (
                    <Typography variant="body2" color="text.secondary">No stocks added yet.</Typography>
                  )}
                  {wl.items.map((item) => {
                    const live = livePrices[item.symbol];
                    const displayPrice = live?.price ?? item.currentPrice;
                    const displayChange = live?.changePercent;
                    return (
                      <Chip
                        key={item.symbol}
                        onClick={() => navigate(`/market/${item.symbol}`)}
                        label={
                          <Box display="flex" alignItems="center" gap={1} py={0.5}>
                            <Typography fontWeight={700} fontSize="0.85rem">{item.symbol}</Typography>
                            <PriceTag price={displayPrice} changePercent={displayChange} size="small" />
                          </Box>
                        }
                        onDelete={() => removeItemMutation.mutate({ watchlistId: wl.id, symbol: item.symbol })}
                        deleteIcon={<DeleteIcon />}
                        variant="outlined"
                        sx={{ height: 'auto', py: 0.5, cursor: 'pointer' }}
                      />
                    );
                  })}
                </Stack>
              </CardContent>
            </Card>
          ))}
        </Stack>
      )}

      <Dialog open={createOpen} onClose={() => setCreateOpen(false)} fullWidth maxWidth="xs">
        <DialogTitle>Create Watchlist</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            label="Watchlist name"
            value={newName}
            onChange={(e) => setNewName(e.target.value)}
            margin="dense"
            autoFocus
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateOpen(false)}>Cancel</Button>
          <Button variant="contained" disabled={!newName.trim()} onClick={() => createMutation.mutate(newName)}>
            Create
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={addItemOpen !== null} onClose={() => setAddItemOpen(null)} fullWidth maxWidth="xs">
        <DialogTitle>Add Stock Symbol</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            label="Symbol (e.g. AAPL)"
            value={newSymbol}
            onChange={(e) => setNewSymbol(e.target.value.toUpperCase())}
            margin="dense"
            autoFocus
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAddItemOpen(null)}>Cancel</Button>
          <Button
            variant="contained"
            disabled={!newSymbol.trim()}
            onClick={() => addItemOpen !== null && addItemMutation.mutate({ watchlistId: addItemOpen, symbol: newSymbol })}
          >
            Add
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
