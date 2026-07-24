import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { PriceTag } from './PriceTag';

describe('PriceTag', () => {
  it('renders formatted price', () => {
    render(<PriceTag price={189.5} />);
    expect(screen.getByText('$189.50')).toBeInTheDocument();
  });

  it('shows positive change in green with up arrow', () => {
    render(<PriceTag price={189.5} changePercent={2.34} />);
    expect(screen.getByText(/▲/)).toBeInTheDocument();
    expect(screen.getByText(/2.34%/)).toBeInTheDocument();
  });

  it('shows negative change with down arrow', () => {
    render(<PriceTag price={189.5} changePercent={-1.2} />);
    expect(screen.getByText(/▼/)).toBeInTheDocument();
  });

  it('omits change indicator when changePercent not provided', () => {
    render(<PriceTag price={100} />);
    expect(screen.queryByText(/▲|▼/)).not.toBeInTheDocument();
  });
});
