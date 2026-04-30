import React, { useMemo, useState } from 'react'
import { ArrowRight, Eye, EyeOff, GraduationCap, Lock, Mail, User } from 'lucide-react'

const ROLE_OPTIONS = [
  { value: 'student', label: 'Student' },
  { value: 'lecturer', label: 'Lecturer' },
  { value: 'admin', label: 'Admin' },
]

export default function RegisterForm({ onSubmit, loading = false, error = '' }) {
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    roleName: 'student',
  })

  const strength = useMemo(() => {
    let score = 0
    if (form.password.length >= 8) score += 1
    if (/[A-Z]/.test(form.password)) score += 1
    if (/[0-9]/.test(form.password)) score += 1
    if (/[^A-Za-z0-9]/.test(form.password)) score += 1
    return score
  }, [form.password])

  const handleSubmit = (event) => {
    event.preventDefault()
    onSubmit?.(form)
  }

  return (
    <div className="glass rounded-3xl p-8 shadow-2xl">
      <div className="flex flex-col items-center mb-8">
        <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-blue-500 to-violet-600 flex items-center justify-center mb-4 shadow-lg shadow-violet-500/30">
          <GraduationCap size={26} className="text-white" />
        </div>
        <h1 className="text-2xl font-bold text-white tracking-tight">Create account</h1>
        <p className="text-sm text-slate-400 mt-1">Join EduVision today</p>
      </div>

      {error ? (
        <div className="flex items-center gap-2 px-4 py-3 mb-5 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
          <span className="text-xs font-bold">!</span>
          {error}
        </div>
      ) : null}

      <div className="grid grid-cols-3 gap-2 mb-6">
        {ROLE_OPTIONS.map((role) => (
          <button
            key={role.value}
            type="button"
            onClick={() => setForm((current) => ({ ...current, roleName: role.value }))}
            className={`py-2 rounded-lg text-xs font-semibold transition-all duration-200 ${form.roleName === role.value ? 'bg-gradient-to-r from-blue-500 to-violet-600 text-white shadow-md' : 'text-slate-500 hover:text-slate-300 bg-slate-900/40'}`}
          >
            {role.label}
          </button>
        ))}
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-2 gap-3">
          <div className="relative">
            <User size={15} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none" />
            <input
              type="text"
              placeholder="First name"
              value={form.firstName}
              onChange={(event) => setForm((current) => ({ ...current, firstName: event.target.value }))}
              className="input-field pl-10"
              autoComplete="given-name"
            />
          </div>
          <div className="relative">
            <input
              type="text"
              placeholder="Last name"
              value={form.lastName}
              onChange={(event) => setForm((current) => ({ ...current, lastName: event.target.value }))}
              className="input-field"
              autoComplete="family-name"
            />
          </div>
        </div>

        <div className="relative">
          <Mail size={15} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none" />
          <input
            type="email"
            placeholder="Email address"
            value={form.email}
            onChange={(event) => setForm((current) => ({ ...current, email: event.target.value }))}
            className="input-field pl-10"
            autoComplete="email"
          />
        </div>

        <div>
          <div className="relative">
            <Lock size={15} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none" />
            <input
              type={showPassword ? 'text' : 'password'}
              placeholder="Password"
              value={form.password}
              onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))}
              className="input-field pl-10 pr-10"
              autoComplete="new-password"
            />
            <button
              type="button"
              onClick={() => setShowPassword((current) => !current)}
              className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300 transition-colors"
            >
              {showPassword ? <EyeOff size={15} /> : <Eye size={15} />}
            </button>
          </div>
          <div className="mt-2 flex gap-1">
            {[1, 2, 3, 4].map((step) => (
              <div
                key={step}
                className={`h-1 flex-1 rounded-full ${step <= strength ? 'bg-emerald-500' : 'bg-slate-700'}`}
              />
            ))}
          </div>
        </div>

        <div className="relative">
          <Lock size={15} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none" />
          <input
            type={showConfirm ? 'text' : 'password'}
            placeholder="Confirm password"
            value={form.confirmPassword}
            onChange={(event) => setForm((current) => ({ ...current, confirmPassword: event.target.value }))}
            className="input-field pl-10 pr-10"
            autoComplete="new-password"
          />
          <button
            type="button"
            onClick={() => setShowConfirm((current) => !current)}
            className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300 transition-colors"
          >
            {showConfirm ? <EyeOff size={15} /> : <Eye size={15} />}
          </button>
        </div>

        <button
          type="submit"
          disabled={loading}
          className="btn-primary w-full flex items-center justify-center gap-2 text-sm disabled:opacity-60 disabled:cursor-not-allowed"
        >
          {loading ? <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" /> : <>Create Account <ArrowRight size={15} /></>}
        </button>
      </form>
    </div>
  )
}