import React from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import './Dashboard.css';

function Dashboard() {
    const navigate = useNavigate();

    const handleLogout = async () => {
        try {
            await authService.logout();
            navigate('/');
        } catch (err) {
            console.error('Logout failed:', err);
            navigate('/');
        }
    };

    const menuItems = [
        {
            title: 'Members',
            icon: '👥',
            description: 'Manage gym members',
            path: '/members',
            color: '#667eea'
        },
        {
            title: 'Attendance',
            icon: '📋',
            description: 'Mark daily attendance',
            path: '/attendance',
            color: '#f093fb'
        },
        {
            title: 'Payments',
            icon: '💳',
            description: 'Record monthly payments',
            path: '/payments',
            color: '#4facfe'
        },
        {
            title: 'Pending Fees',
            icon: '💰',
            description: 'View members with pending fees',
            path: '/pending-fees',
            color: '#fa709a'
        }
    ];

    if (authService.isAdmin()) {
        menuItems.push({
            title: 'User Access',
            icon: '🔐',
            description: 'Manage system users',
            path: '/users',
            color: '#ff9f43'
        });
    }

    return (
        <div className="dashboard-container">
            <header className="dashboard-header">
                <div className="header-content">
                    <h1>🏋️ Titan Fitness</h1>
                    <button onClick={handleLogout} className="logout-button">
                        Logout
                    </button>
                </div>
            </header>

            <main className="dashboard-main">
                <div className="welcome-section">
                    <h2>Welcome, Admin!</h2>
                    <p>Manage your gym operations efficiently</p>
                </div>

                <div className="menu-grid">
                    {menuItems.map((item, index) => (
                        <div
                            key={index}
                            className="menu-card"
                            onClick={() => navigate(item.path)}
                            style={{ '--card-color': item.color }}
                        >
                            <div className="menu-icon">{item.icon}</div>
                            <h3>{item.title}</h3>
                            <p>{item.description}</p>
                        </div>
                    ))}
                </div>
            </main>
        </div>
    );
}

export default Dashboard;
