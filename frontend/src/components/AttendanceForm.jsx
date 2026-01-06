import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { attendanceService } from '../services/attendanceService';
import { memberService } from '../services/memberService';
import './AttendanceForm.css';

function AttendanceForm() {
    const [members, setMembers] = useState([]);
    const [formData, setFormData] = useState({
        memberId: '',
        attendanceDate: new Date().toISOString().split('T')[0],
        checkInTime: new Date().toTimeString().split(' ')[0].substring(0, 5)
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
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

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await attendanceService.mark({
                ...formData,
                memberId: parseInt(formData.memberId)
            });
            alert('Attendance marked successfully!');
            setFormData({
                memberId: '',
                attendanceDate: new Date().toISOString().split('T')[0],
                checkInTime: new Date().toTimeString().split(' ')[0].substring(0, 5)
            });
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to mark attendance');
        } finally {
            setLoading(false);
        }
    };

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

                    <div className="form-group">
                        <label htmlFor="memberId">Select Member *</label>
                        <select
                            id="memberId"
                            name="memberId"
                            value={formData.memberId}
                            onChange={handleChange}
                            required
                        >
                            <option value="">-- Select a member --</option>
                            {members.map(member => (
                                <option key={member.id} value={member.id}>
                                    {member.name} ({member.email})
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="form-group">
                        <label htmlFor="attendanceDate">Attendance Date *</label>
                        <input
                            type="date"
                            id="attendanceDate"
                            name="attendanceDate"
                            value={formData.attendanceDate}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="checkInTime">Check-in Time *</label>
                        <input
                            type="time"
                            id="checkInTime"
                            name="checkInTime"
                            value={formData.checkInTime}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <button type="submit" className="submit-button" disabled={loading}>
                        {loading ? 'Marking...' : 'Mark Attendance'}
                    </button>
                </form>
            </div>
        </div>
    );
}

export default AttendanceForm;
