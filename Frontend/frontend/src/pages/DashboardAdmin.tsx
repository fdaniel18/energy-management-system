// @ts-nocheck
import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { listUsers, createUser, updateUser, deleteUser, type User } from '../services/users';
import { listAllDevices, createDevice, updateDevice, deleteDevice, type Device } from '../services/devices';
import Modal from '../components/Modal';
import AdminChat from '../components/AdminChat';

const emptyUser: User = { email: '', firstName: '', lastName: '', password: '' };
const emptyDevice: Device = { name: '', maximumConsumptionValue: 0, userId: null };

type TabKey = 'users' | 'devices' | 'chat';

const DashboardAdmin: React.FC = () => {
  const { token, email } = useAuth();
  const [activeTab, setActiveTab] = useState<TabKey>('users');
  const [users, setUsers] = useState<User[]>([]);
  const [devices, setDevices] = useState<Device[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  // Modals state
  const [userModalOpen, setUserModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [userForm, setUserForm] = useState<User>(emptyUser);

  const [deviceModalOpen, setDeviceModalOpen] = useState(false);
  const [editingDevice, setEditingDevice] = useState<Device | null>(null);
  const [deviceForm, setDeviceForm] = useState<Device>(emptyDevice);

  const loadAll = async () => {
    setLoading(true);
    setError(null);
    try {
      const [u, d] = await Promise.all([listUsers(), listAllDevices()]);
      setUsers(u);
      setDevices(d);
    } catch (e: any) {
      setError(e.message || 'Eroare la încărcarea datelor');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!token) return;
    loadAll();
  }, [token]);

  // User modal handlers
  const openCreateUser = () => {
    setEditingUser(null);
    setUserForm(emptyUser);
    setUserModalOpen(true);
  };
  const openEditUser = (u: User) => {
    setEditingUser(u);
    setUserForm({ id: u.id, email: u.email, firstName: u.firstName, lastName: u.lastName });
    setUserModalOpen(true);
  };
  const submitUserModal = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingUser && editingUser.id) {
        // When editing, don't send password
        const { password, ...userWithoutPassword } = userForm;
        await updateUser(editingUser.id, userWithoutPassword);
      } else {
        // When creating, password is required
        if (!userForm.password) {
          setError('Parola este obligatorie pentru crearea unui user nou');
          return;
        }
        await createUser(userForm);
      }
      setUserModalOpen(false);
      setEditingUser(null);
      setUserForm(emptyUser);
      await loadAll();
    } catch (e: any) {
      setError(e.message || 'Eroare salvare user');
    }
  };

  const onDeleteUser = async (id?: number) => {
    if (!id) return;
    try {
      await deleteUser(id);
      await loadAll();
    } catch (e: any) {
      setError(e.message || 'Eroare ștergere user');
    }
  };

  // Device modal handlers
  const openCreateDevice = () => {
    setEditingDevice(null);
    setDeviceForm(emptyDevice);
    setDeviceModalOpen(true);
  };
  const openEditDevice = (d: Device) => {
    setEditingDevice(d);
    setDeviceForm({ id: d.id, name: d.name, maximumConsumptionValue: d.maximumConsumptionValue, userId: d.userId ?? null });
    setDeviceModalOpen(true);
  };
  const submitDeviceModal = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (isNaN(Number(deviceForm.maximumConsumptionValue))) {
        setError('Valoarea consumului trebuie să fie numerică.');
        return;
      }
      const payload = { ...deviceForm, maximumConsumptionValue: Number(deviceForm.maximumConsumptionValue), userId: deviceForm.userId === '' ? null : deviceForm.userId };
      if (editingDevice && editingDevice.id) {
        await updateDevice(editingDevice.id, payload);
      } else {
        await createDevice(payload);
      }
      setDeviceModalOpen(false);
      setEditingDevice(null);
      setDeviceForm(emptyDevice);
      await loadAll();
    } catch (e: any) {
      setError(e.message || 'Eroare salvare device');
    }
  };

  const onDeleteDevice = async (id?: number) => {
    if (!id) return;
    try {
      await deleteDevice(id);
      await loadAll();
    } catch (e: any) {
      setError(e.message || 'Eroare ștergere device');
    }
  };

  if (!token) return <div className="container"><div className="alert alert-error">Trebuie să vă autentificați.</div></div>;

  return (
    <div className="container" style={{ paddingTop: 24 }}>
      <div className="hero" style={{ textAlign: 'left', paddingTop: 0 }}>
        <h2>Admin Dashboard</h2>
        <p className="note">Cont: {email}. Gestionați utilizatori și device-uri; asignați device-uri către utilizatori.</p>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 12 }}>{error}</div>}
      {loading && <div>Se încarcă…</div>}

      {/* Tabs */}
      <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
        <button
          className={`btn btn-tab ${activeTab === 'users' ? 'btn-tab-active' : ''}`}
          onClick={()=>setActiveTab('users')}
        >Utilizatori</button>
        <button
          className={`btn btn-tab ${activeTab === 'devices' ? 'btn-tab-active' : ''}`}
          onClick={()=>setActiveTab('devices')}
        >Device-uri</button>
        <button
          className={`btn btn-tab ${activeTab === 'chat' ? 'btn-tab-active' : ''}`}
          onClick={()=>setActiveTab('chat')}
        >Chat Suport</button>
      </div>

      <div style={{ display: activeTab === 'chat' ? 'block' : 'none' }}>
        <AdminChat users={users} />
      </div>

      {/* USERS SECTION */}
      {activeTab === 'users' && (
        <section>
          {/* Small actions row with create button */}
          <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 8 }}>
            <button className="btn btn-primary" onClick={openCreateUser}>Creează user</button>
          </div>

          <div className="panel">
            <div style={{ overflowY: 'auto', maxHeight: '55vh' }}>
              <table className="table table-striped">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Email</th>
                    <th>First</th>
                    <th>Last</th>
                    <th>Actiuni</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map(u => (
                    <tr key={u.id}>
                      <td>{u.id}</td>
                      <td>{u.email}</td>
                      <td>{u.firstName}</td>
                      <td>{u.lastName}</td>
                      <td style={{ display: 'flex', gap: 8 }}>
                        <button className="btn btn-primary btn-sm" onClick={()=>openEditUser(u)}>Editează</button>
                        <button className="btn btn-danger btn-sm" onClick={()=>onDeleteUser(u.id)}>Șterge</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </section>
      )}

      {/* DEVICES SECTION */}
      {activeTab === 'devices' && (
        <section>
          {/* Small actions row with create button */}
          <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 8 }}>
            <button className="btn btn-primary" onClick={openCreateDevice}>Creează device</button>
          </div>

          <div className="panel">
            <div style={{ overflowY: 'auto', maxHeight: '55vh' }}>
              <table className="table table-striped">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Nume</th>
                    <th>Consum</th>
                    <th>User</th>
                    <th>Actiuni</th>
                  </tr>
                </thead>
                <tbody>
                  {devices.map(d => (
                    <tr key={d.id}>
                      <td>{d.id}</td>
                      <td>{d.name}</td>
                      <td>{d.maximumConsumptionValue}</td>
                      <td>{users.find(u=>u.id===d.userId)?.firstName ?? ''} {users.find(u=>u.id===d.userId)?.lastName ?? ''}</td>
                      <td style={{ display: 'flex', gap: 8 }}>
                        <button className="btn btn-primary btn-sm" onClick={()=>openEditDevice(d)}>Editează</button>
                        <button className="btn btn-danger btn-sm" onClick={()=>onDeleteDevice(d.id)}>Șterge</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </section>
      )}

      {/* User Modal */}
      <Modal
        isOpen={userModalOpen}
        title={editingUser ? 'Editează user' : 'Creează user'}
        onClose={()=>{ setUserModalOpen(false); setEditingUser(null); }}
        footer={(
          <>
            <button className="btn btn-ghost" onClick={()=>{ setUserModalOpen(false); setEditingUser(null); }}>Anulează</button>
            <button className="btn btn-primary" onClick={(e)=>submitUserModal(e)}>Salvează</button>
          </>
        )}
      >
        <form className="form" onSubmit={submitUserModal}>
          <div className="form-control">
            <label>Email</label>
            <input type="email" value={userForm.email} onChange={e=>setUserForm({...userForm, email:e.target.value})} required />
          </div>
          {!editingUser && (
            <div className="form-control">
              <label>Parola</label>
              <input type="password" value={userForm.password || ''} onChange={e=>setUserForm({...userForm, password:e.target.value})} required />
            </div>
          )}
          <div className="form-control">
            <label>First name</label>
            <input value={userForm.firstName} onChange={e=>setUserForm({...userForm, firstName:e.target.value})} required />
          </div>
          <div className="form-control">
            <label>Last name</label>
            <input value={userForm.lastName} onChange={e=>setUserForm({...userForm, lastName:e.target.value})} required />
          </div>
        </form>
      </Modal>

      {/* Device Modal */}
      <Modal
        isOpen={deviceModalOpen}
        title={editingDevice ? 'Editează device' : 'Creează device'}
        onClose={()=>{ setDeviceModalOpen(false); setEditingDevice(null); }}
        footer={(
          <>
            <button className="btn btn-ghost" onClick={()=>{ setDeviceModalOpen(false); setEditingDevice(null); }}>Anulează</button>
            <button className="btn btn-primary" onClick={(e)=>submitDeviceModal(e)}>Salvează</button>
          </>
        )}
      >
        <form className="form" onSubmit={submitDeviceModal}>
          <div className="form-control">
            <label>Nume</label>
            <input value={deviceForm.name} onChange={e=>setDeviceForm({...deviceForm, name:e.target.value})} required />
          </div>
          <div className="form-control">
            <label>Consum maxim</label>
            <input type="text" value={deviceForm.maximumConsumptionValue}
                   onChange={e=>setDeviceForm({...deviceForm, maximumConsumptionValue: e.target.value})} required />
          </div>
          <div className="form-control">
            <label>Asignat user</label>
            <select value={deviceForm.userId ?? ''} onChange={e=>setDeviceForm({...deviceForm, userId: e.target.value === '' ? null : Number(e.target.value)})}>
              <option value="">Niciunul</option>
              {users.map(u => <option key={u.id} value={u.id}>{u.firstName} {u.lastName} ({u.email})</option>)}
            </select>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default DashboardAdmin;
