import api from './api'

const alertService = {
  getSessionAlerts: (sessionId) => api.get(`/alerts/session/${sessionId}`).then(r => r.data),
  acknowledge: (alertId) => api.put(`/alerts/${alertId}/acknowledge`).then(r => r.data),
  resolve: (alertId) => api.put(`/alerts/${alertId}/resolve`).then(r => r.data),
}

export default alertService