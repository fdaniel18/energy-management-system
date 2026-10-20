// @ts-nocheck
import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const HomeUser: React.FC = () => {
  const { email } = useAuth();

  return (
    <div className="container">
      <div className="hero" style={{ textAlign: 'left' }}>
        <h1>Bun venit{email ? `, ${email}` : ''}!</h1>
        <p className="note">Aceasta este pagina de acasă pentru utilizatori.</p>
      </div>

      <div className="panel" style={{ marginTop: 16 }}>
        <h3>Ce poți face ca utilizator</h3>
        <ul style={{ marginTop: 8, lineHeight: 1.7 }}>
          <li>Vizualizezi lista dispozitivelor tale.</li>
          <li>Vezi valoarea maximă de consum pentru fiecare dispozitiv.</li>
          <li>Primești actualizări atunci când se modifică asignarea dispozitivelor tale.</li>
        </ul>

        <div className="actions" style={{ marginTop: 16 }}>
          <Link to="/my-devices" className="btn btn-primary">Vezi device-urile mele</Link>
        </div>
      </div>
    </div>
  );
};

export default HomeUser;
