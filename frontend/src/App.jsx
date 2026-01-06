import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import MemberList from './components/MemberList';
import MemberForm from './components/MemberForm';
import AttendanceForm from './components/AttendanceForm';
import PaymentForm from './components/PaymentForm';
import './App.css';

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<Login />} />
                <Route path="/dashboard" element={<Dashboard />} />
                <Route path="/members" element={<MemberList />} />
                <Route path="/members/new" element={<MemberForm />} />
                <Route path="/attendance" element={<AttendanceForm />} />
                <Route path="/payments" element={<PaymentForm />} />
                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </Router>
    );
}

export default App;
