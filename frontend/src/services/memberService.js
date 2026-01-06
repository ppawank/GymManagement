import api from './api';

export const memberService = {
    register: async (memberData) => {
        const response = await api.post('/members', memberData);
        return response.data;
    },

    getAll: async () => {
        const response = await api.get('/members');
        return response.data;
    },

    getById: async (id) => {
        const response = await api.get(`/members/${id}`);
        return response.data;
    },

    activate: async (id) => {
        const response = await api.put(`/members/${id}/activate`);
        return response.data;
    },

    deactivate: async (id) => {
        const response = await api.put(`/members/${id}/deactivate`);
        return response.data;
    }
};
