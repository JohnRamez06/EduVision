import React, { useContext, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { AuthContext } from '../../context/AuthContext'
import authService from '../../services/authService'
import LoginForm from '../../components/auth/LoginForm'
import ThemeToggle from '../../components/common/ThemeToggle'

const ROLES = [
  { id: 'student',  label: 'Student' },
  { id: 'lecturer', label: 'Lecturer' },
  { id: 'dean',     label: 'Dean' },
  { id: 'admin',    label: 'Admin' },
]

const ROLE_REDIRECT = {
  student:  '/student',
  lecturer: '/lecturer',
  dean:     '/dean',
  admin:    '/admin',
}
const handleLogin = async (email, password) => {
    try {
        const response = await authService.login(email, password);
        const { token, role } = response.data;
        
        // Store token and role
        localStorage.setItem('token', token);
        localStorage.setItem('role', role);
        
        // Redirect based on role
        if (role === 'lecturer') {
            navigate('/lecturer');
        } else if (role === 'student') {
            navigate('/student');
        } else if (role === 'dean') {
            navigate('/dean');
        } else if (role === 'admin') {
            navigate('/admin');
        } else {
            navigate('/');
        }
    } catch (error) {
        console.error('Login failed:', error);
    }
};

export default function LoginPage() {
  const navigate = useNavigate()
  const { login } = useContext(AuthContext)

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async ({ email, password }) => {
    if (!email || !password) {
      setError('Please fill in all fields.')
      return
    }
    setLoading(true)
    try {
      const data = await authService.login(email, password)
      const token = data.token
      const user = {
        email: data.email,
        fullName: data.fullName,
        roles: data.roles,
        role: data.roles?.[0]?.toLowerCase() ?? 'student',
      }
      login(user, token)
      navigate(ROLE_REDIRECT[user.role] ?? '/')
    } catch (err) {
      const msg = err.response?.data?.message ?? err.message ?? 'Invalid credentials.'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="relative min-h-screen bg-navy-900 overflow-hidden bg-grid flex items-center justify-center px-4">
      <div className="orb w-[500px] h-[500px] bg-blue-600/15 -top-32 -left-32 animate-float-slow" />
      <div className="orb w-[400px] h-[400px] bg-violet-600/12 -bottom-24 -right-24 animate-float" />
      <div className="absolute top-5 right-5 z-20">
        <ThemeToggle />
      </div>

      <div className="relative z-10 w-full max-w-md animate-fade-up">
        <LoginForm onSubmit={handleSubmit} loading={loading} error={error} />
        <div className="flex items-center justify-between mt-5 text-xs text-slate-600">
          <Link to="/register" className="hover:text-slate-400 transition-colors">Create one</Link>
          <Link to="/" className="hover:text-slate-400 transition-colors">Back to home</Link>
        </div>
      </div>
    </div>
  )
}
