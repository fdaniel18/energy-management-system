// @ts-nocheck
import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Topbar: React.FC = () => {
  const { token, role, logout } = useAuth();
  const isAuthed = Boolean(token);
  const isAdmin = isAuthed && role === 'ADMIN';
  const isUser = isAuthed && role === 'USER';

  return (
    <div className="topbar">
      <div className="topbar-inner container">
        <div className="brand">
          <span className="title">Device Manager</span>
          <span className="badge">v0.1</span>
        </div>
        <nav className="nav">
          {!isAdmin && <Link to="/">Acasă</Link>}
          {isUser && <Link to="/my-devices">Device-urile mele</Link>}
          {(isUser || isAdmin) && <Link to="/energy-consumption">Consum energetic</Link>}
          {isAdmin && <Link to="/admin">Administrare</Link>}
          {!isAuthed && <Link to="/login">Login</Link>}
          {!isAuthed && <Link to="/register">Register</Link>}
          {isAuthed && (
            <button className="btn-logout" onClick={logout}>Logout</button>
          )}
        </nav>
      </div>
    </div>
  );
};

export default Topbar;
