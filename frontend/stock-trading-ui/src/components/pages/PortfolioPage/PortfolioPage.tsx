import { Box, Typography, Table, TableHead, TableRow, TableCell, TableBody, Paper, TableContainer } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { portfolioApi } from '../../../api/portfolioApi';
import { PortfolioSummaryPanel } from '../../organisms/PortfolioSummaryPanel/PortfolioSummaryPanel';
import { HoldingRow } from '../../molecules/HoldingRow/HoldingRow';
import { LoadingSpinner } from '../../atoms/LoadingSpinner/LoadingSpinner';

export function PortfolioPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['portfolio'],
    queryFn: portfolioApi.getPortfolio,
    refetchInterval: 15000,
  });

  if (isLoading) return <LoadingSpinner />;
  if (!data) return null;

  return (
    <Box>
      <Typography variant="h5" mb={3}>My Portfolio</Typography>

      <Box mb={4}>
        <PortfolioSummaryPanel summary={data} />
      </Box>

      <TableContainer component={Paper} variant="outlined">
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Symbol</TableCell>
              <TableCell align="right">Qty</TableCell>
              <TableCell align="right">Avg Buy Price</TableCell>
              <TableCell align="right">Current Price</TableCell>
              <TableCell align="right">Value</TableCell>
              <TableCell align="right">P&L</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {data.holdings.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  <Typography color="text.secondary" py={3}>
                    No holdings yet. Start trading to build your portfolio.
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              data.holdings.map((h) => <HoldingRow key={h.symbol} holding={h} />)
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
