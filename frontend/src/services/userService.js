import api from './api';

export const userService = {
    create: async (userData) => {
        const response = await api.post('/users', userData);
        return response.data;
    },

    getAll: async () => {
        const response = await api.get('/users');
        return response.data;
    },

    updateRole: async (userId, role) => {
        const response = await api.put(`/users/${userId}/role`, { role });
        return response.data;
    },

    delete: async (userId) => {
        await api.delete(`/users/${userId}`);
    }
};
