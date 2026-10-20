// @ts-nocheck
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register } from '../services/auth';

const Register: React.FC = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    if (!firstName.trim() || !lastName.trim()) {
      setError('Introduceți prenume și nume.');
      return;
    }
    setLoading(true);
    try {
      await register({ email, password, firstName, lastName });
      setSuccess('Cont creat cu succes. Vă puteți autentifica.');
      setTimeout(() => navigate('/login'), 800);
    } catch (err: any) {
      setError(err.message || 'Register failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h2>Register</h2>
        {error && <div className="alert alert-error" style={{ marginBottom: 10 }}>{error}</div>}
        {success && <div className="alert alert-success" style={{ marginBottom: 10 }}>{success}</div>}

        <form className="form" onSubmit={onSubmit}>
          <div className="form-control">
            <label>First name</label>
            <input
              type="text"
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              required
            />
          </div>
          <div className="form-control">
            <label>Last name</label>
            <input
              type="text"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              required
            />
          </div>
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
              {loading ? 'Se crează…' : 'Register'}
            </button>
          </div>
        </form>

        <p style={{ marginTop: 12 }}>
          Aveți deja cont? <Link to="/login">Login</Link>
        </p>
      </div>
    </div>
  );
};

export default Register;
