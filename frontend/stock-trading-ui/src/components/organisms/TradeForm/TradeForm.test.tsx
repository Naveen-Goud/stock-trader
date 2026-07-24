import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { TradeForm } from './TradeForm';
import { tradingApi } from '../../../api/tradingApi';

vi.mock('../../../api/tradingApi');

function renderWithQueryClient(component: React.ReactNode) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(<QueryClientProvider client={queryClient}>{component}</QueryClientProvider>);
}

describe('TradeForm', () => {
  beforeEach(() => vi.clearAllMocks());

  it('renders BUY/SELL toggle defaulting to BUY', () => {
    renderWithQueryClient(<TradeForm symbol="AAPL" currentPrice={190} />);
    expect(screen.getByRole('button', { name: 'Buy', pressed: true })).toBeInTheDocument();
  });

  it('disables submit button when quantity is empty', () => {
    renderWithQueryClient(<TradeForm symbol="AAPL" currentPrice={190} />);
    expect(screen.getByRole('button', { name: /BUY AAPL/i })).toBeDisabled();
  });

  it('calculates estimated total correctly', () => {
    renderWithQueryClient(<TradeForm symbol="AAPL" currentPrice={190} />);
    fireEvent.change(screen.getByLabelText('Quantity'), { target: { value: '10' } });
    expect(screen.getByText(/Estimated Total: \$1900.00/)).toBeInTheDocument();
  });

  it('calls buy API on submit when BUY selected', async () => {
    (tradingApi.buy as any).mockResolvedValue({
      id: 1, symbol: 'AAPL', tradeType: 'BUY', quantity: 10,
      price: 190, totalAmount: 1900, status: 'EXECUTED', executedAt: '2026-06-16T10:00:00Z',
    });

    renderWithQueryClient(<TradeForm symbol="AAPL" currentPrice={190} />);
    fireEvent.change(screen.getByLabelText('Quantity'), { target: { value: '10' } });
    fireEvent.click(screen.getByRole('button', { name: /BUY AAPL/i }));

    await waitFor(() => expect(tradingApi.buy).toHaveBeenCalledTimes(1));
  });

  it('shows error alert when trade fails', async () => {
    (tradingApi.buy as any).mockRejectedValue({ response: { data: { message: 'Insufficient balance' } } });

    renderWithQueryClient(<TradeForm symbol="AAPL" currentPrice={190} />);
    fireEvent.change(screen.getByLabelText('Quantity'), { target: { value: '10' } });
    fireEvent.click(screen.getByRole('button', { name: /BUY AAPL/i }));

    await waitFor(() => expect(screen.getByText('Insufficient balance')).toBeInTheDocument());
  });
});
