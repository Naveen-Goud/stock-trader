import { useState } from 'react';
import { Box, Button, Typography, Alert, Link as MuiLink } from '@mui/material';
import { Link, useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { useDispatch } from 'react-redux';
import { FormField } from '../../molecules/FormField/FormField';
import { authApi } from '../../../api/authApi';
import { setTokens, setUser } from '../../../store/authSlice';

export function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const mutation = useMutation({
    mutationFn: () => authApi.login({ username, password }),
    onSuccess: async (data) => {
      dispatch(setTokens({ accessToken: data.accessToken, refreshToken: data.refreshToken }));
      const profile = await authApi.getProfile();
      dispatch(setUser(profile));
      navigate('/dashboard');
    },
  });

  return (
    <Box component="form" onSubmit={(e) => { e.preventDefault(); mutation.mutate(); }}>
      <Typography variant="h5" mb={0.5}>Welcome back</Typography>
      <Typography variant="body2" color="text.secondary" mb={3}>
        Sign in to your StockSim account
      </Typography>

      <FormField label="Username" value={username} onChange={(e) => setUsername(e.target.value)} required autoFocus />
      <FormField label="Password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />

      {mutation.isError && (
        <Alert severity="error" sx={{ mt: 1 }}>
          {(mutation.error as any)?.response?.data?.message ?? 'Invalid username or password.'}
        </Alert>
      )}

      <Button fullWidth type="submit" variant="contained" size="large" sx={{ mt: 3 }} disabled={mutation.isPending}>
        {mutation.isPending ? 'Signing in…' : 'Sign In'}
      </Button>

      <Typography variant="body2" mt={2.5} textAlign="center" color="text.secondary">
        Don't have an account? <MuiLink component={Link} to="/register" fontWeight={600}>Sign up</MuiLink>
      </Typography>
    </Box>
  );
}
