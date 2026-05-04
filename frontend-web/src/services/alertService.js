// frontend-web/src/services/alertService.js
import api from './api';

// api base already has /api/v1, so just use /alerts
const API_BASE = '/alerts';

const alertService = {
    getPendingAlerts: async () => {
        const response = await api.get(`${API_BASE}/pending`);
        return response.data;
    },

    acknowledgeAlert: async (alertId) => {
        const response = await api.post(`${API_BASE}/${alertId}/acknowledge`);
        return response.data;
    },

    getNotifications: async () => {
        const response = await api.get(`${API_BASE}/notifications`);
        return response.data;
    },

    markNotificationRead: async (notificationId) => {
        const response = await api.post(`${API_BASE}/notifications/${notificationId}/read`);
        return response.data;
    },

    markAllNotificationsRead: async () => {
        const response = await api.post(`${API_BASE}/notifications/read-all`);
        return response.data;
    },

    getSessionAlerts: async (sessionId) => {
        const response = await api.get(`${API_BASE}/session/${sessionId}`);
        return response.data;
    }
};

export default alertService;