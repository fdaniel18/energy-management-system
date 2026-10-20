// @ts-nocheck
import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { login } from '../services/auth';
import { useAuth } from '../context/AuthContext';

const Login: React.FC = () => {
  const { setAuth } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const res = await login({ email, password });
      setAuth({ token: res.token, email: res.email, role: res.role, userId: res.userId });
      navigate('/');
    } catch (err: any) {
      setError(err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h2>Login</h2>
        <p className="note">Autentificați-vă pentru a accesa Device Manager.</p>

        {error && <div className="alert alert-error" style={{ marginBottom: 10 }}>{error}</div>}

        <form className="form" onSubmit={onSubmit}>
          <div className="form-control">
            <label>Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>
          <div className="form-control">
            <label>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          <div className="actions">
            <button type="submit" disabled={loading} className="btn btn-primary btn-block">
              {loading ? 'Se conectează…' : 'Login'}
            </button>
          </div>
        </form>

        <p style={{ marginTop: 12 }}>
          Nu ai cont? <Link to="/register">Creează unul</Link>
        </p>
      </div>
    </div>
  );
};

export default Login;
