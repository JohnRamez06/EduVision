import React, { useEffect, useState } from 'react'
import {
  BarChart, Bar, LineChart, Line, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend,
} from 'recharts'
import {
  ClipboardList, Brain, TrendingUp, Activity,
  AlertTriangle, Users, BookOpen, Clock, ChevronDown, ChevronUp,
  RefreshCw, Printer,
} from 'lucide-react'
import {
  getAttendanceAnalytics, getStudentFocusReport,
  getPeakActivityReport, getLecturerPerformance,
} from '../../services/deanService'

// ── Layout wrapper ────────────────────────────────────────────────────────────
function DeanLayout({ children }) {
  return (
    <div className="min-h-screen p-6" style={{ background: 'var(--bg-app, #0f172a)' }}>
      <div className="max-w-7xl mx-auto">{children}</div>
    </div>
  )
}

const Skeleton = ({ className = '' }) => (
  <div className={`animate-pulse rounded-xl bg-slate-800/60 ${className}`} />
)

const TOOLTIP = {
  contentStyle: { background: '#1E293B', border: '1px solid #334155', borderRadius: 10, fontSize: 12 },
  labelStyle: { color: '#94A3B8' },
}

const RISK_STYLE = {
  HIGH:   'bg-rose-500/15 text-rose-300 border-rose-500/25',
  MEDIUM: 'bg-amber-500/15 text-amber-300 border-amber-500/25',
  LOW:    'bg-emerald-500/15 text-emerald-300 border-emerald-500/25',
}

const DAYS = ['', 'Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']

const TABS = [
  { id: 'attendance',  label: 'Attendance Analytics', icon: ClipboardList },
  { id: 'focus',       label: 'Student Focus',        icon: Brain         },
  { id: 'lecturer',   label: 'Lecturer Performance',  icon: Users         },
  { id: 'peak',       label: 'Peak Activity',         icon: Activity      },
]

