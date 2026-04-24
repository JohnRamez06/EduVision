import api from './api'

const lecturerService = {
  getDashboard:       ()           => api.get('/facade/lecturer/dashboard').then(r => r.data),
  getSessionStudents: (sessionId)  => api.get(`/facade/lecturer/sessions/${sessionId}/students`).then(r => r.data),
}

export default lecturerService
