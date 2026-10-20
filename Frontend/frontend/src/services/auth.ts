// Types and API calls for authentication
import { apiFetch } from './api';

export type LoginRequest = { email: string; password: string };
export type LoginResponse = {
  message: string;
  email: string;
  role: string;
  userId: number | null;
  token: string;
};

export type RegisterRequest = {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
};

export type RegisterResponse = {
  message: string;
  user: {
    id: number;
    userId: number;
    email: string;
    role: string;
  };
};

export async function login(req: LoginRequest): Promise<LoginResponse> {
  const data = await apiFetch('/auth/login', {
    method: 'POST',
    body: JSON.stringify(req),
  });
  return data as LoginResponse;
}

export async function register(req: RegisterRequest): Promise<RegisterResponse> {
  const data = await apiFetch('/auth/register', {
    method: 'POST',
    body: JSON.stringify(req),
  });
  return data as RegisterResponse;
}
