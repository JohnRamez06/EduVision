import api from './api'

const notificationService = {
  getAll: () => api.get('/notifications').then(r => r.data),
  getUnread: () => api.get('/notifications/unread').then(r => r.data),
  markRead: (id) => api.put(`/notifications/${id}/read`).then(r => r.data),
  delete: (id) => api.delete(`/notifications/${id}`).then(r => r.data),
}

export default notificationService