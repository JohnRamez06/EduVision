import api from './api'

const studentService = {
  getDashboard:       ()           => api.get('/facade/student/dashboard').then(r => r.data),
  getCourses:         ()           => api.get('/facade/student/courses').then(r => r.data),
  getSummaries:       ()           => api.get('/facade/student/summaries').then(r => r.data),
  getRecommendations: ()           => api.get('/facade/student/recommendations').then(r => r.data),
  getTimeline:        (sessionId)  => api.get(`/facade/student/lecture/${sessionId}/timeline`).then(r => r.data),
  getConsentStatus:   (studentId)  => api.get(`/consent/status/${studentId}`).then(r => r.data),
  grantConsent:       (policyId)   => api.post('/consent/grant', { policyId }).then(r => r.data),
  revokeConsent:      (policyId)   => api.post('/consent/revoke', { policyId }).then(r => r.data),
  getWeeklyReport:    (userId)     => api.get('/reports', { params: { userId } }).then(r => r.data),
}

export default studentService
