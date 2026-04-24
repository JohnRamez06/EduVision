import React, { useContext, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  BookOpen, Clock, Users, Zap, TrendingUp,
  AlertTriangle, CheckCircle, Calendar, MapPin,
  ChevronRight, Activity,
} from 'lucide-react'
import LecturerLayout from '../../layouts/LecturerLayout'
import { AuthContext } from '../../context/AuthContext'
import lecturerService from '../../services/lecturerService'

const Skeleton = ({ className = '' }) => (
  <div className={`animate-pulse rounded-xl bg-slate-800/60 ${className}`} />
)

const greet = name => {
  const h = new Date().getHours()
  const prefix = h < 12 ? 'Good morning' : h < 17 ? 'Good afternoon' : 'Good evening'
  return `${prefix}, ${name?.split(' ')[0] ?? 'Lecturer'}`
}

const StatCard = ({ icon: Icon, label, value, sub, color = 'emerald' }) => {
  const colors = {
    emerald: 'bg-emerald-500/15 text-emerald-400',
    teal:    'bg-teal-500/15 text-teal-400',
    blue:    'bg-blue-500/15 text-blue-400',
    amber:   'bg-amber-500/15 text-amber-400',
  }
  return (
    <div className="glass rounded-2xl p-5 flex items-center gap-4">
      <div className={`w-11 h-11 rounded-xl flex items-center justify-center shrink-0 ${colors[color]}`}>
        <Icon size={20} />
      </div>
      <div className="min-w-0">
        <p className="text-xs text-slate-500 font-medium mb-0.5">{label}</p>
        <p className="text-xl font-bold text-white leading-none">{value ?? '—'}</p>
        {sub && <p className="text-xs text-slate-500 mt-0.5">{sub}</p>}
      </div>
    </div>
  )
}

function EngagementBar({ value }) {
  const pct = Math.min(100, Math.max(0, Math.round(value)))
  const color = pct >= 70 ? 'bg-emerald-500' : pct >= 45 ? 'bg-amber-500' : 'bg-rose-500'
  return (
    <div className="flex items-center gap-2">
      <div className="flex-1 h-1.5 bg-slate-800 rounded-full overflow-hidden">
        <div className={`h-full rounded-full ${color}`} style={{ width: `${pct}%` }} />
      </div>
      <span className="text-xs text-slate-400 w-8 text-right">{pct}%</span>
    </div>
  )
}

