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
    }
};
