import api from './api'

// PDF generation via R + LaTeX can take 30–90 s on first run.
const GENERATE_TIMEOUT = 120_000  // 2 minutes

const reportService = {
  generateStudent: (studentId, weekId) =>
    api.post(`/reports/student/${studentId}/weekly/${weekId}`, {}, { timeout: GENERATE_TIMEOUT }).then(r => r.data),
  generateLecturer: (lecturerId, weekId) =>
    api.post(`/reports/lecturer/${lecturerId}/weekly/${weekId}`, {}, { timeout: GENERATE_TIMEOUT }).then(r => r.data),
  generateDean: (weekId) =>
    api.post(`/reports/dean/weekly/${weekId}`, {}, { timeout: GENERATE_TIMEOUT }).then(r => r.data),
  generateSession: (sessionId) =>
    api.post(`/reports/session/${sessionId}`, {}, { timeout: GENERATE_TIMEOUT }).then(r => r.data),
  download: (fileName) =>
    api.get(`/reports/download/${fileName}`, { responseType: 'blob' }),
  getMyReports: () =>
    api.get('/reports/my').then(r => r.data),
}

export default reportService
