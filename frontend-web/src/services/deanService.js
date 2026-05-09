import api from "./api"; // Must exist in src/services/api.js

/** Department summary for dean dashboard */
export const getDashboard = async () => {
  const res = await api.get("/facade/dean/dashboard");
  return res.data;
};

/** Lecturer performance/rankings */
export const getLecturerPerformance = async () => {
  const res = await api.get("/facade/dean/lecturer-performance");
  return res.data;
};

/** Per-course statistics */
export const getCourseStats = async () => {
  const res = await api.get("/facade/dean/course-stats");
  return res.data;
};

/** Weekly trend data (last 12 weeks) */
export const getWeeklyTrends = async () => {
  const res = await api.get("/facade/dean/weekly-trends");
  return res.data;
};

export const getAttendanceAnalytics = () =>
  api.get("/facade/dean/reports/attendance").then(r => r.data);

export const getStudentFocusReport = () =>
  api.get("/facade/dean/reports/focus").then(r => r.data);

export const getPeakActivityReport = () =>
  api.get("/facade/dean/reports/peak-activity").then(r => r.data);