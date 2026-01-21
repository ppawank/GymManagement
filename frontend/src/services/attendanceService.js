import api from './api';

export const attendanceService = {
    mark: async (attendanceData) => {
        const response = await api.post('/attendance', attendanceData);
        return response.data;
    },

    markBulk: async (memberIds) => {
        const response = await api.post('/attendance/bulk', { memberIds });
        return response.data;
    },

    getAll: async () => {
        const response = await api.get('/attendance');
        return response.data;
    },

    getMemberAttendance: async (memberId) => {
        const response = await api.get(`/attendance/member/${memberId}`);
        return response.data;
    }
};
