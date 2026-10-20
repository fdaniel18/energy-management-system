import { apiFetch } from './api';

export type User = {
  id?: number;
  email: string;
  firstName: string;
  lastName: string;
  password?: string; // Added for user creation
};

export async function listUsers(): Promise<User[]> {
  return apiFetch('/users');
}

export async function createUser(u: User): Promise<User> {
  // Use the auth/register endpoint to properly create user with credentials
  const registerPayload = {
    email: u.email,
    password: u.password,
    firstName: u.firstName,
    lastName: u.lastName
  };
  return apiFetch('/auth/register', { method: 'POST', body: JSON.stringify(registerPayload) });
}

export async function updateUser(id: number, u: User): Promise<User> {
  return apiFetch(`/users/${id}`, { method: 'PUT', body: JSON.stringify(u) });
}

export async function deleteUser(id: number): Promise<void> {
  await apiFetch(`/users/${id}`, { method: 'DELETE' });
}
