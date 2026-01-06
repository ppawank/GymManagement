import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { memberService } from '../services/memberService';
import './MemberForm.css';

function MemberForm() {
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        phone: '',
        joinDate: new Date().toISOString().split('T')[0]
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

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
            await memberService.register(formData);
            alert('Member registered successfully!');
            navigate('/members');
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to register member');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <button onClick={() => navigate('/members')} className="back-button">
                    ← Back
                </button>
                <h1>Register New Member</h1>
                <div></div>
            </div>

            <div className="form-container">
                <form onSubmit={handleSubmit} className="member-form">
                    {error && <div className="error-message">{error}</div>}

                    <div className="form-group">
                        <label htmlFor="name">Full Name *</label>
                        <input
                            type="text"
                            id="name"
                            name="name"
                            value={formData.name}
                            onChange={handleChange}
                            placeholder="Enter member's full name"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="email">Email Address *</label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            placeholder="Enter email address"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="phone">Phone Number *</label>
                        <input
                            type="tel"
                            id="phone"
                            name="phone"
                            value={formData.phone}
                            onChange={handleChange}
                            placeholder="Enter phone number"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="joinDate">Join Date *</label>
                        <input
                            type="date"
                            id="joinDate"
                            name="joinDate"
                            value={formData.joinDate}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <button type="submit" className="submit-button" disabled={loading}>
                        {loading ? 'Registering...' : 'Register Member'}
                    </button>
                </form>
            </div>
        </div>
    );
}

export default MemberForm;
