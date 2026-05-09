import api from './api'

const BASE = 'http://localhost:8080/api/v1'

function getUserId() {
  try {
    const u = JSON.parse(localStorage.getItem('eduvision.user') || '{}')
    return u.userId || u.id || ''
  } catch { return '' }
}

const reportService = {
  // Legacy R-based (may fail if pandoc not installed)
  generateLecturer: (lecturerId, weekId) =>
    api.post(`/reports/lecturer/${lecturerId}/weekly/${weekId}`, {}, { timeout: 120000 }).then(r => r.data),

  // Java HTML reports — public URLs, IDs in path, always work
  openSessionReport:        (sessionId) => {
    const sid = getUserId()
    window.open(`${BASE}/html-reports/student/${sid}/session/${sessionId}`, '_blank')
  },
  openLecturerCourseReport: (courseId)  => {
    const lid = getUserId()
    window.open(`${BASE}/html-reports/lecturer/${lid}/course/${courseId}`, '_blank')
  },

  getMyReports: () => api.get('/reports/my').then(r => r.data),
}

export default reportService
