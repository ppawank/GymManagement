import api from './api';

export const searchService = {
    health: async () => {
        const response = await api.get('/search/health');
        return response.data;
    },

    searchAll: async (query, size = 20) => {
        const response = await api.get('/search', { params: { q: query, size } });
        return response.data;
    },

    searchIndex: async (index, query, size = 20) => {
        const response = await api.get(`/search/${index}`, { params: { q: query, size } });
        return response.data;
    }
};
