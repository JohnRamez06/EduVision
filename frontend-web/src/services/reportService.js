import api from './api'

const reportService = {
  generateStudent: (studentId, weekId) =>
    api.post(`/reports/student/${studentId}/weekly/${weekId}`).then(r => r.data),
  generateLecturer: (lecturerId, weekId) =>
    api.post(`/reports/lecturer/${lecturerId}/weekly/${weekId}`).then(r => r.data),
  generateDean: (weekId) =>
    api.post(`/reports/dean/weekly/${weekId}`).then(r => r.data),
  generateSession: (sessionId) =>
    api.post(`/reports/session/${sessionId}`).then(r => r.data),
  download: (fileName) =>
    api.get(`/reports/download/${fileName}`, { responseType: 'blob' }),
}

export default reportService