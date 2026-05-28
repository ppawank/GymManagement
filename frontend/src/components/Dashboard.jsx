import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import { attendanceService } from '../services/attendanceService';
import { gymService } from '../services/gymService';
import { memberService } from '../services/memberService';
import { paymentService } from '../services/paymentService';
import './Dashboard.css';

function Dashboard() {
    const navigate = useNavigate();
    const [members, setMembers] = useState([]);
    const [payments, setPayments] = useState([]);
    const [attendance, setAttendance] = useState([]);
    const [occupancy, setOccupancy] = useState([]);
    const [trainers, setTrainers] = useState([]);
    const [classes, setClasses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const currentUser = authService.getCurrentUser();

    useEffect(() => {
        loadDashboard();
    }, []);

    const loadDashboard = async () => {
        setLoading(true);
        setError('');

        try {
            const [
                memberData,
                paymentData,
                attendanceData,
                occupancyData,
                trainerData,
                classData
            ] = await Promise.all([
                memberService.getAll(),
                paymentService.getAll(),
                attendanceService.getAll(),
                gymService.getOccupancy(),
                gymService.getTrainers(),
                gymService.getClasses()
            ]);

            setMembers(memberData);
            setPayments(paymentData);
            setAttendance(attendanceData);
            setOccupancy(occupancyData);
            setTrainers(trainerData);
            setClasses(classData);
        } catch (err) {
            setError(err.response?.data?.message || 'Unable to load dashboard metrics');
        } finally {
            setLoading(false);
        }
    };

    const handleLogout = async () => {
        try {
            await authService.logout();
        } finally {
            navigate('/');
        }
    };

    const analytics = useMemo(() => {
        const activeMembers = members.filter((member) => member.status === 'ACTIVE').length;
        const pendingFees = members.filter((member) => member.feeStatus === 'PENDING').length;
        const pendingVerification = payments.filter((payment) => !payment.verified).length;
        const revenue = payments
            .filter((payment) => payment.verified)
            .reduce((sum, payment) => sum + Number(payment.amount || 0), 0);
        const today = new Date().toISOString().slice(0, 10);
        const todayAttendance = attendance.filter((entry) => entry.attendanceDate === today).length;
        const liveOccupancy = occupancy.reduce((sum, branch) => sum + Number(branch.currentOccupancy || 0), 0);
        const availableTrainers = trainers.filter((trainer) => trainer.availabilityStatus === 'AVAILABLE').length;

        return {
            activeMembers,
            pendingFees,
            pendingVerification,
            revenue,
            todayAttendance,
            liveOccupancy,
            availableTrainers,
            classCount: classes.length
        };
    }, [attendance, classes, members, occupancy, payments, trainers]);

    const quickLinks = [
        { label: 'Members', path: '/members', detail: 'Roster and status' },
        { label: 'Logger', path: '/logger', detail: 'Check-ins and equipment' },
        { label: 'Search', path: '/search', detail: 'Elasticsearch portal' },
        { label: 'Payments', path: '/payments', detail: 'Monthly collections' },
        { label: 'Pending Fees', path: '/pending-fees', detail: 'Follow-up queue' }
    ];

    if (authService.isAdmin()) {
        quickLinks.push({ label: 'Users', path: '/users', detail: 'Access control' });
        quickLinks.push({ label: 'Excel', path: '/excel-management', detail: 'Import and export' });
    }

    const metrics = [
        { label: 'Active members', value: analytics.activeMembers },
        { label: 'Today attendance', value: analytics.todayAttendance },
        { label: 'Live occupancy', value: analytics.liveOccupancy },
        { label: 'Pending fees', value: analytics.pendingFees },
        { label: 'Payment review', value: analytics.pendingVerification },
        { label: 'Verified revenue', value: `Rs ${analytics.revenue.toLocaleString('en-IN')}` },
        { label: 'Available trainers', value: analytics.availableTrainers },
        { label: 'Scheduled classes', value: analytics.classCount }
    ];

    return (
        <div className="ops-page">
            <header className="ops-header">
                <div>
                    <p className="eyebrow">Operations console</p>
                    <h1>Titan Fitness</h1>
                    <p className="subtle">Signed in as {currentUser?.username || 'staff'} ({currentUser?.role || 'STAFF'})</p>
                </div>
                <div className="header-actions">
                    <button className="secondary-button" onClick={loadDashboard} disabled={loading}>Refresh</button>
                    <button className="primary-button" onClick={handleLogout}>Logout</button>
                </div>
            </header>

            {error && <div className="notice error">{error}</div>}

            <main className="ops-layout">
                <section className="metrics-grid" aria-label="Analytics">
                    {metrics.map((metric) => (
                        <article className="metric-card" key={metric.label}>
                            <span>{metric.label}</span>
                            <strong>{loading ? '-' : metric.value}</strong>
                        </article>
                    ))}
                </section>

                <section className="panel-grid">
                    <div className="panel wide-panel">
                        <div className="panel-heading">
                            <h2>Branch Occupancy</h2>
                            <button className="text-button" onClick={() => navigate('/logger')}>Open logger</button>
                        </div>
                        <div className="occupancy-list">
                            {occupancy.map((branch) => (
                                <div className="occupancy-row" key={branch.branchId}>
                                    <div>
                                        <strong>{branch.branchName}</strong>
                                        <span>{branch.location}</span>
                                    </div>
                                    <div className="occupancy-meter">
                                        <div style={{ width: `${Math.min(branch.utilizationPercent || 0, 100)}%` }} />
                                    </div>
                                    <b>{branch.currentOccupancy}/{branch.capacity}</b>
                                </div>
                            ))}
                            {!loading && occupancy.length === 0 && <p className="empty-text">No branch occupancy data yet.</p>}
                        </div>
                    </div>

                    <div className="panel">
                        <div className="panel-heading">
                            <h2>Quick Actions</h2>
                        </div>
                        <div className="quick-grid">
                            {quickLinks.map((link) => (
                                <button key={link.path} className="quick-link" onClick={() => navigate(link.path)}>
                                    <strong>{link.label}</strong>
                                    <span>{link.detail}</span>
                                </button>
                            ))}
                        </div>
                    </div>

                    <div className="panel">
                        <div className="panel-heading">
                            <h2>Payment Queue</h2>
                            <button className="text-button" onClick={() => navigate('/payments')}>Review</button>
                        </div>
                        <div className="compact-list">
                            {payments.slice(0, 6).map((payment) => (
                                <div className="compact-row" key={payment.id}>
                                    <span>{payment.memberName}</span>
                                    <b>{payment.verified ? 'Verified' : 'Pending'}</b>
                                </div>
                            ))}
                            {!loading && payments.length === 0 && <p className="empty-text">No payment records found.</p>}
                        </div>
                    </div>
                </section>
            </main>
        </div>
    );
}

export default Dashboard;
