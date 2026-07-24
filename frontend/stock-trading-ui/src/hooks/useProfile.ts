import { useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useDispatch, useSelector } from 'react-redux';
import { authApi } from '../api/authApi';
import { setUser } from '../store/authSlice';
import type { RootState } from '../store';

/**
 * Keeps the Redux `auth.user` snapshot (username, wallet balance, etc.) in
 * sync with the server. Without this, the wallet balance shown around the
 * app only ever reflected the value from the moment of login: it never
 * updated after a trade, and disappeared entirely after a page refresh
 * (Redux state resets, but the access token in localStorage keeps the user
 * "logged in").
 */
export function useProfile() {
  const dispatch = useDispatch();
  const isAuthenticated = useSelector((state: RootState) => state.auth.isAuthenticated);

  const query = useQuery({
    queryKey: ['profile'],
    queryFn: authApi.getProfile,
    enabled: isAuthenticated,
    staleTime: 0,
  });

  useEffect(() => {
    if (query.data) {
      dispatch(setUser(query.data));
    }
  }, [query.data, dispatch]);

  return query;
}
