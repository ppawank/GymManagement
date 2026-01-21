import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { attendanceService } from '../services/attendanceService';
import { memberService } from '../services/memberService';
import './AttendanceForm.css';

function AttendanceForm() {
    const [members, setMembers] = useState([]);
    const [selectedMembers, setSelectedMembers] = useState([]);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [loading, setLoading] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        fetchMembers();
    }, []);

    const fetchMembers = async () => {
        try {
            const data = await memberService.getAll();
            // Filter only active members
            const activeMembers = data.filter(m => m.status === 'ACTIVE');
            setMembers(activeMembers);
        } catch (err) {
            setError('Failed to load members');
        }
    };

    const handleCheckboxChange = (memberId) => {
        setSelectedMembers(prev => {
            if (prev.includes(memberId)) {
                return prev.filter(id => id !== memberId);
            } else {
                return [...prev, memberId];
            }
        });
    };

    const handleSelectAll = () => {
        const filteredMemberIds = filteredMembers.map(m => m.id);
        if (selectedMembers.length === filteredMemberIds.length) {
            setSelectedMembers([]);
        } else {
            setSelectedMembers(filteredMemberIds);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        if (selectedMembers.length === 0) {
            setError('Please select at least one member');
            return;
        }

        setLoading(true);

        try {
            const result = await attendanceService.markBulk(selectedMembers);
            setSuccess(`Attendance marked for ${result.length} member(s)!`);
            setSelectedMembers([]);
            setTimeout(() => setSuccess(''), 3000);
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to mark attendance');
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
                <h1>Mark Attendance</h1>
                <div></div>
            </div>

            <div className="form-container">
                <form onSubmit={handleSubmit} className="member-form">
                    {error && <div className="error-message">{error}</div>}
                    {success && <div className="success-message">{success}</div>}

                    <div className="attendance-info">
                        <p>📅 Date: <strong>{new Date().toLocaleDateString()}</strong></p>
                        <p>🕐 Time: <strong>{new Date().toLocaleTimeString()}</strong></p>
                        <p>Selected: <strong>{selectedMembers.length}</strong> member(s)</p>
                    </div>

                    <div className="form-group">
                        <input
                            type="text"
                            placeholder="Search members by name or email..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="search-input"
                        />
                    </div>

                    <div className="checkbox-header">
                        <label>
                            <input
                                type="checkbox"
                                checked={selectedMembers.length === filteredMembers.length && filteredMembers.length > 0}
                                onChange={handleSelectAll}
                            />
                            <span>Select All ({filteredMembers.length})</span>
                        </label>
                    </div>

                    <div className="members-list">
                        {filteredMembers.map(member => (
                            <div key={member.id} className="member-checkbox-item">
                                <label>
                                    <input
                                        type="checkbox"
                                        checked={selectedMembers.includes(member.id)}
                                        onChange={() => handleCheckboxChange(member.id)}
                                    />
                                    <div className="member-info">
                                        <span className="member-name">{member.name}</span>
                                        <span className="member-email">{member.email}</span>
                                    </div>
                                </label>
                            </div>
                        ))}
                    </div>

                    {filteredMembers.length === 0 && (
                        <div className="no-data">No active members found</div>
                    )}

                    <button
                        type="submit"
                        className="submit-button"
                        disabled={loading || selectedMembers.length === 0}
                    >
                        {loading ? 'Marking...' : `Mark Attendance (${selectedMembers.length})`}
                    </button>
                </form>
            </div>
        </div>
    );
}

export default AttendanceForm;
