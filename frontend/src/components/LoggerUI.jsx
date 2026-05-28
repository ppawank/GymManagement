import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { gymService } from '../services/gymService';
import { memberService } from '../services/memberService';
import './Dashboard.css';
import './LoggerUI.css';

function LoggerUI() {
    const navigate = useNavigate();
    const [members, setMembers] = useState([]);
    const [branches, setBranches] = useState([]);
    const [occupancy, setOccupancy] = useState([]);
    const [activeEquipment, setActiveEquipment] = useState([]);
    const [form, setForm] = useState({ memberId: '', branchId: '', equipmentName: '' });
    const [mode, setMode] = useState('checkin');
    const [loading, setLoading] = useState(true);
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');

    useEffect(() => {
        loadLogger();
    }, []);

    const loadLogger = async () => {
        setLoading(true);
        setError('');

        try {
            const [memberData, branchData, occupancyData, equipmentData] = await Promise.all([
                memberService.getAll(),
                gymService.getBranches(),
                gymService.getOccupancy(),
                gymService.getActiveEquipment()
            ]);

            setMembers(memberData.filter((member) => member.status === 'ACTIVE'));
            setBranches(branchData);
            setOccupancy(occupancyData);
            setActiveEquipment(equipmentData);

            setForm((current) => ({
                ...current,
                memberId: current.memberId || memberData.find((member) => member.status === 'ACTIVE')?.id || '',
                branchId: current.branchId || branchData[0]?.id || ''
            }));
        } catch (err) {
            setError(err.response?.data?.message || 'Unable to load logger data');
        } finally {
            setLoading(false);
        }
    };

    const selectedMember = useMemo(
        () => members.find((member) => String(member.id) === String(form.memberId)),
        [form.memberId, members]
    );

    const handleSubmit = async (event) => {
        event.preventDefault();
        setError('');
        setMessage('');

        if (!form.memberId || !form.branchId) {
            setError('Select a member and branch first');
            return;
        }

        try {
            if (mode === 'checkin') {
                await gymService.checkIn(Number(form.memberId), Number(form.branchId));
                setMessage('Check-in recorded');
            } else if (mode === 'checkout') {
                await gymService.checkOut(Number(form.memberId), Number(form.branchId));
                setMessage('Check-out recorded');
            } else {
                if (!form.equipmentName.trim()) {
                    setError('Enter an equipment name');
                    return;
                }
                await gymService.startEquipment(Number(form.memberId), Number(form.branchId), form.equipmentName.trim());
                setMessage('Equipment usage started');
                setForm((current) => ({ ...current, equipmentName: '' }));
            }

            await loadLogger();
        } catch (err) {
            setError(err.response?.data?.message || 'Logger action failed');
        }
    };

    const stopEquipment = async (usageId) => {
        setError('');
        setMessage('');

        try {
            await gymService.stopEquipment(usageId);
            setMessage('Equipment usage stopped');
            await loadLogger();
        } catch (err) {
            setError(err.response?.data?.message || 'Unable to stop equipment usage');
        }
    };

    return (
        <div className="ops-page">
            <header className="ops-header">
                <div>
                    <p className="eyebrow">Logger UI</p>
                    <h1>Front Desk Activity</h1>
                    <p className="subtle">Record check-ins, check-outs, and equipment usage.</p>
                </div>
                <div className="header-actions">
                    <button className="secondary-button" onClick={loadLogger} disabled={loading}>Refresh</button>
                    <button className="secondary-button" onClick={() => navigate('/dashboard')}>Dashboard</button>
                </div>
            </header>

            {error && <div className="notice error">{error}</div>}
            {message && <div className="notice success">{message}</div>}

            <main className="ops-layout logger-layout">
                <section className="panel">
                    <div className="panel-heading">
                        <h2>Log Activity</h2>
                    </div>

                    <div className="segmented-control" role="tablist" aria-label="Logger action">
                        {['checkin', 'checkout', 'equipment'].map((option) => (
                            <button
                                key={option}
                                type="button"
                                className={mode === option ? 'active' : ''}
                                onClick={() => setMode(option)}
                            >
                                {option === 'checkin' ? 'Check in' : option === 'checkout' ? 'Check out' : 'Equipment'}
                            </button>
                        ))}
                    </div>

                    <form className="logger-form" onSubmit={handleSubmit}>
                        <label>
                            Member
                            <select value={form.memberId} onChange={(event) => setForm({ ...form, memberId: event.target.value })}>
                                <option value="">Select member</option>
                                {members.map((member) => (
                                    <option value={member.id} key={member.id}>{member.name}</option>
                                ))}
                            </select>
                        </label>

                        <label>
                            Branch
                            <select value={form.branchId} onChange={(event) => setForm({ ...form, branchId: event.target.value })}>
                                <option value="">Select branch</option>
                                {branches.map((branch) => (
                                    <option value={branch.id} key={branch.id}>{branch.name}</option>
                                ))}
                            </select>
                        </label>

                        {mode === 'equipment' && (
                            <label>
                                Equipment
                                <input
                                    value={form.equipmentName}
                                    onChange={(event) => setForm({ ...form, equipmentName: event.target.value })}
                                    placeholder="Treadmill 2, Bench Press, Rowing Machine"
                                />
                            </label>
                        )}

                        <button className="primary-button" type="submit" disabled={loading}>
                            Save Activity
                        </button>
                    </form>

                    {selectedMember && (
                        <div className="selected-member">
                            <strong>{selectedMember.name}</strong>
                            <span>{selectedMember.email} · {selectedMember.feeStatus || 'UNKNOWN'} fees</span>
                        </div>
                    )}
                </section>

                <section className="panel">
                    <div className="panel-heading">
                        <h2>Live Occupancy</h2>
                    </div>
                    <div className="occupancy-list">
                        {occupancy.map((branch) => (
                            <div className="occupancy-row compact-occupancy" key={branch.branchId}>
                                <div>
                                    <strong>{branch.branchName}</strong>
                                    <span>{branch.location}</span>
                                </div>
                                <b>{branch.currentOccupancy}/{branch.capacity}</b>
                            </div>
                        ))}
                    </div>
                </section>

                <section className="panel wide-logger-panel">
                    <div className="panel-heading">
                        <h2>Active Equipment</h2>
                    </div>
                    <div className="equipment-table">
                        {activeEquipment.map((usage) => (
                            <div className="equipment-row" key={usage.id}>
                                <div>
                                    <strong>{usage.equipmentName}</strong>
                                    <span>Member #{usage.member?.id || usage.memberId} · Branch #{usage.branch?.id || usage.branchId}</span>
                                </div>
                                <button className="secondary-button" onClick={() => stopEquipment(usage.id)}>Stop</button>
                            </div>
                        ))}
                        {!loading && activeEquipment.length === 0 && <p className="empty-text">No active equipment sessions.</p>}
                    </div>
                </section>
            </main>
        </div>
    );
}

export default LoggerUI;
