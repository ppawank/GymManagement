import api from './api';

export const paymentService = {
    record: async (paymentData) => {
        const response = await api.post('/payments', paymentData);
        return response.data;
    },

    getAll: async () => {
        const response = await api.get('/payments');
        return response.data;
    },

    getMemberPayments: async (memberId) => {
        const response = await api.get(`/payments/member/${memberId}`);
        return response.data;
    },

    verify: async (paymentId) => {
        const response = await api.post(`/payments/${paymentId}/verify`);
        return response.data;
    },

    getPendingVerifications: async () => {
        const response = await api.get('/payments/pending-verification');
        return response.data;
    }
};
