import adminService from './adminService'
import lecturerService from './lecturerService'
import reportService from './reportService'
import studentService from './studentService'
import api from './api'

const normalizeScore = (value) => {
  const numeric = Number(value)
  if (!Number.isFinite(numeric)) return null
  return numeric <= 1 ? Math.round(numeric * 100) : Math.round(numeric)
}

const safeDepartmentName = (value) => value ?? 'General'

async function buildFallbackDashboard() {
  const [users, roles, studentDashboard, lecturerDashboard] = await Promise.all([
    adminService.getUsers().catch(() => []),
    adminService.getRoles().catch(() => []),
    studentService.getDashboard().catch(() => null),
    lecturerService.getDashboard().catch(() => null),
  ])

  const lecturers = users.filter((user) => user.roles?.some((role) => role.toLowerCase() === 'lecturer'))
  const students = users.filter((user) => user.roles?.some((role) => role.toLowerCase() === 'student'))
  const courseEngagement = (lecturerDashboard?.courses ?? []).map((course) => ({
    name: course.code ?? course.title ?? 'Course',
    avgEngagement: normalizeScore(course.avgEngagement ?? course.attendanceRate ?? course.engagement),
    attendanceRate: normalizeScore(
      course.attendedSessions != null && course.totalSessions > 0
        ? course.attendedSessions / course.totalSessions
        : course.attendanceRate,
    ),
  }))

  const weeklyReports = (studentDashboard?.recentSummaries ?? []).map((summary) => ({
    week: summary.date ?? summary.week ?? 'Recent',
    avgEngagement: normalizeScore(summary.avgConcentration ?? summary.avgEngagement),
    alertCount: summary.alertCount ?? 0,
  }))

  return {
    summary: {
      totalUsers: users.length,
      totalStudents: students.length,
      totalLecturers: lecturers.length,
      totalRoles: roles.length,
      totalCourses: lecturerDashboard?.courses?.length ?? lecturerDashboard?.totalCourses ?? 0,
      totalSessions: lecturerDashboard?.totalSessions ?? studentDashboard?.overallStats?.totalLecturesAttended ?? 0,
      activeSessions: lecturerDashboard?.activeSessions ?? 0,
      avgEngagement: normalizeScore(
        lecturerDashboard?.recentSessions?.length
          ? lecturerDashboard.recentSessions.reduce((sum, session) => sum + (Number(session.avgEngagement) || 0), 0) / lecturerDashboard.recentSessions.length
          : studentDashboard?.overallStats?.avgConcentration,
      ),
      avgAttendance: normalizeScore(studentDashboard?.overallStats?.avgAttentiveness),
    },
    departmentOverview: [
      {
        department: safeDepartmentName(studentDashboard?.studentInfo?.program),
        studentCount: students.length,
        lecturerCount: lecturers.length,
        avgEngagement: normalizeScore(studentDashboard?.overallStats?.avgConcentration),
      },
    ],
    lecturerComparison: [],
    courseEngagement,
    weeklyReports,
    recentReports: [],
  }
}

const deanService = {
  async getDashboard() {
    try {
      const response = await api.get('/facade/dean/dashboard')
      return response.data
    } catch {
      return buildFallbackDashboard()
    }
  },

  async searchStudent(studentId) {
    if (!studentId) return null

    try {
      const response = await api.get(`/facade/dean/students/${encodeURIComponent(studentId)}`)
      return response.data
    } catch {
      return studentService.getDashboard().then((dashboard) => {
        if (!dashboard?.studentInfo) return null
        return String(dashboard.studentInfo.id ?? dashboard.studentInfo.studentNumber ?? '') === String(studentId)
          ? dashboard
          : null
      })
    }
  },
  getWeeklyReports: (userId) => reportService.getUserReports(userId),
}

export default deanService