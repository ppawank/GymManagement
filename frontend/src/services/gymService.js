import api from './api';

export const gymService = {
    getBranches: async () => {
        const response = await api.get('/gym/branches');
        return response.data;
    },

    getTrainers: async () => {
        const response = await api.get('/gym/trainers');
        return response.data;
    },

    getClasses: async () => {
        const response = await api.get('/gym/classes');
        return response.data;
    },

    getOccupancy: async () => {
        const response = await api.get('/gym/occupancy');
        return response.data;
    },

    checkIn: async (memberId, branchId) => {
        const response = await api.post('/gym/checkin', { memberId, branchId });
        return response.data;
    },

    checkOut: async (memberId, branchId) => {
        const response = await api.post('/gym/checkout', { memberId, branchId });
        return response.data;
    },

    getActiveEquipment: async () => {
        const response = await api.get('/gym/equipment/active');
        return response.data;
    },

    startEquipment: async (memberId, branchId, equipmentName) => {
        const response = await api.post('/gym/equipment/start', { memberId, branchId, equipmentName });
        return response.data;
    },

    stopEquipment: async (usageId) => {
        const response = await api.post(`/gym/equipment/${usageId}/stop`);
        return response.data;
    }
};
