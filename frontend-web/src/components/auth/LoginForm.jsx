import React, { useState } from 'react'
import { ArrowRight, Eye, EyeOff, GraduationCap, Lock, Mail, AlertCircle,
         GraduationCap as StudentIcon, BookOpen, Building2, Shield } from 'lucide-react'

const ROLE_OPTIONS = [
  { value: 'student',  label: 'Student',  icon: StudentIcon },
  { value: 'lecturer', label: 'Lecturer', icon: BookOpen    },
  { value: 'dean',     label: 'Dean',     icon: Building2   },
  { value: 'admin',    label: 'Admin',    icon: Shield      },
]

export default function LoginForm({ onSubmit, loading = false, error = '', initialRole = 'student' }) {
  const [showPassword, setShowPassword] = useState(false)
  const [form, setForm] = useState({ email: '', password: '', role: initialRole })

  const handleSubmit = (event) => {
    event.preventDefault()
    onSubmit?.(form)
  }

  return (
    <div className="glass rounded-3xl p-8 shadow-2xl">
      <div className="flex flex-col items-center mb-8">
        <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-blue-500 to-violet-600 flex items-center justify-center mb-4 shadow-lg shadow-blue-500/30">
          <GraduationCap size={26} className="text-white" />
        </div>
        <h1 className="text-2xl font-bold text-white tracking-tight">Welcome back</h1>
        <p className="text-sm text-slate-400 mt-1">Sign in to your EduVision account</p>
      </div>

      {error ? (
        <div className="flex items-center gap-2.5 px-4 py-3 mb-5 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
          <AlertCircle size={15} className="shrink-0" />
          {error}
        </div>
      ) : null}

      <div className="grid grid-cols-4 gap-1.5 mb-6">
        {ROLE_OPTIONS.map(({ value, label, icon: Icon }) => (
          <button
            key={value}
            type="button"
            onClick={() => setForm((current) => ({ ...current, role: value }))}
            className={`flex flex-col items-center gap-1 py-2.5 rounded-xl text-xs font-medium transition-all duration-150 cursor-pointer ${
              form.role === value
                ? 'bg-gradient-to-br from-blue-500 to-violet-600 text-white force-white shadow-md shadow-blue-500/20 border border-blue-400/20'
                : 'text-slate-600 dark:text-slate-500 hover:text-slate-800 dark:hover:text-slate-300 bg-slate-100 dark:bg-slate-900/50 border border-slate-300 dark:border-slate-800/60 hover:border-slate-400 dark:hover:border-slate-700'
            }`}
          >
            <Icon size={14} />
            {label}
          </button>
        ))}
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
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

        <div className="relative">
          <Lock size={15} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none" />
          <input
            type={showPassword ? 'text' : 'password'}
            placeholder="Password"
            value={form.password}
            onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))}
            className="input-field pl-10 pr-10"
            autoComplete="current-password"
          />
          <button
            type="button"
            onClick={() => setShowPassword((current) => !current)}
            className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300 transition-colors"
          >
            {showPassword ? <EyeOff size={15} /> : <Eye size={15} />}
          </button>
        </div>

        <div className="flex justify-end">
          <span className="text-xs text-slate-600">Forgot password? Contact your administrator.</span>
        </div>

        <button
          type="submit"
          disabled={loading}
          className="btn-primary w-full flex items-center justify-center gap-2 text-sm mt-2 disabled:opacity-60 disabled:cursor-not-allowed"
        >
          {loading ? <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" /> : <>Sign In <ArrowRight size={15} /></>}
        </button>
      </form>

    </div>
  )
}