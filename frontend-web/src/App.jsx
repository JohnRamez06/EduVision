import React, { useContext } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthContext } from './context/AuthContext'
import LoginForm from './components/auth/LoginForm'
import LecturerDashboard from './components/LecturerDashboard/LecturerDashboard'
import StudentDashboard from './components/StudentPortal/StudentDashboard'
import DeanDashboard from './components/DeanDashboard/DeanDashboard'
import AdminDashboard from './components/AdminDashboard/AdminDashboard'
import ProtectedRoute from './components/common/ProtectedRoute'
export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginForm/>} />
      <Route path="/lecturer" element={<ProtectedRoute role="lecturer"><LecturerDashboard/></ProtectedRoute>} />
      <Route path="/student"  element={<ProtectedRoute role="student"><StudentDashboard/></ProtectedRoute>} />
      <Route path="/dean"     element={<ProtectedRoute role="dean"><DeanDashboard/></ProtectedRoute>} />
      <Route path="/admin"    element={<ProtectedRoute role="admin"><AdminDashboard/></ProtectedRoute>} />
      <Route path="*" element={<Navigate to="/login"/>} />
    </Routes>
  )
}
