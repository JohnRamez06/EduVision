import api from './api'

const attendanceService = {
  recordExit: (data) => api.post('/attendance/exit', data).then(r => r.data),
  recordReturn: (data) => api.post('/attendance/return', data).then(r => r.data),
  getSessionExits: (sessionId) => api.get(`/attendance/session/${sessionId}/exits`).then(r => r.data),
  getStudentSessionAttendance: (studentId, sessionId) =>
    api.get(`/attendance/student/${studentId}/session/${sessionId}`).then(r => r.data),
  getWeeklyAttendance: (weekId) => api.get(`/attendance/weekly/${weekId}`).then(r => r.data),
  getCourseWeeklyAttendance: (courseId, weekId) =>
    api.get(`/attendance/course/${courseId}/weekly/${weekId}`).then(r => r.data),
}

export default attendanceService