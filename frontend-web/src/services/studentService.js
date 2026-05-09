import api from './api'
import { openReport } from '../utils/reportOpener'

const studentService = {
  getDashboard:          ()           => api.get('/facade/student/dashboard').then(r => r.data),
  getCourses:            ()           => api.get('/facade/student/courses').then(r => r.data),
  getSummaries:          ()           => api.get('/facade/student/summaries').then(r => r.data),
  getRecommendations:    ()           => api.get('/facade/student/recommendations').then(r => r.data),
  getTimeline:           (sessionId)  => api.get(`/facade/student/lecture/${sessionId}/timeline`).then(r => r.data),
  getCourseAnalytics:    (courseId)   => api.get(`/facade/student/courses/${courseId}/analytics`).then(r => r.data),
  getConsentStatus:      (studentId)  => api.get(`/consent/status/${studentId}`).then(r => r.data),
  grantConsent:          (policyId)   => api.post('/consent/grant', { policyId }).then(r => r.data),
  revokeConsent:         (policyId)   => api.post('/consent/revoke', { policyId }).then(r => r.data),
  getMyReports:          ()           => api.get('/reports/my').then(r => r.data),

  // HTML reports — uses existing /facade/student/** endpoint (already authenticated)
  // Opens a tab synchronously first to avoid popup blockers, then writes HTML via axios
  openSessionReport:   (sessionId) => openReport(`/facade/student/report/session/${sessionId}`),
  openDashboardReport: ()          => openReport('/facade/student/report/dashboard'),
}

export default studentService
