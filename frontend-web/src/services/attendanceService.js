// frontend-web/src/services/attendanceService.js
import api from './api';

const attendanceService = {
  // Get available weeks for a course
  getAvailableWeeks: async (courseId) => {
    const response = await api.get(`/attendance/manual/weeks?courseId=${courseId}`);
    return response.data;
  },

  // Get students for manual attendance
  getStudentsForManualAttendance: async (courseId, weekId) => {
    const response = await api.get(`/attendance/manual/students?courseId=${courseId}&weekId=${weekId}`);
    return response.data;
  },

  // Save manual attendance
  saveManualAttendance: async (payload) => {
    const response = await api.post('/attendance/manual/save', payload);
    return response.data;
  },

  // Get all exit logs for a session (used by StudentExitList)
  getSessionExits: async (sessionId) => {
    const response = await api.get(`/attendance/session/${sessionId}/exits`);
    return response.data;
  },
};

export default attendanceService;