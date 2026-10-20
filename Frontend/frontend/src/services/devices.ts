import { apiFetch } from './api';

export type Device = {
  id?: number;
  name: string;
  maximumConsumptionValue: number;
  userId?: number | null;
};

export async function listAllDevices(): Promise<Device[]> {
  return apiFetch('/devices');
}

export async function getDevicesByUser(userId: number): Promise<Device[]> {
  return apiFetch(`/devices/user/${userId}`);
}

export async function createDevice(d: Device): Promise<Device> {
  return apiFetch('/devices', { method: 'POST', body: JSON.stringify(d) });
}

export async function updateDevice(id: number, d: Device): Promise<Device> {
  return apiFetch(`/devices/${id}`, { method: 'PUT', body: JSON.stringify(d) });
}

export async function deleteDevice(id: number): Promise<void> {
  await apiFetch(`/devices/${id}`, { method: 'DELETE' });
}

