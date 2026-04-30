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

  // Get session history
  getSessionHistory: () => 
    api.get('/facade/lecturer/dashboard').then(r => r.data).then(d => d.recentSessions ?? []),
}

export default lecturerService