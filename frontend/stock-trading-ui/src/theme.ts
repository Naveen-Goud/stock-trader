import { createTheme } from '@mui/material';

// Muted, professional fintech palette: deep slate-navy for primary chrome,
// a restrained teal accent, and desaturated gain/loss colors so the UI reads
// as calm and trustworthy rather than flashy.
const navy = {
  900: '#0f172a',
  800: '#1e293b',
  700: '#334155',
  600: '#475569',
  500: '#64748b',
  300: '#cbd5e1',
  100: '#f1f5f9',
  50: '#f8fafc',
};

const teal = '#0d7a71';
const gain = '#1b7a4a';
const loss = '#a13939';

export const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: navy[800], light: navy[600], dark: navy[900], contrastText: '#fff' },
    secondary: { main: teal, contrastText: '#fff' },
    success: { main: gain },
    error: { main: loss },
    warning: { main: '#9a6a1a' },
    background: { default: navy[50], paper: '#ffffff' },
    text: { primary: navy[900], secondary: navy[600] },
    divider: navy[100],
  },
  shape: { borderRadius: 10 },
  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica Neue", sans-serif',
    h4: { fontWeight: 700, letterSpacing: '-0.02em' },
    h5: { fontWeight: 700, letterSpacing: '-0.01em' },
    h6: { fontWeight: 600 },
    subtitle1: { fontWeight: 600 },
    button: { fontWeight: 600, textTransform: 'none' },
  },
  shadows: [
    'none',
    '0 1px 2px rgba(15,23,42,0.06)',
    '0 1px 3px rgba(15,23,42,0.08)',
    '0 2px 6px rgba(15,23,42,0.08)',
    '0 2px 8px rgba(15,23,42,0.10)',
    '0 4px 10px rgba(15,23,42,0.10)',
    '0 4px 12px rgba(15,23,42,0.10)',
    '0 6px 16px rgba(15,23,42,0.12)',
    '0 6px 18px rgba(15,23,42,0.12)',
    '0 8px 20px rgba(15,23,42,0.12)',
    '0 8px 22px rgba(15,23,42,0.14)',
    '0 10px 24px rgba(15,23,42,0.14)',
    '0 10px 26px rgba(15,23,42,0.14)',
    '0 12px 28px rgba(15,23,42,0.16)',
    '0 12px 30px rgba(15,23,42,0.16)',
    '0 14px 32px rgba(15,23,42,0.16)',
    '0 14px 34px rgba(15,23,42,0.18)',
    '0 16px 36px rgba(15,23,42,0.18)',
    '0 16px 38px rgba(15,23,42,0.18)',
    '0 18px 40px rgba(15,23,42,0.20)',
    '0 18px 42px rgba(15,23,42,0.20)',
    '0 20px 44px rgba(15,23,42,0.20)',
    '0 20px 46px rgba(15,23,42,0.22)',
    '0 22px 48px rgba(15,23,42,0.22)',
    '0 22px 50px rgba(15,23,42,0.22)',
  ],
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: { backgroundColor: navy[50] },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: { backgroundImage: 'none' },
        outlined: { borderColor: navy[100] },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          border: `1px solid ${navy[100]}`,
          boxShadow: '0 1px 2px rgba(15,23,42,0.04)',
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: { borderRadius: 8, paddingTop: 9, paddingBottom: 9 },
        contained: { boxShadow: 'none', '&:hover': { boxShadow: 'none' } },
      },
    },
    MuiChip: {
      styleOverrides: { root: { fontWeight: 600 } },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: '#ffffff',
          borderBottom: `1px solid ${navy[100]}`,
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          backgroundColor: navy[900],
          color: navy[100],
          borderRight: 'none',
        },
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          margin: '2px 10px',
          color: navy[300],
          '&.Mui-selected': {
            backgroundColor: 'rgba(255,255,255,0.08)',
            color: '#ffffff',
          },
          '&.Mui-selected:hover': { backgroundColor: 'rgba(255,255,255,0.12)' },
          '&:hover': { backgroundColor: 'rgba(255,255,255,0.06)' },
        },
      },
    },
    MuiListItemIcon: {
      styleOverrides: { root: { color: 'inherit', minWidth: 40 } },
    },
    MuiTableCell: {
      styleOverrides: {
        head: {
          fontWeight: 700,
          fontSize: '0.75rem',
          letterSpacing: '0.04em',
          textTransform: 'uppercase',
          color: navy[600],
          backgroundColor: navy[50],
          borderBottom: `1px solid ${navy[100]}`,
        },
        body: { borderBottom: `1px solid ${navy[100]}` },
      },
    },
  },
});
