import api from './api'

const lecturerService = {
  // Get lecturer's assigned courses
  getCourses: () => 
    api.get('/lecturers/courses').then(r => r.data),

  // Get lecturer profile
  getProfile: () => 
    api.get('/lecturers/profile').then(r => r.data),

  // Get full dashboard data
  getDashboard: () => 
    api.get('/facade/lecturer/dashboard').then(r => r.data),

  // Get students in a session
  getSessionStudents: (sessionId) =>
    api.get(`/facade/lecturer/sessions/${sessionId}/students`).then(r => r.data),

  // Get face-detected students for a live session
  getDetectedStudents: (sessionId) =>
    api.get(`/facade/lecturer/sessions/${sessionId}/detected-students`).then(r => r.data),

  // Get session history
  getSessionHistory: () =>
    api.get('/facade/lecturer/dashboard').then(r => r.data).then(d => d.recentSessions ?? []),

  // Analytics
  getSessionFocusTimeline:      (sessionId) => api.get(`/facade/lecturer/sessions/${sessionId}/focus-timeline`).then(r => r.data),
  getCourseAtRiskStudents:      (courseId)  => api.get(`/facade/lecturer/courses/${courseId}/at-risk`).then(r => r.data),
  getCourseLectureComparison:   (courseId)  => api.get(`/facade/lecturer/courses/${courseId}/lecture-comparison`).then(r => r.data),
  getCourseBehavioralPatterns:  (courseId)  => api.get(`/facade/lecturer/courses/${courseId}/behavioral-patterns`).then(r => r.data),
  getCourseAIPredictions:       (courseId)  => api.get(`/facade/lecturer/courses/${courseId}/ai-predictions`).then(r => r.data),
}

export default lecturerService