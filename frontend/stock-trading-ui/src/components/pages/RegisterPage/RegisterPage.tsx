import { useState } from 'react';
import { Box, Button, Typography, Alert, Link as MuiLink } from '@mui/material';
import { Link, useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { FormField } from '../../molecules/FormField/FormField';
import { authApi } from '../../../api/authApi';

export function RegisterPage() {
  const [form, setForm] = useState({ username: '', email: '', password: '' });
  const navigate = useNavigate();

  const mutation = useMutation({
    mutationFn: () => authApi.register(form),
    onSuccess: () => navigate('/login'),
  });

  return (
    <Box component="form" onSubmit={(e) => { e.preventDefault(); mutation.mutate(); }}>
      <Typography variant="h5" mb={0.5}>Create your account</Typography>
      <Typography variant="body2" color="text.secondary" mb={3}>
        Start trading with a virtual $100,000 wallet
      </Typography>

      <FormField label="Username" value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} required autoFocus />
      <FormField label="Email" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
      <FormField
        label="Password"
        type="password"
        helperText="Min 8 characters, with uppercase, lowercase, digit, special character"
        value={form.password}
        onChange={(e) => setForm({ ...form, password: e.target.value })}
        required
      />

      {mutation.isError && (
        <Alert severity="error" sx={{ mt: 1 }}>
          {(mutation.error as any)?.response?.data?.message ?? 'Registration failed.'}
        </Alert>
      )}

      <Button fullWidth type="submit" variant="contained" size="large" sx={{ mt: 3 }} disabled={mutation.isPending}>
        {mutation.isPending ? 'Creating account…' : 'Sign Up'}
      </Button>

      <Typography variant="body2" mt={2.5} textAlign="center" color="text.secondary">
        Already have an account? <MuiLink component={Link} to="/login" fontWeight={600}>Sign in</MuiLink>
      </Typography>
    </Box>
  );
}
