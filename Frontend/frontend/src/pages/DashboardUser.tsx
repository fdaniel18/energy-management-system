// @ts-nocheck
import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { getDevicesByUser, type Device } from '../services/devices';

const DashboardUser: React.FC = () => {
  const { userId, email, token } = useAuth();
  const [devices, setDevices] = useState<Device[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!token || !userId) return;
    setLoading(true);
    setError(null);
    getDevicesByUser(userId)
      .then((d) => setDevices(d))
      .catch((e) => setError(e.message || 'Eroare la încărcarea device-urilor'))
      .finally(() => setLoading(false));
  }, [token, userId]);

  if (!token) return <div className="container"><div className="alert alert-error">Trebuie să vă autentificați.</div></div>;

  return (
    <div className="container">
      <div className="hero" style={{ textAlign: 'left', paddingTop: 24 }}>
        <h2>Device-urile mele</h2>
        <p className="note">Cont: {email}</p>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 12 }}>{error}</div>}
      {loading && <div>Se încarcă…</div>}

      <div className="panel">
        {devices && devices.length === 0 && <div className="note">Nu aveți device-uri asignate.</div>}

        {devices && devices.length > 0 && (
          <div style={{ overflowX: 'auto' }}>
            <table className="table table-striped">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Nume</th>
                  <th>Consum maxim</th>
                </tr>
              </thead>
              <tbody>
                {devices.map(d => (
                  <tr key={d.id}>
                    <td>{d.id}</td>
                    <td>{d.name}</td>
                    <td>{d.maximumConsumptionValue}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default DashboardUser;