export default function LecturerDashboard() {
  const { user } = useContext(AuthContext)
  const navigate = useNavigate()
  const [data, setData]       = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState('')

  useEffect(() => {
    lecturerService.getDashboard()
      .then(setData)
      .catch(e => setError(e.response?.data?.message ?? 'Failed to load dashboard.'))
      .finally(() => setLoading(false))
  }, [])

  const profile  = data?.profile
  const courses  = data?.courses ?? []
  const sessions = data?.recentSessions ?? []

  return (
    <LecturerLayout>
      {/* Greeting */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-white">
          {loading ? 'Loading…' : greet(profile?.fullName ?? user?.fullName)}
        </h1>
        <p className="text-sm text-slate-500 mt-1">
          {profile
            ? [profile.department, profile.specialization].filter(Boolean).join(' · ') || 'Lecturer dashboard'
            : 'Your teaching overview'}
        </p>
      </div>

      {error && (
        <div className="flex items-center gap-2 px-4 py-3 mb-6 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
          <AlertTriangle size={15} className="shrink-0" /> {error}
        </div>
      )}

      {/* KPI row */}
      {loading ? (
        <div className="grid grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
          {[...Array(3)].map((_, i) => <Skeleton key={i} className="h-24" />)}
        </div>
      ) : (
        <div className="grid grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
          <StatCard icon={BookOpen}   color="emerald" label="Courses Taught"  value={data?.totalCourses}   sub="this semester" />
          <StatCard icon={Clock}      color="teal"    label="Total Sessions"  value={data?.totalSessions}  sub="across all courses" />
          <StatCard icon={Activity}   color="amber"   label="Active Sessions" value={data?.activeSessions} sub="currently live" />
        </div>
      )}

      {/* Profile info strip */}
      {!loading && profile && (
        <div className="glass rounded-2xl p-4 mb-5 flex flex-wrap gap-4">
          {[
            { icon: Users,    text: profile.employeeId,    label: 'Employee ID' },
            { icon: BookOpen, text: profile.department,    label: 'Department' },
            { icon: Zap,      text: profile.specialization,label: 'Specialization' },
            { icon: MapPin,   text: profile.officeLocation, label: 'Office' },
          ].filter(i => i.text).map(({ icon: Icon, text, label }) => (
            <div key={label} className="flex items-center gap-2 text-sm">
              <Icon size={13} className="text-slate-500 shrink-0" />
              <span className="text-slate-500 text-xs">{label}:</span>
              <span className="text-slate-300 text-xs font-medium">{text}</span>
            </div>
          ))}
        </div>
      )}

      {/* Courses + Recent Sessions */}
      <div className="grid grid-cols-1 lg:grid-cols-5 gap-5">

        {/* Courses grid */}
        <div className="lg:col-span-2 glass rounded-2xl p-5">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold text-white flex items-center gap-2">
              <BookOpen size={16} className="text-emerald-400" /> My Courses
            </h2>
            <span className="text-xs text-slate-500">{courses.length} courses</span>
          </div>

          {loading ? (
            <div className="space-y-3">{[...Array(3)].map((_, i) => <Skeleton key={i} className="h-20" />)}</div>
          ) : courses.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-32 text-slate-600 text-sm">
              <BookOpen size={28} className="mb-2 opacity-30" />
              No courses assigned yet
            </div>
          ) : (
            <div className="space-y-3">
              {courses.map(c => (
                <div key={c.courseId} onClick={() => navigate('/lecturer/courses')} className="flex items-start gap-3 p-3.5 rounded-xl bg-navy-900/50 hover:bg-navy-900/80 transition-colors group cursor-pointer">
                  <div className="w-9 h-9 rounded-lg bg-emerald-500/10 flex items-center justify-center shrink-0 mt-0.5">
                    <BookOpen size={15} className="text-emerald-400" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-xs font-mono text-emerald-400 mb-0.5">{c.code}</p>
                    <p className="text-sm font-medium text-slate-200 truncate">{c.title}</p>
                    <div className="flex items-center gap-3 mt-1">
                      <span className="text-xs text-slate-500">{c.totalSessions} sessions</span>
                      {c.activeSessions > 0 && (
                        <span className="flex items-center gap-1 text-xs text-emerald-400 font-medium">
                          <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
                          Live
                        </span>
                      )}
                    </div>
                  </div>
                  <ChevronRight size={14} className="text-slate-600 group-hover:text-emerald-400 shrink-0 transition-colors mt-1" />
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Recent sessions table */}
        <div className="lg:col-span-3 glass rounded-2xl p-5">
          <h2 className="font-semibold text-white flex items-center gap-2 mb-4">
            <Clock size={16} className="text-teal-400" /> Recent Sessions
          </h2>

          {loading ? (
            <div className="space-y-3">{[...Array(4)].map((_, i) => <Skeleton key={i} className="h-12" />)}</div>
          ) : sessions.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-40 text-slate-600 text-sm">
              <Clock size={28} className="mb-2 opacity-30" />
              No completed sessions yet
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-xs text-slate-500 border-b border-slate-800">
                    <th className="text-left pb-2.5 font-medium">Course</th>
                    <th className="text-left pb-2.5 font-medium">Date</th>
                    <th className="text-center pb-2.5 font-medium">Students</th>
                    <th className="text-center pb-2.5 font-medium">Alerts</th>
                    <th className="text-left pb-2.5 font-medium w-28">Engagement</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60">
                  {sessions.map(s => (
                    <tr key={s.sessionId} className="hover:bg-slate-800/20 transition-colors group cursor-pointer">
                      <td className="py-3 pr-3 font-medium text-slate-200 truncate max-w-[110px]">{s.courseName ?? '—'}</td>
                      <td className="py-3 pr-3 text-slate-500 whitespace-nowrap text-xs">
                        {s.date ? new Date(s.date).toLocaleDateString('en-GB', { day: 'numeric', month: 'short' }) : '—'}
                      </td>
                      <td className="py-3 pr-3 text-center">
                        <span className="flex items-center justify-center gap-1 text-slate-300">
                          <Users size={12} className="text-slate-500" />
                          {s.studentCount}
                        </span>
                      </td>
                      <td className="py-3 pr-3 text-center">
                        {s.alertCount > 0 ? (
                          <span className="text-xs font-semibold text-amber-400 bg-amber-500/10 px-2 py-0.5 rounded-full">
                            {s.alertCount}
                          </span>
                        ) : (
                          <CheckCircle size={14} className="text-emerald-500 mx-auto" />
                        )}
                      </td>
                      <td className="py-3">
                        <EngagementBar value={s.avgEngagement} />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </LecturerLayout>
  )
}
