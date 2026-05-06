import React, { useContext } from 'react'
import { Navigate, Route, Routes, Link } from 'react-router-dom'
import { AuthContext } from './context/AuthContext'
import ProtectedRoute from './components/common/ProtectedRoute'
import SplashPage from './pages/SplashPage'
import LoginPage from './pages/auth/LoginPage'
import RegisterPage from './pages/auth/RegisterPage'
import StudentDashboard from './pages/StudentPortal/StudentDashboard'
import StudentAnalytics from './pages/StudentPortal/StudentAnalytics'
import StudentCourses from './pages/StudentPortal/StudentCourses'
import StudentProfile from './pages/StudentPortal/StudentProfile'
import StudentRecommendations from './pages/StudentPortal/StudentRecommendations'
import StudentConsent from './pages/StudentPortal/StudentConsent'
import LecturerDashboard from './pages/LecturerPortal/LecturerDashboard'
import LecturerCourses from './pages/LecturerPortal/LecturerCourses'
import LecturerLiveSession from './pages/LecturerPortal/LecturerLiveSession'
import LecturerSessions from './pages/LecturerPortal/LecturerSessions'
import LecturerProfile from './pages/LecturerPortal/LecturerProfile'
import LecturerReports from './pages/LecturerPortal/LecturerReports'
import AdminDashboard from './pages/AdminPortal/AdminDashboard'
import AdminUsers from './pages/AdminPortal/AdminUsers'
import AdminRoles from './pages/AdminPortal/AdminRoles'
import DeanDashboard from './components/DeanDashboard/DeanDashboard'
import FaceEnrollment from './components/AdminDashboard/FaceEnrollment'
import StartLiveSessionPage from './pages/StartLiveSessionPage'
import ManualAttendance from './components/LecturerDashboard/ManualAttendance'

function HomeRedirect() {
  const { token, role } = useContext(AuthContext)

  if (!token) {
    return <Navigate to="/login" replace />
  }

  const normalizedRole = String(role ?? '').toLowerCase()
  if (normalizedRole === 'lecturer') return <Navigate to="/lecturer" replace />
  if (normalizedRole === 'admin') return <Navigate to="/admin" replace />
  if (normalizedRole === 'dean') return <Navigate to="/dean" replace />
  return <Navigate to="/student" replace />
}

function UnauthorizedPage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-navy-950 text-slate-200 px-6">
      <div className="glass rounded-3xl p-8 max-w-lg w-full text-center">
        <h1 className="text-2xl font-bold mb-2">Unauthorized</h1>
        <p className="text-sm text-slate-400 mb-5">Your account does not have access to this route.</p>
        <Link to="/" className="btn-primary inline-flex">Go home</Link>
      </div>
    </div>
  )
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<SplashPage />} />
      <Route path="/home" element={<HomeRedirect />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/unauthorized" element={<UnauthorizedPage />} />

      <Route path="/student" element={<ProtectedRoute role="student"><StudentDashboard /></ProtectedRoute>} />
      <Route path="/student/analytics" element={<ProtectedRoute role="student"><StudentAnalytics /></ProtectedRoute>} />
      <Route path="/student/courses" element={<ProtectedRoute role="student"><StudentCourses /></ProtectedRoute>} />
      <Route path="/student/profile" element={<ProtectedRoute role="student"><StudentProfile /></ProtectedRoute>} />
      <Route path="/student/recommendations" element={<ProtectedRoute role="student"><StudentRecommendations /></ProtectedRoute>} />
      <Route path="/student/consent" element={<ProtectedRoute role="student"><StudentConsent /></ProtectedRoute>} />

      <Route path="/lecturer" element={<ProtectedRoute role="lecturer"><LecturerDashboard /></ProtectedRoute>} />
      <Route path="/lecturer/courses" element={<ProtectedRoute role="lecturer"><LecturerCourses /></ProtectedRoute>} />
      <Route path="/lecturer/live" element={<ProtectedRoute role="lecturer"><LecturerLiveSession /></ProtectedRoute>} />
      <Route path="/lecturer/sessions" element={<ProtectedRoute role="lecturer"><LecturerSessions /></ProtectedRoute>} />
      <Route path="/lecturer/reports" element={<ProtectedRoute role="lecturer"><LecturerReports /></ProtectedRoute>} />
      <Route path="/lecturer/profile" element={<ProtectedRoute role="lecturer"><LecturerProfile /></ProtectedRoute>} />

      <Route path="/admin" element={<ProtectedRoute role="admin"><AdminDashboard /></ProtectedRoute>} />
      <Route path="/admin/users" element={<ProtectedRoute role="admin"><AdminUsers /></ProtectedRoute>} />
      <Route path="/admin/roles" element={<ProtectedRoute role="admin"><AdminRoles /></ProtectedRoute>} />

      <Route path="/dean" element={<ProtectedRoute role="dean"><DeanDashboard /></ProtectedRoute>} />
      <Route path="/admin/face-enrollment" element={<ProtectedRoute role="admin"><FaceEnrollment /></ProtectedRoute>} />
      <Route path="/start-live-session" element={<ProtectedRoute role="lecturer"><StartLiveSessionPage /></ProtectedRoute>} />
      <Route path="/lecturer/courses/:courseId/manual-attendance" element={
  <ProtectedRoute role="lecturer">
    <ManualAttendance />
  </ProtectedRoute>
} />

      <Route path="*" element={<Navigate to="/home" replace />} />
    </Routes>
  )
}