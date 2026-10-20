// @ts-nocheck
import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { getDevicesByUser, type Device } from '../services/devices';
import { getDailyConsumption, type HourlyConsumption } from '../services/monitoring';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import { Bar, Line } from 'react-chartjs-2';

// Register Chart.js components
ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

const EnergyConsumptionChart: React.FC = () => {
  const { userId, email, token } = useAuth();
  const [devices, setDevices] = useState<Device[] | null>(null);
  const [selectedDeviceId, setSelectedDeviceId] = useState<number | null>(null);
  const [selectedDate, setSelectedDate] = useState<string>(
    new Date().toISOString().split('T')[0]
  );
  const [chartType, setChartType] = useState<'bar' | 'line'>('bar');
  const [consumptionData, setConsumptionData] = useState<HourlyConsumption[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  // Load user's devices
  useEffect(() => {
    if (!token || !userId) return;
    setLoading(true);
    setError(null);
    getDevicesByUser(userId)
      .then((d) => {
        setDevices(d);
        if (d.length > 0) {
          setSelectedDeviceId(d[0].id!);
        }
      })
      .catch((e) => setError(e.message || 'Eroare la încărcarea device-urilor'))
      .finally(() => setLoading(false));
  }, [token, userId]);

  // Load consumption data when device or date changes
  useEffect(() => {
    if (!selectedDeviceId || !selectedDate) return;

    setLoading(true);
    setError(null);
    getDailyConsumption(selectedDeviceId, selectedDate)
      .then((data) => setConsumptionData(data))
      .catch((e) => setError(e.message || 'Eroare la încărcarea datelor de consum'))
      .finally(() => setLoading(false));
  }, [selectedDeviceId, selectedDate]);

  if (!token) {
    return (
      <div className="container">
        <div className="alert alert-error">Trebuie să vă autentificați.</div>
      </div>
    );
  }

  // Prepare chart data
  const chartData = {
    labels: Array.from({ length: 24 }, (_, i) => `${i}:00`),
    datasets: [
      {
        label: 'Consum energetic (kWh)',
        data: consumptionData?.map((c) => c.consumptionValue) || Array(24).fill(0),
        backgroundColor: 'rgba(75, 192, 192, 0.6)',
        borderColor: 'rgba(75, 192, 192, 1)',
        borderWidth: 2,
        fill: false,
      },
    ],
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top' as const,
      },
      title: {
        display: true,
        text: `Consum energetic pe ore - ${selectedDate}`,
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        title: {
          display: true,
          text: 'Consum (kWh)',
        },
      },
      x: {
        title: {
          display: true,
          text: 'Ora zilei',
        },
      },
    },
  };

  const totalConsumption = consumptionData?.reduce((sum, c) => sum + c.consumptionValue, 0) || 0;
  const selectedDevice = devices?.find((d) => d.id === selectedDeviceId);

  return (
    <div className="container">
      <div className="hero" style={{ textAlign: 'left', paddingTop: 24 }}>
        <h2>Consum energetic istoric</h2>
        <p className="note">Cont: {email}</p>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 12 }}>{error}</div>}

      <div className="panel" style={{ marginBottom: 20 }}>
        <h3>Selectați device-ul și data</h3>
        <div style={{ display: 'flex', gap: '16px', flexWrap: 'wrap', marginTop: 16 }}>
          <div style={{ flex: '1 1 200px' }}>
            <label htmlFor="device-select" style={{ display: 'block', marginBottom: 8 }}>
              Device:
            </label>
            <select
              id="device-select"
              value={selectedDeviceId || ''}
              onChange={(e) => setSelectedDeviceId(Number(e.target.value))}
              disabled={!devices || devices.length === 0}
              style={{ width: '100%', padding: '8px', fontSize: '14px' }}
            >
              {devices?.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.name}
                </option>
              ))}
            </select>
          </div>

          <div style={{ flex: '1 1 200px' }}>
            <label htmlFor="date-select" style={{ display: 'block', marginBottom: 8 }}>
              Data:
            </label>
            <input
              id="date-select"
              type="date"
              value={selectedDate}
              onChange={(e) => setSelectedDate(e.target.value)}
              max={new Date().toISOString().split('T')[0]}
              style={{ width: '100%', padding: '8px', fontSize: '14px' }}
            />
          </div>

          <div style={{ flex: '1 1 200px' }}>
            <label htmlFor="chart-type" style={{ display: 'block', marginBottom: 8 }}>
              Tip grafic:
            </label>
            <select
              id="chart-type"
              value={chartType}
              onChange={(e) => setChartType(e.target.value as 'bar' | 'line')}
              style={{ width: '100%', padding: '8px', fontSize: '14px' }}
            >
              <option value="bar">Grafic cu bare</option>
              <option value="line">Grafic cu linii</option>
            </select>
          </div>
        </div>
      </div>

      {loading && <div>Se încarcă datele...</div>}

      {!loading && consumptionData && selectedDevice && (
        <>
          <div className="panel" style={{ marginBottom: 20 }}>
            <h3>Statistici</h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px', marginTop: 16 }}>
              <div>
                <strong>Device:</strong> {selectedDevice.name}
              </div>
              <div>
                <strong>Consum total:</strong> {totalConsumption.toFixed(2)} kWh
              </div>
              <div>
                <strong>Consum maxim:</strong> {selectedDevice.maximumConsumptionValue} kWh/h
              </div>
              <div>
                <strong>Medie pe oră:</strong> {(totalConsumption / 24).toFixed(2)} kWh
              </div>
            </div>
          </div>

          <div className="panel">
            <div style={{ height: '500px', position: 'relative' }}>
              {chartType === 'bar' ? (
                <Bar data={chartData} options={chartOptions} />
              ) : (
                <Line data={chartData} options={chartOptions} />
              )}
            </div>
          </div>
        </>
      )}

      {!loading && devices && devices.length === 0 && (
        <div className="panel">
          <div className="note">Nu aveți device-uri asignate.</div>
        </div>
      )}
    </div>
  );
};

export default EnergyConsumptionChart;

