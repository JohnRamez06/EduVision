import api from './api'

const cameraService = {
	getConfigs: (userId) => api.get('/camera/configs', { params: userId ? { userId } : undefined }).then((response) => response.data),
	createConfig: (body) => api.post('/camera/configs', body).then((response) => response.data),
	updateConfig: (id, body) => api.put(`/camera/${id}`, body).then((response) => response.data),
	deleteConfig: (id) => api.delete(`/camera/${id}`).then((response) => response.data),
	testConnection: (id, body) => api.post(`/camera/${id}/test`, body).then((response) => response.data),
}

export default cameraService
