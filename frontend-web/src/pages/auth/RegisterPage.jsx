import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { GraduationCap, User, Mail, Lock, Eye, EyeOff, ArrowRight, AlertCircle, CheckCircle } from 'lucide-react'
import authService from '../../services/authService'

const ROLES = [
  { id: 'student',  label: 'Student',  desc: 'Track my engagement' },
  { id: 'lecturer', label: 'Lecturer', desc: 'Monitor my class' },
]

export default function RegisterPage() {
  const navigate = useNavigate()

  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', password: '', confirm: '', role: 'student' })
  const [showPass, setShowPass]     = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)
  const [loading, setLoading]       = useState(false)
  const [error, setError]           = useState('')
  const [success, setSuccess]       = useState(false)

  const set = (k, v) => { setForm(f => ({ ...f, [k]: v })); setError('') }

  const passwordStrength = pw => {
    let s = 0
    if (pw.length >= 8) s++
    if (/[A-Z]/.test(pw)) s++
    if (/[0-9]/.test(pw)) s++
    if (/[^A-Za-z0-9]/.test(pw)) s++
    return s
  }

  const strength = passwordStrength(form.password)
  const strengthLabel = ['', 'Weak', 'Fair', 'Good', 'Strong'][strength]
  const strengthColor = ['', 'bg-rose-500', 'bg-amber-500', 'bg-blue-500', 'bg-emerald-500'][strength]

  const handleSubmit = async e => {
    e.preventDefault()
    if (!form.firstName || !form.lastName || !form.email || !form.password || !form.confirm) {
      setError('Please fill in all fields.'); return
    }
    if (form.password !== form.confirm) {
      setError('Passwords do not match.'); return
    }
    if (strength < 2) {
      setError('Please choose a stronger password.'); return
    }
    setLoading(true)
    try {
      await authService.register(form.firstName, form.lastName, form.email, form.password, form.role)
      setSuccess(true)
      setTimeout(() => navigate('/login'), 2200)
    } catch (err) {
      const msg = err.response?.data?.message ?? err.message ?? 'Registration failed.'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  if (success) {
    return (
      <div className="min-h-screen bg-navy-900 bg-grid flex items-center justify-center px-4">
        <div className="glass rounded-3xl p-10 max-w-sm w-full text-center animate-fade-up">
          <div className="w-16 h-16 rounded-full bg-emerald-500/15 border border-emerald-500/30 flex items-center justify-center mx-auto mb-5">
            <CheckCircle size={30} className="text-emerald-400" />
          </div>
          <h2 className="text-xl font-bold text-white mb-2">Account created!</h2>
          <p className="text-sm text-slate-400">Redirecting you to sign in…</p>
        </div>
      </div>
    )
  }

  return (
    <div className="relative min-h-screen bg-navy-900 overflow-hidden bg-grid flex items-center justify-center px-4 py-10">

      {/* Orbs */}
      <div className="orb w-[450px] h-[450px] bg-violet-600/15 -top-28 -right-28 animate-float" />
      <div className="orb w-[380px] h-[380px] bg-blue-600/12 -bottom-20 -left-20 animate-float-slow" />

      <div className="relative z-10 w-full max-w-md animate-fade-up">
        <div className="glass rounded-3xl p-8 shadow-2xl">

          {/* Logo */}
          <div className="flex flex-col items-center mb-8">
            <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-blue-500 to-violet-600 flex items-center justify-center mb-4 shadow-lg shadow-violet-500/30 animate-glow-pulse">
              <GraduationCap size={26} className="text-white" />
            </div>
            <h1 className="text-2xl font-bold text-white tracking-tight">Create account</h1>
            <p className="text-sm text-slate-400 mt-1">Join EduVision today</p>
          </div>

          {/* Role selector */}
          <div className="grid grid-cols-2 gap-3 mb-6">
            {ROLES.map(r => (
              <button
                key={r.id}
                type="button"
                onClick={() => set('role', r.id)}
                className={`p-3.5 rounded-xl text-left transition-all duration-200 border ${
                  form.role === r.id
                    ? 'border-blue-500/60 bg-blue-500/10'
                    : 'border-slate-700/50 hover:border-slate-600'
                }`}
              >
                <p className={`text-sm font-semibold mb-0.5 ${form.role === r.id ? 'text-blue-300' : 'text-slate-300'}`}>
                  {r.label}
                </p>
                <p className="text-xs text-slate-500">{r.desc}</p>
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
            {/* Name row */}
            <div className="grid grid-cols-2 gap-3">
              <div className="relative">
                <User size={15} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none" />
                <input
                  type="text"
                  placeholder="First name"
                  value={form.firstName}
                  onChange={e => set('firstName', e.target.value)}
                  className="input-field pl-10"
                  autoComplete="given-name"
                />
              </div>
              <div className="relative">
                <input
                  type="text"
                  placeholder="Last name"
                  value={form.lastName}
                  onChange={e => set('lastName', e.target.value)}
                  className="input-field px-4"
                  autoComplete="family-name"
                />
              </div>
            </div>

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
            <div>
              <div className="relative">
                <Lock size={15} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none" />
                <input
                  type={showPass ? 'text' : 'password'}
                  placeholder="Password"
                  value={form.password}
                  onChange={e => set('password', e.target.value)}
                  className="input-field pl-10 pr-10"
                  autoComplete="new-password"
                />
                <button
                  type="button"
                  onClick={() => setShowPass(s => !s)}
                  className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300 transition-colors"
                >
                  {showPass ? <EyeOff size={15} /> : <Eye size={15} />}
                </button>
              </div>

              {/* Strength bar */}
              {form.password && (
                <div className="mt-2 flex items-center gap-2">
                  <div className="flex gap-1 flex-1">
                    {[1,2,3,4].map(i => (
                      <div
                        key={i}
                        className={`h-1 flex-1 rounded-full transition-all duration-300 ${i <= strength ? strengthColor : 'bg-slate-700'}`}
                      />
                    ))}
                  </div>
                  <span className="text-xs text-slate-500">{strengthLabel}</span>
                </div>
              )}
            </div>

            {/* Confirm password */}
            <div className="relative">
              <Lock size={15} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none" />
              <input
                type={showConfirm ? 'text' : 'password'}
                placeholder="Confirm password"
                value={form.confirm}
                onChange={e => set('confirm', e.target.value)}
                className={`input-field pl-10 pr-10 ${
                  form.confirm && form.confirm !== form.password ? 'border-rose-500/50' : ''
                }`}
                autoComplete="new-password"
              />
              <button
                type="button"
                onClick={() => setShowConfirm(s => !s)}
                className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300 transition-colors"
              >
                {showConfirm ? <EyeOff size={15} /> : <Eye size={15} />}
              </button>
            </div>

            {/* Terms */}
            <p className="text-xs text-slate-500 text-center px-2">
              By creating an account you agree to our{' '}
              <span className="text-blue-400 cursor-pointer hover:text-blue-300">Terms</span>
              {' '}and{' '}
              <span className="text-blue-400 cursor-pointer hover:text-blue-300">Privacy Policy</span>.
            </p>

            {/* Submit */}
            <button
              type="submit"
              disabled={loading}
              className="btn-primary w-full flex items-center justify-center gap-2 text-sm disabled:opacity-60 disabled:cursor-not-allowed disabled:hover:shadow-none disabled:hover:transform-none"
            >
              {loading ? (
                <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                <>Create Account <ArrowRight size={15} /></>
              )}
            </button>
          </form>

          <div className="flex items-center gap-3 my-6">
            <div className="flex-1 h-px bg-slate-800" />
            <span className="text-xs text-slate-600">already a member?</span>
            <div className="flex-1 h-px bg-slate-800" />
          </div>

          <p className="text-center text-sm text-slate-500">
            <Link to="/login" className="text-blue-400 hover:text-blue-300 font-medium transition-colors">
              Sign in instead
            </Link>
          </p>
        </div>

        <p className="text-center mt-5 text-xs text-slate-600">
          <Link to="/" className="hover:text-slate-400 transition-colors">← Back to home</Link>
        </p>
      </div>
    </div>
  )
}
