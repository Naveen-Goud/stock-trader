import { describe, it, expect } from 'vitest';
import { formatCurrency, formatPercent, formatRelativeTime } from './formatters';

describe('formatters', () => {
  it('formatCurrency formats USD correctly', () => {
    expect(formatCurrency(1900)).toBe('$1,900.00');
    expect(formatCurrency(0)).toBe('$0.00');
  });

  it('formatPercent adds + sign for positive values', () => {
    expect(formatPercent(2.5)).toBe('+2.50%');
    expect(formatPercent(-1.2)).toBe('-1.20%');
  });

  it('formatRelativeTime returns "just now" for recent timestamps', () => {
    const now = new Date().toISOString();
    expect(formatRelativeTime(now)).toBe('just now');
  });

  it('formatRelativeTime returns minutes for timestamps within the hour', () => {
    const fiveMinAgo = new Date(Date.now() - 5 * 60000).toISOString();
    expect(formatRelativeTime(fiveMinAgo)).toBe('5m ago');
  });
});
