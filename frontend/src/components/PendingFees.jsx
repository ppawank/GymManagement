import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { memberService } from '../services/memberService';
import './PendingFees.css';

function PendingFees() {
    const [members, setMembers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [searchTerm, setSearchTerm] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        fetchPendingFees();
    }, []);

    const fetchPendingFees = async () => {
        try {
            const data = await memberService.getPendingFees();
            setMembers(data);
        } catch (err) {
            setError('Failed to load pending fees');
        } finally {
            setLoading(false);
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
                <h1>Pending Fees - {new Date().toLocaleString('default', { month: 'long', year: 'numeric' })}</h1>
                <div style={{ width: '120px' }}></div> {/* Spacer for alignment */}
            </div>

            <div className="search-bar">
                <input
                    type="text"
                    placeholder="Search members by name or email..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                />
            </div>

            {loading && <div className="loading">Loading pending fees...</div>}
            {error && <div className="error-message">{error}</div>}

            {!loading && !error && filteredMembers.length === 0 && (
                <div className="no-data">
                    {searchTerm ? 'No members found matching your search.' : 'All members have paid their fees for this month! 🎉'}
                </div>
            )}

            {!loading && !error && filteredMembers.length > 0 && (
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
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}

export default PendingFees;
