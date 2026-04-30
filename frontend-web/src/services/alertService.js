import api from './api'

const alertService = {
	getAlertsBySession: (sessionId) => api.get(`/alerts/session/${sessionId}`).then((response) => response.data),
	acknowledgeAlert: (id, userId) => api.put(`/alerts/${id}/acknowledge`, null, { params: { userId } }).then((response) => response.data),
	resolveAlert: (id, userId) => api.put(`/alerts/${id}/resolve`, null, { params: { userId } }).then((response) => response.data),
}

export default alertService
