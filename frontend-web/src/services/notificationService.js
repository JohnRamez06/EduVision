import api from './api'

const notificationService = {
	getUserNotifications: (userId) => api.get('/notifications', { params: { userId } }).then((response) => response.data),
	markAsRead: (id) => api.put(`/notifications/${id}/read`).then((response) => response.data),
	deleteNotification: (id) => api.delete(`/notifications/${id}`).then((response) => response.data),
}

export default notificationService
