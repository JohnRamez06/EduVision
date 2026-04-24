import React, { useContext } from 'react'
import { Navigate } from 'react-router-dom'
import { AuthContext } from '../../context/AuthContext'

export default function ProtectedRoute({ children, role }) {
  const { token, user } = useContext(AuthContext)
  if (!token) return <Navigate to="/login" replace />
  if (role && user?.role) {
    // normalise — backend may return "STUDENT" or "ROLE_STUDENT"
    const userRole = user.role.replace(/^ROLE_/i, '').toLowerCase()
    if (userRole !== role.toLowerCase()) return <Navigate to="/unauthorized" replace />
  }
  return children
}
