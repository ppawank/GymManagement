import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { memberService } from '../services/memberService';
import './MemberList.css';

function MemberList() {
    const [members, setMembers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [searchTerm, setSearchTerm] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        fetchMembers();
    }, []);

    const fetchMembers = async () => {
        try {
            const data = await memberService.getAll();
            setMembers(data);
        } catch (err) {
            setError('Failed to load members');
        } finally {
            setLoading(false);
        }
    };

    const handleActivate = async (id) => {
        try {
            await memberService.activate(id);
            fetchMembers();
        } catch (err) {
            alert(err.response?.data?.message || 'Failed to activate member');
        }
    };

    const handleDeactivate = async (id) => {
        try {
            await memberService.deactivate(id);
            fetchMembers();
        } catch (err) {
            alert(err.response?.data?.message || 'Failed to deactivate member');
        }
    };

    const filteredMembers = members.filter(member =>
        member.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        member.email.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="page-container">
            <div className="page-header">
                <button onClick={() => navigate('/dashboard')} className="back-button">
                    ← Back
                </button>
                <h1>Member Management</h1>
                <button onClick={() => navigate('/members/new')} className="primary-button">
                    + Add Member
                </button>
            </div>

            <div className="search-bar">
                <input
                    type="text"
                    placeholder="Search members by name or email..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                />
            </div>

            {loading && <div className="loading">Loading members...</div>}
            {error && <div className="error-message">{error}</div>}

            <div className="table-container">
                <table className="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Name</th>
                            <th>Email</th>
                            <th>Phone</th>
                            <th>Join Date</th>
                            <th>Status</th>
                            <th>Fee Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filteredMembers.map(member => (
                            <tr key={member.id}>
                                <td>{member.id}</td>
                                <td>{member.name}</td>
                                <td>{member.email}</td>
                                <td>{member.phone}</td>
                                <td>{new Date(member.joinDate).toLocaleDateString()}</td>
                                <td>
                                    <span className={`status-badge ${member.status.toLowerCase()}`}>
                                        {member.status}
                                    </span>
                                </td>
                                <td>
                                    <span className={`status-badge ${member.feeStatus?.toLowerCase()}`}>
                                        {member.feeStatus}
                                    </span>
                                </td>
                                <td>
                                    {member.status === 'ACTIVE' ? (
                                        <button
                                            onClick={() => handleDeactivate(member.id)}
                                            className="action-button deactivate"
                                        >
                                            Deactivate
                                        </button>
                                    ) : (
                                        <button
                                            onClick={() => handleActivate(member.id)}
                                            className="action-button activate"
                                        >
                                            Activate
                                        </button>
                                    )}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

export default MemberList;
