import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { userService } from '../services/userService';
import { authService } from '../services/authService';
import './UserManagement.css';

function UserManagement() {
    const [users, setUsers] = useState([]);
    const [formData, setFormData] = useState({
        username: '',
        password: '',
        role: 'STAFF'
    });
    const [editingId, setEditingId] = useState(null);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        // Security check
        if (!authService.isAdmin()) {
            navigate('/dashboard');
            return;
        }
        fetchUsers();
    }, [navigate]);

    const fetchUsers = async () => {
        try {
            const data = await userService.getAll();
            setUsers(data);
        } catch (err) {
            setError('Failed to load users');
        }
    };

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        setLoading(true);

        try {
            if (editingId) {
                // Update role only (password update not implemented in this simple UI)
                await userService.updateRole(editingId, formData.role);
                setSuccess('User role updated successfully');
            } else {
                await userService.create(formData);
                setSuccess('User created successfully');
            }

            setFormData({ username: '', password: '', role: 'STAFF' });
            setEditingId(null);
            fetchUsers();
        } catch (err) {
            setError(err.response?.data?.message || 'Operation failed');
        } finally {
            setLoading(false);
        }
    };

    const handleEdit = (user) => {
        setEditingId(user.id);
        setFormData({
            username: user.username,
            password: '', // Don't show password
            role: user.role
        });
        window.scrollTo(0, 0);
    };

    const handleDelete = async (userId) => {
        if (!window.confirm('Are you sure you want to delete this user?')) return;

        try {
            await userService.delete(userId);
            setSuccess('User deleted successfully');
            fetchUsers();
        } catch (err) {
            setError('Failed to delete user');
        }
    };

    const handleCancel = () => {
        setEditingId(null);
        setFormData({ username: '', password: '', role: 'STAFF' });
        setError('');
        setSuccess('');
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <button onClick={() => navigate('/dashboard')} className="back-button">
                    ← Back
                </button>
                <h1>User Management</h1>
                <div style={{ width: '80px' }}></div>
            </div>

            <div className="form-container">
                <h2 className="section-title">{editingId ? 'Edit User Role' : 'Create New User'}</h2>

                <form onSubmit={handleSubmit} className="user-form">
                    {error && <div className="error-message full-width">{error}</div>}
                    {success && <div className="success-message full-width">{success}</div>}

                    <div className="form-group">
                        <label htmlFor="username">Username</label>
                        <input
                            type="text"
                            id="username"
                            name="username"
                            value={formData.username}
                            onChange={handleChange}
                            required
                            disabled={!!editingId} // Cannot change username
                        />
                    </div>

                    {!editingId && (
                        <div className="form-group">
                            <label htmlFor="password">Password</label>
                            <input
                                type="password"
                                id="password"
                                name="password"
                                value={formData.password}
                                onChange={handleChange}
                                required
                            />
                        </div>
                    )}

                    <div className="form-group">
                        <label htmlFor="role">Role</label>
                        <select
                            id="role"
                            name="role"
                            value={formData.role}
                            onChange={handleChange}
                            required
                        >
                            <option value="ADMIN">Admin</option>
                            <option value="STAFF">Staff</option>
                            <option value="VIEW_ONLY">View Only</option>
                        </select>
                    </div>

                    <div className="form-actions">
                        <button type="submit" className="btn-primary" disabled={loading}>
                            {loading ? 'Saving...' : (editingId ? 'Update Role' : 'Create User')}
                        </button>
                        {editingId && (
                            <button type="button" className="btn-secondary" onClick={handleCancel}>
                                Cancel
                            </button>
                        )}
                    </div>
                </form>
            </div>

            <div className="users-list">
                <h2 className="section-title">Existing Users</h2>
                {users.map(user => (
                    <div key={user.id} className="user-card">
                        <div className="user-info">
                            <h3>{user.username}</h3>
                            <div className="user-meta">
                                <span className={`role-badge role-${user.role}`}>
                                    {user.role}
                                </span>
                                <span className={`status-badge status-${user.active ? 'active' : 'inactive'}`}>
                                    {user.active ? 'Active' : 'Inactive'}
                                </span>
                                <span>Created: {new Date(user.createdAt).toLocaleDateString()}</span>
                            </div>
                        </div>
                        <div className="user-actions">
                            <button
                                className="btn-icon"
                                onClick={() => handleEdit(user)}
                                title="Edit Role"
                            >
                                ✏️
                            </button>
                            <button
                                className="btn-icon btn-delete"
                                onClick={() => handleDelete(user.id)}
                                title="Delete User"
                                disabled={user.username === 'admin'} // Protect default admin
                            >
                                🗑️
                            </button>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default UserManagement;
