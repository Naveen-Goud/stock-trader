import { useState } from 'react';
import { Box, Typography, TextField, MenuItem, InputAdornment, Paper } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { marketApi } from '../../../api/marketApi';
import { StockTable } from '../../organisms/StockTable/StockTable';
import { LoadingSpinner } from '../../atoms/LoadingSpinner/LoadingSpinner';
import { useDebounce } from '../../../hooks/useDebounce';

export function StockMarketPage() {
  const [page, setPage] = useState(0);
  const [sector, setSector] = useState('');
  const [search, setSearch] = useState('');
  const navigate = useNavigate();
  const debouncedSearch = useDebounce(search, 400);

  const { data: sectors } = useQuery({ queryKey: ['sectors'], queryFn: marketApi.getSectors });

  const { data, isLoading } = useQuery({
    queryKey: ['stocks', page, sector, debouncedSearch],
    queryFn: () =>
      debouncedSearch
        ? marketApi.search(debouncedSearch, page, 20)
        : marketApi.getStocks(page, 20, sector || undefined),
  });

  return (
    <Box>
      <Typography variant="h5" mb={3}>Stock Market</Typography>

      <Paper variant="outlined" sx={{ p: 2, mb: 3 }}>
        <Box display="flex" gap={2} flexWrap="wrap">
          <TextField
            placeholder="Search by symbol or company"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            size="small"
            sx={{ flexGrow: 1, minWidth: 220 }}
            InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment> }}
          />
          <TextField select label="Sector" value={sector} onChange={(e) => setSector(e.target.value)} size="small" sx={{ minWidth: 180 }}>
            <MenuItem value="">All Sectors</MenuItem>
            {sectors?.map((s) => <MenuItem key={s} value={s}>{s}</MenuItem>)}
          </TextField>
        </Box>
      </Paper>

      {isLoading ? (
        <LoadingSpinner />
      ) : data && data.stocks.length === 0 ? (
        <Paper variant="outlined" sx={{ p: 4, textAlign: 'center' }}>
          <Typography color="text.secondary">No stocks match your search.</Typography>
        </Paper>
      ) : data ? (
        <StockTable
          stocks={data.stocks}
          totalElements={data.totalElements}
          page={page}
          size={20}
          onPageChange={setPage}
          onRowClick={(symbol) => navigate(`/market/${symbol}`)}
        />
      ) : null}
    </Box>
  );
}
