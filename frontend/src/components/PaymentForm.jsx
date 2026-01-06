import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { paymentService } from '../services/paymentService';
import { memberService } from '../services/memberService';
import './PaymentForm.css';

function PaymentForm() {
    const [members, setMembers] = useState([]);
    const [formData, setFormData] = useState({
        memberId: '',
        amount: '',
        paymentMonth: new Date().getMonth() + 1,
        paymentYear: new Date().getFullYear(),
        paymentDate: new Date().toISOString().split('T')[0]
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
            setMembers(data);
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
            await paymentService.record({
                ...formData,
                memberId: parseInt(formData.memberId),
                paymentMonth: parseInt(formData.paymentMonth),
                paymentYear: parseInt(formData.paymentYear)
            });
            alert('Payment recorded successfully!');
            setFormData({
                memberId: '',
                amount: '',
                paymentMonth: new Date().getMonth() + 1,
                paymentYear: new Date().getFullYear(),
                paymentDate: new Date().toISOString().split('T')[0]
            });
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to record payment');
        } finally {
            setLoading(false);
        }
    };

    const months = [
        { value: 1, label: 'January' },
        { value: 2, label: 'February' },
        { value: 3, label: 'March' },
        { value: 4, label: 'April' },
        { value: 5, label: 'May' },
        { value: 6, label: 'June' },
        { value: 7, label: 'July' },
        { value: 8, label: 'August' },
        { value: 9, label: 'September' },
        { value: 10, label: 'October' },
        { value: 11, label: 'November' },
        { value: 12, label: 'December' }
    ];

    const years = Array.from({ length: 5 }, (_, i) => new Date().getFullYear() - 2 + i);

    return (
        <div className="page-container">
            <div className="page-header">
                <button onClick={() => navigate('/dashboard')} className="back-button">
                    ← Back
                </button>
                <h1>Record Payment</h1>
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
                        <label htmlFor="amount">Amount *</label>
                        <input
                            type="number"
                            id="amount"
                            name="amount"
                            value={formData.amount}
                            onChange={handleChange}
                            placeholder="Enter payment amount"
                            step="0.01"
                            min="0"
                            required
                        />
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="paymentMonth">Month *</label>
                            <select
                                id="paymentMonth"
                                name="paymentMonth"
                                value={formData.paymentMonth}
                                onChange={handleChange}
                                required
                            >
                                {months.map(month => (
                                    <option key={month.value} value={month.value}>
                                        {month.label}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="form-group">
                            <label htmlFor="paymentYear">Year *</label>
                            <select
                                id="paymentYear"
                                name="paymentYear"
                                value={formData.paymentYear}
                                onChange={handleChange}
                                required
                            >
                                {years.map(year => (
                                    <option key={year} value={year}>
                                        {year}
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="paymentDate">Payment Date *</label>
                        <input
                            type="date"
                            id="paymentDate"
                            name="paymentDate"
                            value={formData.paymentDate}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <button type="submit" className="submit-button" disabled={loading}>
                        {loading ? 'Recording...' : 'Record Payment'}
                    </button>
                </form>
            </div>
        </div>
    );
}

export default PaymentForm;
