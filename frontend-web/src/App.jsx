import React, { useContext } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthContext } from './context/AuthContext'
import ProtectedRoute from './components/common/ProtectedRoute'

import SplashPage              from './pages/SplashPage'
import LoginPage               from './pages/auth/LoginPage'
import RegisterPage            from './pages/auth/RegisterPage'
import StudentDashboard        from './pages/StudentPortal/StudentDashboard'
import StudentCourses          from './pages/StudentPortal/StudentCourses'
import StudentAnalytics        from './pages/StudentPortal/StudentAnalytics'
import StudentRecommendations  from './pages/StudentPortal/StudentRecommendations'
import StudentConsent          from './pages/StudentPortal/StudentConsent'
import StudentProfile          from './pages/StudentPortal/StudentProfile'

import LecturerDashboard    from './pages/LecturerPortal/LecturerDashboard'
import LecturerCourses      from './pages/LecturerPortal/LecturerCourses'
import LecturerSessions     from './pages/LecturerPortal/LecturerSessions'
import LecturerProfile      from './pages/LecturerPortal/LecturerProfile'
import LecturerLiveSession  from './pages/LecturerPortal/LecturerLiveSession'

import AdminDashboard from './pages/AdminPortal/AdminDashboard'
import AdminUsers     from './pages/AdminPortal/AdminUsers'
import AdminRoles     from './pages/AdminPortal/AdminRoles'

const ROLE_REDIRECT = {
  student:  '/student',
  lecturer: '/lecturer',
  dean:     '/dean',
  admin:    '/admin',
}

const Guard = ({ role, children }) => (
  <ProtectedRoute role={role}>{children}</ProtectedRoute>
)

export default function App() {
  const { token, user } = useContext(AuthContext)
  const home = token ? (ROLE_REDIRECT[user?.role] ?? '/student') : null

  return (
    <Routes>
      {/* Public */}
      <Route path="/"         element={token ? <Navigate to={home} replace /> : <SplashPage />} />
      <Route path="/login"    element={token ? <Navigate to={home} replace /> : <LoginPage />} />
      <Route path="/register" element={token ? <Navigate to={home} replace /> : <RegisterPage />} />

      {/* Student portal */}
      <Route path="/student"                  element={<Guard role="student"><StudentDashboard /></Guard>} />
      <Route path="/student/courses"          element={<Guard role="student"><StudentCourses /></Guard>} />
      <Route path="/student/analytics"        element={<Guard role="student"><StudentAnalytics /></Guard>} />
      <Route path="/student/recommendations"  element={<Guard role="student"><StudentRecommendations /></Guard>} />
      <Route path="/student/consent"          element={<Guard role="student"><StudentConsent /></Guard>} />
      <Route path="/student/profile"          element={<Guard role="student"><StudentProfile /></Guard>} />

      {/* Lecturer portal */}
      <Route path="/lecturer"          element={<Guard role="lecturer"><LecturerDashboard /></Guard>} />
      <Route path="/lecturer/courses"  element={<Guard role="lecturer"><LecturerCourses /></Guard>} />
      <Route path="/lecturer/live"     element={<Guard role="lecturer"><LecturerLiveSession /></Guard>} />
      <Route path="/lecturer/sessions" element={<Guard role="lecturer"><LecturerSessions /></Guard>} />
      <Route path="/lecturer/profile"  element={<Guard role="lecturer"><LecturerProfile /></Guard>} />

      {/* Admin portal */}
      <Route path="/admin"        element={<Guard role="admin"><AdminDashboard /></Guard>} />
      <Route path="/admin/users"  element={<Guard role="admin"><AdminUsers /></Guard>} />
      <Route path="/admin/roles"  element={<Guard role="admin"><AdminRoles /></Guard>} />

      {/* Placeholders */}
      <Route path="/dean/*" element={<Guard role="dean"><ComingSoon label="Dean" /></Guard>} />

      {/* Fallbacks */}
      <Route path="/unauthorized" element={
        <div className="min-h-screen bg-navy-900 flex items-center justify-center text-rose-400 text-sm">
          Access denied — you do not have permission to view this page.
        </div>
      } />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

function ComingSoon({ label }) {
  return (
    <div className="min-h-screen bg-navy-900 flex items-center justify-center text-slate-400 text-sm">
      {label} dashboard — coming soon
    </div>
  )
}
