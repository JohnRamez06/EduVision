import React, { useState, useContext } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { GraduationCap, Mail, Lock, Eye, EyeOff, ArrowRight, AlertCircle } from 'lucide-react'
import { AuthContext } from '../../context/AuthContext'
import authService from '../../services/authService'

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

export default function LoginPage() {
  const navigate = useNavigate()
  const { login } = useContext(AuthContext)

  const [form, setForm]         = useState({ email: '', password: '', role: 'student' })
  const [showPass, setShowPass] = useState(false)
  const [loading, setLoading]   = useState(false)
  const [error, setError]       = useState('')

  const set = (k, v) => { setForm(f => ({ ...f, [k]: v })); setError('') }

  const handleSubmit = async e => {
    e.preventDefault()
    if (!form.email || !form.password) { setError('Please fill in all fields.'); return }
    setLoading(true)
    try {
      const { user, token } = await authService.login(form.email, form.password)
      login(user, token)
      // redirect based on role returned by server, not the tab
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

      {/* Orbs */}
      <div className="orb w-[500px] h-[500px] bg-blue-600/15 -top-32 -left-32 animate-float-slow" />
      <div className="orb w-[400px] h-[400px] bg-violet-600/12 -bottom-24 -right-24 animate-float" />

      {/* Card */}
      <div className="relative z-10 w-full max-w-md animate-fade-up">
        <div className="glass rounded-3xl p-8 shadow-2xl">

          {/* Logo */}
          <div className="flex flex-col items-center mb-8">
            <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-blue-500 to-violet-600 flex items-center justify-center mb-4 shadow-lg shadow-blue-500/30 animate-glow-pulse">
              <GraduationCap size={26} className="text-white" />
            </div>
            <h1 className="text-2xl font-bold text-white tracking-tight">Welcome back</h1>
            <p className="text-sm text-slate-400 mt-1">Sign in to your EduVision account</p>
          </div>

          {/* Role selector */}
          <div className="flex gap-1.5 p-1 rounded-xl bg-navy-900/60 mb-6">
            {ROLES.map(r => (
              <button
                key={r.id}
                type="button"
                onClick={() => set('role', r.id)}
                className={`flex-1 py-2 rounded-lg text-xs font-semibold transition-all duration-200 ${
                  form.role === r.id
                    ? 'bg-gradient-to-r from-blue-500 to-violet-600 text-white shadow-md'
                    : 'text-slate-500 hover:text-slate-300'
                }`}
              >
                {r.label}
              </button>
            ))}
          </div>

          {/* Error */}
          {error && (
            <div className="flex items-center gap-2 px-4 py-3 mb-5 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
              <AlertCircle size={15} className="shrink-0" />
              {error}
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Email */}
            <div className="relative">
              <Mail size={15} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none" />
              <input
                type="email"
                placeholder="Email address"
                value={form.email}
                onChange={e => set('email', e.target.value)}
                className="input-field pl-10"
                autoComplete="email"
              />
            </div>

            {/* Password */}
            <div className="relative">
              <Lock size={15} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none" />
              <input
                type={showPass ? 'text' : 'password'}
                placeholder="Password"
                value={form.password}
                onChange={e => set('password', e.target.value)}
                className="input-field pl-10 pr-10"
                autoComplete="current-password"
              />
              <button
                type="button"
                onClick={() => setShowPass(s => !s)}
                className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300 transition-colors"
              >
                {showPass ? <EyeOff size={15} /> : <Eye size={15} />}
              </button>
            </div>

            {/* Forgot */}
            <div className="flex justify-end">
              <span className="text-xs text-slate-600 cursor-default">
                Forgot password? Contact your administrator.
              </span>
            </div>

            {/* Submit */}
            <button
              type="submit"
              disabled={loading}
              className="btn-primary w-full flex items-center justify-center gap-2 text-sm mt-2 disabled:opacity-60 disabled:cursor-not-allowed disabled:hover:shadow-none disabled:hover:transform-none"
            >
              {loading ? (
                <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                <>Sign In <ArrowRight size={15} /></>
              )}
            </button>
          </form>

          {/* Divider */}
          <div className="flex items-center gap-3 my-6">
            <div className="flex-1 h-px bg-slate-800" />
            <span className="text-xs text-slate-600">or</span>
            <div className="flex-1 h-px bg-slate-800" />
          </div>

          {/* Register link */}
          <p className="text-center text-sm text-slate-500">
            Don't have an account?{' '}
            <Link to="/register" className="text-blue-400 hover:text-blue-300 font-medium transition-colors">
              Create one
            </Link>
          </p>
        </div>

        {/* Back to splash */}
        <p className="text-center mt-5 text-xs text-slate-600">
          <Link to="/" className="hover:text-slate-400 transition-colors">← Back to home</Link>
        </p>
      </div>
    </div>
  )
}
