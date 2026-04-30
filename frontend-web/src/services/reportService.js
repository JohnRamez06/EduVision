import api from './api'

const reportService = {
	generateReport: (body) => api.post('/reports/generate', body).then((response) => response.data),
	getUserReports: (userId) => api.get('/reports', { params: { userId } }).then((response) => response.data),
	getReportStatus: (id) => api.get(`/reports/${id}/status`).then((response) => response.data),
	downloadReport: async (id) => {
		const response = await api.get(`/reports/${id}/download`, { responseType: 'blob' })
		return response.data
	},
}

export default reportService