// ── Attendance tab ────────────────────────────────────────────────────────────
function AttendanceTab() {
  const [data, setData]   = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getAttendanceAnalytics()
      .then(setData)
      .catch(e => setError(e.response?.data?.message ?? 'Failed to load attendance analytics.'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">{[...Array(4)].map((_, i) => <Skeleton key={i} className="h-64" />)}</div>
  if (error)   return <ErrorBanner msg={error} />

  const weekly = (data?.weeklyAttendance ?? []).map(r => ({ ...r, avgAttendance: Number(r.avgAttendance) }))
  const courses = (data?.avgAttendancePerCourse ?? []).map(r => ({ ...r, avgAttendance: Number(r.avgAttendance) }))
  const absent  = data?.topAbsentStudents ?? []
  const heatmap = data?.attendanceHeatmap ?? []

  return (
    <div className="space-y-5">
      {/* Weekly trend */}
      <div className="glass rounded-2xl p-5">
        <h3 className="font-semibold text-white mb-4 flex items-center gap-2"><TrendingUp size={15} className="text-[#667D9D]" /> Weekly Attendance Trend</h3>
        {weekly.length === 0 ? <Empty /> : (
          <ResponsiveContainer width="100%" height={220}>
            <LineChart data={weekly}>
              <CartesianGrid strokeDasharray="3 3" stroke="#1E293B" />
              <XAxis dataKey="label" tick={{ fontSize: 11, fill: '#64748B' }} />
              <YAxis domain={[0, 100]} tick={{ fontSize: 11, fill: '#64748B' }} unit="%" />
              <Tooltip {...TOOLTIP} formatter={v => [`${v}%`]} />
              <Line type="monotone" dataKey="avgAttendance" name="Avg Attendance" stroke="#3B82F6" strokeWidth={2} dot={{ r: 3 }} connectNulls />
            </LineChart>
          </ResponsiveContainer>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
        {/* Attendance per course */}
        <div className="glass rounded-2xl p-5">
          <h3 className="font-semibold text-white mb-4 flex items-center gap-2"><BookOpen size={15} className="text-[#667D9D]" /> Average Attendance per Course</h3>
          {courses.length === 0 ? <Empty /> : (
            <div className="space-y-3">
              {courses.map(c => {
                const pct = Number(c.avgAttendance)
                const color = pct >= 80 ? 'from-emerald-500 to-teal-500' : pct >= 60 ? 'from-amber-500 to-orange-400' : 'from-rose-500 to-pink-500'
                return (
                  <div key={c.code}>
                    <div className="flex justify-between text-xs mb-1">
                      <span className="text-slate-300 truncate max-w-[65%]">{c.code} – {c.title}</span>
                      <span className={pct >= 80 ? 'text-emerald-400' : pct >= 60 ? 'text-amber-400' : 'text-rose-400'} style={{fontWeight:600}}>{pct}%</span>
                    </div>
                    <div className="h-2 bg-slate-800 rounded-full overflow-hidden">
                      <div className={`h-full rounded-full bg-gradient-to-r ${color}`} style={{ width: `${pct}%` }} />
                    </div>
                  </div>
                )
              })}
            </div>
          )}
        </div>

        {/* Top absent students */}
        <div className="glass rounded-2xl p-5">
          <h3 className="font-semibold text-white mb-4 flex items-center gap-2"><AlertTriangle size={15} className="text-rose-400" /> Top Absent Students</h3>
          {absent.length === 0 ? <Empty msg="No absences recorded" /> : (
            <div className="overflow-x-auto">
              <table className="w-full text-xs">
                <thead><tr className="text-slate-500 uppercase tracking-wide">
                  <th className="text-left pb-2">Student</th>
                  <th className="text-center pb-2">Absences</th>
                  <th className="text-center pb-2">Rate</th>
                </tr></thead>
                <tbody>
                  {absent.map((s, i) => (
                    <tr key={i} className="border-t border-slate-800/40">
                      <td className="py-2 text-slate-300">{s.studentName}</td>
                      <td className="py-2 text-center text-rose-400 font-semibold">{s.absences}</td>
                      <td className="py-2 text-center">
                        <span className="px-2 py-0.5 rounded-full bg-rose-500/10 text-rose-300 border border-rose-500/20">{s.absenceRate}%</span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* Heatmap */}
      {heatmap.length > 0 && (
        <div className="glass rounded-2xl p-5">
          <h3 className="font-semibold text-white mb-4 flex items-center gap-2"><Clock size={15} className="text-[#667D9D]" /> Attendance Heatmap (Day × Hour)</h3>
          <HeatmapGrid data={heatmap} valueKey="attendanceRate" />
        </div>
      )}
    </div>
  )
}

// ── Focus tab ─────────────────────────────────────────────────────────────────
function FocusTab() {
  const [data, setData]     = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError]   = useState('')

  useEffect(() => {
    getStudentFocusReport()
      .then(setData)
      .catch(e => setError(e.response?.data?.message ?? 'Failed to load focus report.'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">{[...Array(4)].map((_, i) => <Skeleton key={i} className="h-64" />)}</div>
  if (error)   return <ErrorBanner msg={error} />

  const trend       = (data?.focusTrend ?? []).map(r => ({ ...r, avgFocus: Number(r.avgFocus) }))
  const courses     = (data?.focusByCourse ?? []).map(r => ({ ...r, avgFocus: Number(r.avgFocus) }))
  const distracted  = data?.mostDistractedStudents ?? []
  const allStudents = data?.focusPerStudent ?? []

  return (
    <div className="space-y-5">
      {/* Focus trend */}
      <div className="glass rounded-2xl p-5">
        <h3 className="font-semibold text-white mb-4 flex items-center gap-2"><TrendingUp size={15} className="text-[#667D9D]" /> Focus Trend Over Semester</h3>
        {trend.length === 0 ? <Empty /> : (
          <ResponsiveContainer width="100%" height={220}>
            <LineChart data={trend}>
              <CartesianGrid strokeDasharray="3 3" stroke="#1E293B" />
              <XAxis dataKey="label" tick={{ fontSize: 11, fill: '#64748B' }} />
              <YAxis domain={[0, 100]} tick={{ fontSize: 11, fill: '#64748B' }} unit="%" />
              <Tooltip {...TOOLTIP} formatter={v => [`${v}%`]} />
              <Line type="monotone" dataKey="avgFocus" name="Avg Focus" stroke="#8B5CF6" strokeWidth={2} dot={{ r: 3 }} connectNulls />
            </LineChart>
          </ResponsiveContainer>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
        {/* Focus by course */}
        <div className="glass rounded-2xl p-5">
          <h3 className="font-semibold text-white mb-4 flex items-center gap-2"><BookOpen size={15} className="text-[#667D9D]" /> Avg Focus by Course</h3>
          {courses.length === 0 ? <Empty /> : (
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={courses} margin={{ left: -20, bottom: 30 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1E293B" />
                <XAxis dataKey="code" tick={{ fontSize: 10, fill: '#64748B' }} angle={-30} textAnchor="end" interval={0} />
                <YAxis domain={[0, 100]} tick={{ fontSize: 11, fill: '#64748B' }} unit="%" />
                <Tooltip {...TOOLTIP} formatter={v => [`${v}%`, 'Avg Focus']} />
                <Bar dataKey="avgFocus" radius={[4, 4, 0, 0]}>
                  {courses.map((c, i) => (
                    <Cell key={i} fill={c.avgFocus >= 70 ? '#10B981' : c.avgFocus >= 45 ? '#F59E0B' : '#F43F5E'} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* Most distracted */}
        <div className="glass rounded-2xl p-5">
          <h3 className="font-semibold text-white mb-4 flex items-center gap-2"><AlertTriangle size={15} className="text-amber-400" /> Most Distracted Students</h3>
          {distracted.length === 0 ? <Empty msg="No data yet" /> : (
            <div className="space-y-2">
              {distracted.map((s, i) => (
                <div key={i} className="flex items-center justify-between py-2 border-b border-slate-800/40 last:border-0">
                  <span className="text-sm text-slate-300">{s.studentName}</span>
                  <div className="flex items-center gap-2">
                    <span className="text-xs text-slate-500">Focus</span>
                    <span className={`text-xs font-semibold ${Number(s.avgFocus) < 40 ? 'text-rose-400' : 'text-amber-400'}`}>{s.avgFocus}%</span>
                    <span className="text-xs text-slate-600">|</span>
                    <span className="text-xs text-slate-500">Distraction</span>
                    <span className="text-xs font-semibold text-rose-400">{s.distractionRate}%</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* All students table */}
      {allStudents.length > 0 && (
        <div className="glass rounded-2xl p-5">
          <h3 className="font-semibold text-white mb-4 flex items-center gap-2"><Users size={15} className="text-[#667D9D]" /> All Students – Focus Overview</h3>
          <div className="overflow-x-auto">
            <table className="w-full text-xs">
              <thead><tr className="text-slate-500 uppercase tracking-wide text-left">
                <th className="pb-2 pr-4">Student</th>
                <th className="pb-2 pr-4 text-center">Avg Focus</th>
                <th className="pb-2 pr-4 text-center">Attentiveness</th>
                <th className="pb-2 pr-4 text-center">Sessions</th>
                <th className="pb-2">Top Emotion</th>
              </tr></thead>
              <tbody>
                {allStudents.map((s, i) => (
                  <tr key={i} className="border-t border-slate-800/40 hover:bg-slate-800/30">
                    <td className="py-2 pr-4 text-slate-300">{s.studentName}</td>
                    <td className="py-2 pr-4 text-center">
                      <span className={`font-semibold ${Number(s.avgFocus) >= 70 ? 'text-emerald-400' : Number(s.avgFocus) >= 45 ? 'text-amber-400' : 'text-rose-400'}`}>{s.avgFocus}%</span>
                    </td>
                    <td className="py-2 pr-4 text-center text-slate-300">{s.avgAttentiveness}%</td>
                    <td className="py-2 pr-4 text-center text-slate-500">{s.sessionsAttended}</td>
                    <td className="py-2 text-slate-400 capitalize">{s.dominantEmotion ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}

// ── Lecturer Performance tab ──────────────────────────────────────────────────
function LecturerTab() {
  const [data, setData]     = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError]   = useState('')

  useEffect(() => {
    getLecturerPerformance()
      .then(setData)
      .catch(e => setError(e.response?.data?.message ?? 'Failed to load lecturer performance.'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <Skeleton className="h-80" />
  if (error)   return <ErrorBanner msg={error} />

  const RATING_STYLE = {
    'Excellent':           'bg-emerald-500/15 text-emerald-300 border-emerald-500/25',
    'Good':                'bg-[#667D9D]/15  text-[#ACBBC6]  border-[#667D9D]/25',
    'Average':             'bg-amber-500/15  text-amber-300  border-amber-500/25',
    'Needs Improvement':   'bg-rose-500/15   text-rose-300   border-rose-500/25',
    'Insufficient Data':   'bg-slate-700/40  text-slate-400  border-slate-700',
  }

  return (
    <div className="glass rounded-2xl p-5">
      <h3 className="font-semibold text-white mb-4 flex items-center gap-2"><Users size={15} className="text-[#667D9D]" /> Lecturer Performance Insights</h3>
      {data.length === 0 ? <Empty /> : (
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead><tr className="text-slate-500 text-xs uppercase tracking-wide text-left">
              <th className="pb-3 pr-4">Lecturer</th>
              <th className="pb-3 pr-4">Department</th>
              <th className="pb-3 pr-4 text-center">Courses</th>
              <th className="pb-3 pr-4 text-center">Sessions</th>
              <th className="pb-3 pr-4 text-center">Student Focus</th>
              <th className="pb-3 pr-4 text-center">Avg Attendance</th>
              <th className="pb-3">Rating</th>
            </tr></thead>
            <tbody>
              {data.map((l, i) => (
                <tr key={i} className="border-t border-slate-800/40 hover:bg-slate-800/30">
                  <td className="py-3 pr-4 font-medium text-slate-200">{l.lecturerName}</td>
                  <td className="py-3 pr-4 text-slate-400 text-xs">{l.department}</td>
                  <td className="py-3 pr-4 text-center text-slate-300">{l.courseCount}</td>
                  <td className="py-3 pr-4 text-center text-slate-300">{l.sessionCount}</td>
                  <td className="py-3 pr-4 text-center">
                    <span className={`font-semibold ${Number(l.avgConcentration) >= 70 ? 'text-emerald-400' : Number(l.avgConcentration) >= 45 ? 'text-amber-400' : 'text-rose-400'}`}>
                      {l.avgConcentration}%
                    </span>
                  </td>
                  <td className="py-3 pr-4 text-center text-slate-300">{l.avgAttendance}%</td>
                  <td className="py-3">
                    <span className={`text-xs px-2 py-0.5 rounded-full border font-medium ${RATING_STYLE[l.rating] ?? RATING_STYLE['Insufficient Data']}`}>
                      {l.rating}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

// ── Peak Activity tab ─────────────────────────────────────────────────────────
function PeakActivityTab() {
  const [data, setData]     = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError]   = useState('')

  useEffect(() => {
    getPeakActivityReport()
      .then(setData)
      .catch(e => setError(e.response?.data?.message ?? 'Failed to load peak activity.'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">{[...Array(2)].map((_, i) => <Skeleton key={i} className="h-64" />)}</div>
  if (error)   return <ErrorBanner msg={error} />

  const byHour = (data?.focusByHour ?? []).map(r => ({ ...r, hourOfDay: Number(r.hourOfDay), avgFocus: Number(r.avgFocus) }))
  const byDay  = (data?.focusByDay  ?? []).map(r => ({ ...r, dayNum: Number(r.dayNum), avgFocus: Number(r.avgFocus) }))
  const slots  = data?.lectureSlots ?? []

  return (
    <div className="space-y-5">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
        {/* Focus by hour */}
        <div className="glass rounded-2xl p-5">
          <h3 className="font-semibold text-white mb-4 flex items-center gap-2"><Clock size={15} className="text-[#667D9D]" /> Focus by Hour of Day</h3>
          {byHour.length === 0 ? <Empty /> : (
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={byHour}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1E293B" />
                <XAxis dataKey="hourOfDay" tick={{ fontSize: 10, fill: '#64748B' }} tickFormatter={h => `${h}:00`} />
                <YAxis domain={[0, 100]} tick={{ fontSize: 11, fill: '#64748B' }} unit="%" />
                <Tooltip {...TOOLTIP} labelFormatter={h => `${h}:00`} formatter={v => [`${v}%`, 'Avg Focus']} />
                <Bar dataKey="avgFocus" radius={[4, 4, 0, 0]}>
                  {byHour.map((d, i) => (
                    <Cell key={i} fill={d.avgFocus >= 70 ? '#10B981' : d.avgFocus >= 45 ? '#F59E0B' : '#F43F5E'} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* Focus by day */}
        <div className="glass rounded-2xl p-5">
          <h3 className="font-semibold text-white mb-4 flex items-center gap-2"><Activity size={15} className="text-[#667D9D]" /> Focus by Day of Week</h3>
          {byDay.length === 0 ? <Empty /> : (
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={byDay}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1E293B" />
                <XAxis dataKey="dayName" tick={{ fontSize: 10, fill: '#64748B' }} />
                <YAxis domain={[0, 100]} tick={{ fontSize: 11, fill: '#64748B' }} unit="%" />
                <Tooltip {...TOOLTIP} formatter={v => [`${v}%`, 'Avg Focus']} />
                <Bar dataKey="avgFocus" radius={[4, 4, 0, 0]}>
                  {byDay.map((d, i) => (
                    <Cell key={i} fill={d.avgFocus >= 70 ? '#10B981' : d.avgFocus >= 45 ? '#F59E0B' : '#F43F5E'} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>

      {/* Best/worst slots */}
      {slots.length > 0 && (
        <div className="glass rounded-2xl p-5">
          <h3 className="font-semibold text-white mb-4 flex items-center gap-2"><TrendingUp size={15} className="text-[#667D9D]" /> Lecture Slots – Best to Worst</h3>
          <div className="overflow-x-auto">
            <table className="w-full text-xs">
              <thead><tr className="text-slate-500 uppercase tracking-wide text-left">
                <th className="pb-2 pr-4">Course</th>
                <th className="pb-2 pr-4">Slot</th>
                <th className="pb-2 pr-4 text-center">Avg Focus</th>
                <th className="pb-2 pr-4 text-center">Attentiveness</th>
                <th className="pb-2 text-center">Sessions</th>
              </tr></thead>
              <tbody>
                {slots.map((s, i) => (
                  <tr key={i} className="border-t border-slate-800/40">
                    <td className="py-2 pr-4 text-slate-300">{s.courseCode}</td>
                    <td className="py-2 pr-4 text-slate-400">{s.slot}</td>
                    <td className="py-2 pr-4 text-center">
                      <span className={`font-semibold ${Number(s.avgFocus) >= 70 ? 'text-emerald-400' : Number(s.avgFocus) >= 45 ? 'text-amber-400' : 'text-rose-400'}`}>{s.avgFocus}%</span>
                    </td>
                    <td className="py-2 pr-4 text-center text-slate-300">{s.avgAttentiveness}%</td>
                    <td className="py-2 text-center text-slate-500">{s.sessionCount}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}

// ── Shared helpers ────────────────────────────────────────────────────────────
function Empty({ msg = 'No data yet' }) {
  return <div className="flex items-center justify-center h-32 text-slate-600 text-sm">{msg}</div>
}
function ErrorBanner({ msg }) {
  return (
    <div className="flex items-center gap-2 px-4 py-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
      <AlertTriangle size={14} className="shrink-0" /> {msg}
    </div>
  )
}

function HeatmapGrid({ data, valueKey }) {
  const hours = [...new Set(data.map(d => Number(d.hourOfDay)))].sort((a, b) => a - b)
  const days  = [...new Set(data.map(d => ({ num: Number(d.dayNum), name: d.dayName })))].sort((a, b) => a.num - b.num)

  const get = (dayNum, hour) => {
    const r = data.find(d => Number(d.dayNum) === dayNum && Number(d.hourOfDay) === hour)
    return r ? Number(r[valueKey]) : null
  }
  const color = v => {
    if (v === null) return '#1E293B'
    if (v >= 80) return '#10B981'
    if (v >= 60) return '#3B82F6'
    if (v >= 40) return '#F59E0B'
    return '#EF4444'
  }

  return (
    <div className="overflow-x-auto">
      <table className="text-xs">
        <thead>
          <tr>
            <th className="pr-3 text-slate-500 text-left pb-2">Day \ Hour</th>
            {hours.map(h => <th key={h} className="px-2 pb-2 text-slate-500 text-center min-w-[42px]">{h}:00</th>)}
          </tr>
        </thead>
        <tbody>
          {days.map(d => (
            <tr key={d.num}>
              <td className="pr-3 py-1 text-slate-400 font-medium whitespace-nowrap">{d.name}</td>
              {hours.map(h => {
                const v = get(d.num, h)
                return (
                  <td key={h} className="px-2 py-1 text-center">
                    <div
                      className="w-8 h-8 rounded-lg flex items-center justify-center text-[10px] font-semibold text-white mx-auto"
                      style={{ background: color(v) }}
                      title={v !== null ? `${v}%` : 'No data'}
                    >
                      {v !== null ? `${Math.round(v)}` : '—'}
                    </div>
                  </td>
                )
              })}
            </tr>
          ))}
        </tbody>
      </table>
      <div className="flex items-center gap-4 mt-3 text-xs text-slate-500">
        {[['#10B981','≥ 80%'], ['#3B82F6','60–79%'], ['#F59E0B','40–59%'], ['#EF4444','< 40%']].map(([c, l]) => (
          <span key={l} className="flex items-center gap-1"><span className="w-3 h-3 rounded" style={{ background: c }} />{l}</span>
        ))}
      </div>
    </div>
  )
}

// ── Main page ─────────────────────────────────────────────────────────────────
export default function DeanReports() {
  const [tab, setTab] = useState('attendance')

  return (
    <DeanLayout>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-white flex items-center gap-2">
          <ClipboardList size={22} className="text-[#667D9D]" /> Dean Analytics Reports
        </h1>
        <p className="text-sm text-slate-500 mt-1">Comprehensive analytics across attendance, focus, lecturer performance, and activity patterns</p>
      </div>

      {/* Tab bar */}
      <div className="flex items-center justify-between mb-6 flex-wrap gap-3">
        <div className="flex gap-2 flex-wrap">
          {TABS.map(t => {
            const Icon = t.icon
            return (
              <button
                key={t.id}
                onClick={() => setTab(t.id)}
                className={`flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium transition-all ${
                  tab === t.id
                    ? 'bg-[#667D9D]/20 text-[#ACBBC6] border border-[#667D9D]/30'
                    : 'text-slate-500 border border-slate-800 hover:text-slate-300 hover:border-slate-700'
                }`}
              >
                <Icon size={14} /> {t.label}
              </button>
            )
          })}
        </div>
        <button
          onClick={() => window.print()}
          className="flex items-center gap-2 px-3 py-2 rounded-xl text-xs font-medium text-slate-400 border border-slate-800 hover:text-slate-200 hover:border-slate-700 transition-all"
        >
          <Printer size={13} /> Print / Save as PDF
        </button>
      </div>

      {tab === 'attendance' && <AttendanceTab />}
      {tab === 'focus'      && <FocusTab />}
      {tab === 'lecturer'   && <LecturerTab />}
      {tab === 'peak'       && <PeakActivityTab />}
    </DeanLayout>
  )
}
