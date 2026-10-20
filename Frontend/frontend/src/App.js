import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import './App.css';
import Login from './pages/Login';
import Register from './pages/Register';
import { useAuth } from './context/AuthContext';
import Topbar from './components/Topbar';
import DashboardUser from './pages/DashboardUser';
import DashboardAdmin from './pages/DashboardAdmin';
import HomeUser from './pages/HomeUser';
import EnergyConsumptionChart from './pages/EnergyConsumptionChart';
import NotificationListener from './components/NotificationListener';
import ChatSupport from './components/ChatSupport';

function Home() {
  const { role, token } = useAuth();
  const isAuthed = Boolean(token);

  if (isAuthed && role === 'ADMIN') {
    return <DashboardAdmin />;
  }
  if (isAuthed && role === 'USER') {
    return <HomeUser />;
  }

  return (
    <div className="container">
      <div className="hero">
        <h1>Device Manager</h1>
        <p>Gestionați dispozitivele cu ușurință.</p>
      </div>
    </div>
  );
}

const PrivateRoute = ({ element }) => {
  const { token } = useAuth();
  return token ? element : <Navigate to="/login" replace />;
};

const AdminRoute = ({ element }) => {
  const { token, role } = useAuth();
  return token && role === 'ADMIN' ? element : <Navigate to="/" replace />;
};

function App() {
  return (
    <div className="App">
      <Topbar />
      <NotificationListener />
      <ChatSupport />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/my-devices" element={<PrivateRoute element={<DashboardUser />} />} />
        <Route path="/energy-consumption" element={<PrivateRoute element={<EnergyConsumptionChart />} />} />
        <Route path="/admin" element={<AdminRoute element={<DashboardAdmin />} />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </div>
  );
}

export default App;
