import api from './api';

export const authService = {
    login: async (username, password) => {
        const response = await api.post('/auth/login', { username, password });
        if (response.data.token) {
            localStorage.setItem('user', JSON.stringify(response.data));
        }
        return response.data;
    },

    logout: async () => {
        try {
            await api.post('/auth/logout');
        } finally {
            localStorage.removeItem('user');
        }
    },

    getCurrentUser: () => {
        return JSON.parse(localStorage.getItem('user'));
    },

    isAdmin: () => {
        const user = JSON.parse(localStorage.getItem('user'));
        return user?.role === 'ADMIN';
    },

    validate: async () => {
        try {
            const response = await api.get('/auth/validate');
            if (response.data.valid) {
                const storedUser = JSON.parse(localStorage.getItem('user'));
                if (storedUser) {
                    storedUser.username = response.data.username;
                    storedUser.role = response.data.role;
                    localStorage.setItem('user', JSON.stringify(storedUser));
                }
            } else {
                localStorage.removeItem('user');
            }
            return response.data;
        } catch (error) {
            localStorage.removeItem('user');
            throw error;
        }
    }
};
