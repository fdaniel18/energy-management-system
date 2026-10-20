import { apiFetch } from './api';

export type HourlyConsumption = {
  hour: number;
  consumptionValue: number;
};

export async function getDailyConsumption(
  deviceId: number,
  date: string
): Promise<HourlyConsumption[]> {
  return apiFetch(`/api/monitoring/daily-consumption/device/${deviceId}?date=${date}`);
}

