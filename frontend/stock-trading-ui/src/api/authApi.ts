import { axiosClient } from './axiosClient';
import type { LoginRequest, RegisterRequest, AuthResponse, User } from '../types/auth.types';

export const authApi = {
  register: (data: RegisterRequest) =>
    axiosClient.post<User>('/api/users/register', data).then((r) => r.data),
  login: (data: LoginRequest) =>
    axiosClient.post<AuthResponse>('/api/users/login', data).then((r) => r.data),
  getProfile: () =>
    axiosClient.get<User>('/api/users/me').then((r) => r.data),
};
