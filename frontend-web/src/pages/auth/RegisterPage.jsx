import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import authService from '../../services/authService'
import RegisterForm from '../../components/auth/RegisterForm'

export default function RegisterPage() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (form) => {
    const { firstName, lastName, email, password, confirmPassword, roleName } = form

    if (!firstName || !lastName || !email || !password || !confirmPassword) {
      setError('Please fill in all fields.')
      return
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match.')
      return
    }

    setLoading(true)
    setError('')
    try {
      await authService.register(firstName, lastName, email, password, roleName)
      navigate('/login')
    } catch (err) {
      setError(err.response?.data?.message ?? err.message ?? 'Registration failed.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="relative min-h-screen bg-navy-900 overflow-hidden bg-grid flex items-center justify-center px-4 py-10">
      <div className="orb w-[500px] h-[500px] bg-[#16254F]/15 -top-32 -left-32 animate-float-slow" />
      <div className="orb w-[400px] h-[400px] bg-[#16254F]/12 -bottom-24 -right-24 animate-float" />

      <div className="relative z-10 w-full max-w-md animate-fade-up">
        <RegisterForm onSubmit={handleSubmit} loading={loading} error={error} />
        <div className="flex items-center justify-between mt-5 text-xs text-slate-600">
          <Link to="/login" className="hover:text-slate-400 transition-colors">Sign in</Link>
          <Link to="/" className="hover:text-slate-400 transition-colors">Back to home</Link>
        </div>
      </div>
    </div>
  )
}