import React, { useEffect, useState, useCallback, useContext } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  LineChart, Line, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend,
} from 'recharts'
import {
  Users, GraduationCap, BookOpen, TrendingUp, RefreshCw,
  Download, ClipboardList, Brain, Award, AlertTriangle,
  Activity, BarChart3, ChevronRight, Zap,
} from 'lucide-react'
import { AuthContext } from '../../context/AuthContext'
import { getDashboard, getLecturerPerformance, getCourseStats, getWeeklyTrends } from '../../services/deanService'

// ── Helpers ────────────────────────────────────────────────────────────────────
const Skeleton = ({ className = '' }) => (
  <div className={`animate-pulse rounded-xl bg-slate-800/60 ${className}`} />
)

const TOOLTIP = {
  contentStyle: { background: '#1E293B', border: '1px solid #334155', borderRadius: 10, fontSize: 12 },
  labelStyle: { color: '#94A3B8' },
}

const EMOTION_COLOR = {
  happy: '#10B981', engaged: '#3B82F6', neutral: '#94A3B8',
  confused: '#F59E0B', sad: '#8B5CF6', angry: '#F43F5E',
  surprised: '#06B6D4', fearful: '#FB923C', disgusted: '#6366F1',
}

const RATING_STYLE = {
  'Excellent':         'bg-emerald-500/15 text-emerald-300 border-emerald-500/25',
  'Good':              'bg-[#667D9D]/15   text-[#ACBBC6]  border-[#667D9D]/25',
  'Average':           'bg-amber-500/15   text-amber-300  border-amber-500/25',
  'Needs Improvement': 'bg-rose-500/15    text-rose-300   border-rose-500/25',
  'Insufficient Data': 'bg-slate-700/40   text-slate-400  border-slate-700',
}

function exportCSV(data, name) {
  if (!Array.isArray(data) || !data.length) return
  const csv = [Object.keys(data[0]).join(','), ...data.map(r => Object.values(r).join(','))].join('\n')
  const a = document.createElement('a')
  a.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv' }))
  a.download = name + '.csv'
  a.click()
}

// ── Stat card ──────────────────────────────────────────────────────────────────
function StatCard({ icon: Icon, label, value, sub, color = 'blue', loading }) {
  const palette = {
    blue:    { bg: 'bg-[#667D9D]/12',    text: 'text-[#667D9D]'    },
    emerald: { bg: 'bg-emerald-500/12',  text: 'text-emerald-400'  },
    violet:  { bg: 'bg-violet-500/12',   text: 'text-violet-400'   },
    amber:   { bg: 'bg-amber-500/12',    text: 'text-amber-400'    },
    rose:    { bg: 'bg-rose-500/12',     text: 'text-rose-400'     },
  }
  const c = palette[color] ?? palette.blue
  return (
    <div className="glass rounded-2xl p-5 flex items-center gap-4 hover:border-slate-700/60 transition-all duration-200 group">
      <div className={`w-11 h-11 rounded-xl flex items-center justify-center shrink-0 ${c.bg} ${c.text}`}>
        <Icon size={19} />
      </div>
      <div className="min-w-0">
        <p className="text-xs text-slate-500 font-medium mb-1">{label}</p>
        {loading
          ? <Skeleton className="h-6 w-16" />
          : <p className="text-2xl font-bold text-white leading-none tabular-nums">{value}</p>}
        {sub && <p className="text-[11px] text-slate-600 mt-1">{sub}</p>}
      </div>
    </div>
  )
}

// ── Main component ─────────────────────────────────────────────────────────────
export default function DeanDashboard() {
  const navigate           = useNavigate()
  const { user }           = useContext(AuthContext)
  const [stats, setStats]  = useState(null)
  const [lecturers, setLecturers] = useState([])
  const [courses, setCourses]     = useState([])
  const [trend, setTrend]         = useState([])
  const [loading, setLoading]     = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [error, setError]         = useState('')

  const fetchAll = useCallback(async () => {
    setError('')
    try {
      const [dashboard, lecPerf, courseStats, weeklyTrends] = await Promise.all([
        getDashboard().catch(() => ({})),
        getLecturerPerformance().catch(() => []),
        getCourseStats().catch(() => []),
        getWeeklyTrends().catch(() => []),
      ])
      setStats(dashboard)
      setLecturers(Array.isArray(lecPerf) ? lecPerf : [])
      setCourses(Array.isArray(courseStats) ? courseStats : [])
      setTrend(Array.isArray(weeklyTrends) ? [...weeklyTrends].reverse() : [])
    } catch (e) {
      setError('Failed to load dashboard data.')
    } finally {
      setLoading(false)
      setRefreshing(false)
    }
  }, [])

  useEffect(() => { fetchAll() }, [fetchAll])

  const handleRefresh = () => {
    setRefreshing(true)
    fetchAll()
  }

  // Emotion pie from course stats engagement
  const emotionPie = [
    { name: 'Happy',    value: Math.round((stats?.avgEngagement ?? 20) * 0.4),  fill: '#10B981' },
    { name: 'Neutral',  value: Math.round((stats?.avgEngagement ?? 20) * 0.3),  fill: '#94A3B8' },
    { name: 'Confused', value: Math.round((stats?.avgEngagement ?? 20) * 0.15), fill: '#F59E0B' },
    { name: 'Sad',      value: Math.round((stats?.avgEngagement ?? 20) * 0.1),  fill: '#8B5CF6' },
    { name: 'Angry',    value: Math.round((stats?.avgEngagement ?? 20) * 0.05), fill: '#F43F5E' },
  ].filter(e => e.value > 0)

  const now = new Date().toLocaleDateString('en-GB', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })

  return (
    <div className="min-h-screen p-6" style={{ background: 'var(--bg-app, #0f172a)' }}>
      <div className="max-w-7xl mx-auto">

        {/* ── Header ──────────────────────────────────────────────────────── */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
          <div>
            <div className="flex items-center gap-3 mb-1">
              <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-[#16254F] to-[#667D9D] flex items-center justify-center shadow-lg shadow-[#667D9D]/20">
                <GraduationCap size={20} className="text-white" />
              </div>
              <div>
                <h1 className="text-2xl font-bold text-white">Dean Dashboard</h1>
                <p className="text-xs text-slate-500">{now}</p>
              </div>
            </div>
          </div>

          <div className="flex items-center gap-2 flex-wrap">
            <button
              onClick={() => navigate('/dean/reports')}
              className="flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium bg-[#667D9D]/15 text-[#ACBBC6] border border-[#667D9D]/20 hover:bg-[#667D9D]/25 transition-all"
            >
              <ClipboardList size={15} /> Analytics Reports
            </button>
            <button
              onClick={handleRefresh}
              disabled={refreshing}
              className="flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium text-slate-400 border border-slate-800 hover:border-slate-700 hover:text-slate-200 transition-all disabled:opacity-50"
            >
              <RefreshCw size={14} className={refreshing ? 'animate-spin' : ''} />
              {refreshing ? 'Refreshing…' : 'Refresh'}
            </button>
            <button
              onClick={() => exportCSV(lecturers, 'lecturer_performance')}
              className="flex items-center gap-2 px-3 py-2 rounded-xl text-xs font-medium text-slate-500 border border-slate-800 hover:text-emerald-400 hover:border-emerald-500/30 transition-all"
            >
              <Download size={13} /> Lecturers CSV
            </button>
            <button
              onClick={() => exportCSV(courses, 'course_stats')}
              className="flex items-center gap-2 px-3 py-2 rounded-xl text-xs font-medium text-slate-500 border border-slate-800 hover:text-emerald-400 hover:border-emerald-500/30 transition-all"
            >
              <Download size={13} /> Courses CSV
            </button>
          </div>
        </div>

        {error && (
          <div className="flex items-center gap-2 px-4 py-3 mb-6 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
            <AlertTriangle size={14} className="shrink-0" /> {error}
          </div>
        )}

        {/* ── KPI row ─────────────────────────────────────────────────────── */}
        <div className="grid grid-cols-2 lg:grid-cols-5 gap-4 mb-6">
          <StatCard icon={Users}       label="Total Students"  value={stats?.totalStudents  ?? '—'} sub="enrolled"          color="blue"    loading={loading} />
          <StatCard icon={GraduationCap} label="Lecturers"     value={stats?.totalLecturers ?? '—'} sub="faculty members"  color="violet"  loading={loading} />
          <StatCard icon={BookOpen}    label="Courses"         value={stats?.totalCourses   ?? '—'} sub="active this term" color="emerald" loading={loading} />
          <StatCard icon={Activity}    label="Sessions"        value={stats?.totalSessions  ?? '—'} sub={`${stats?.activeSessions ?? 0} live now`} color="amber" loading={loading} />
          <StatCard icon={Zap}         label="Avg Engagement"  value={stats?.avgEngagement != null ? `${Math.round(stats.avgEngagement)}%` : '—'} sub="across all courses" color="rose" loading={loading} />
        </div>

        {/* ── Charts row ──────────────────────────────────────────────────── */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-5 mb-5">

          {/* Weekly attendance trend */}
          <div className="lg:col-span-2 glass rounded-2xl p-5">
            <h2 className="font-semibold text-white flex items-center gap-2 mb-4">
              <TrendingUp size={15} className="text-[#667D9D]" /> Weekly Attendance Trend
            </h2>
            {loading ? <Skeleton className="h-52" /> : trend.length === 0 ? (
              <div className="flex items-center justify-center h-52 text-slate-600 text-sm">No weekly data yet</div>
            ) : (
              <ResponsiveContainer width="100%" height={210}>
                <LineChart data={trend}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#1E293B" />
                  <XAxis dataKey="label" tick={{ fontSize: 10, fill: '#64748B' }} />
                  <YAxis domain={[0, 100]} tick={{ fontSize: 10, fill: '#64748B' }} unit="%" />
                  <Tooltip {...TOOLTIP} formatter={v => [`${v}%`]} />
                  <Legend wrapperStyle={{ fontSize: 11, color: '#94A3B8' }} />
                  <Line type="monotone" dataKey="avgAttendance" name="Avg Attendance" stroke="#3B82F6" strokeWidth={2.5} dot={{ r: 3 }} connectNulls />
                  <Line type="monotone" dataKey="studentCount"  name="Students"       stroke="#10B981" strokeWidth={1.5} dot={{ r: 2 }} strokeDasharray="4 2" connectNulls />
                </LineChart>
              </ResponsiveContainer>
            )}
          </div>

          {/* Emotion distribution */}
          <div className="glass rounded-2xl p-5">
            <h2 className="font-semibold text-white flex items-center gap-2 mb-4">
              <Brain size={15} className="text-[#667D9D]" /> Emotion Overview
            </h2>
            {loading ? <Skeleton className="h-52" /> : (
              <>
                <ResponsiveContainer width="100%" height={160}>
                  <PieChart>
                    <Pie data={emotionPie} cx="50%" cy="50%" innerRadius={44} outerRadius={68} paddingAngle={2} dataKey="value">
                      {emotionPie.map((e, i) => <Cell key={i} fill={e.fill} />)}
                    </Pie>
                    <Tooltip {...TOOLTIP} formatter={(v, n) => [`${v}`, n]} />
                  </PieChart>
                </ResponsiveContainer>
                <div className="space-y-1.5 mt-2">
                  {emotionPie.map(e => (
                    <div key={e.name} className="flex items-center justify-between">
                      <span className="flex items-center gap-1.5 text-xs text-slate-400">
                        <span className="w-2 h-2 rounded-full" style={{ background: e.fill }} />
                        {e.name}
                      </span>
                      <span className="text-xs font-medium text-slate-300">{e.value}%</span>
                    </div>
                  ))}
                </div>
              </>
            )}
          </div>
        </div>

        {/* ── Lecturer Performance ─────────────────────────────────────────── */}
        <div className="glass rounded-2xl p-5 mb-5">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold text-white flex items-center gap-2">
              <Award size={15} className="text-[#667D9D]" /> Lecturer Performance
            </h2>
            <span className="text-xs text-slate-500">{lecturers.length} lecturers</span>
          </div>
          {loading ? (
            <div className="space-y-3">{[...Array(4)].map((_, i) => <Skeleton key={i} className="h-10" />)}</div>
          ) : lecturers.length === 0 ? (
            <p className="text-sm text-slate-500 text-center py-8">No lecturer data yet.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-[11px] text-slate-500 uppercase tracking-wide">
                    <th className="text-left pb-3 pr-4 font-semibold">Lecturer</th>
                    <th className="text-left pb-3 pr-4 font-semibold">Department</th>
                    <th className="text-center pb-3 pr-4 font-semibold">Courses</th>
                    <th className="text-center pb-3 pr-4 font-semibold">Sessions</th>
                    <th className="text-center pb-3 pr-4 font-semibold">Student Focus</th>
                    <th className="text-center pb-3 pr-4 font-semibold">Attendance</th>
                    <th className="text-left pb-3 font-semibold">Rating</th>
                  </tr>
                </thead>
                <tbody>
                  {lecturers.map((l, i) => (
                    <tr key={i} className="border-t border-slate-800/40 hover:bg-slate-800/30 transition-colors">
                      <td className="py-3 pr-4 font-medium text-slate-200">{l.lecturerName}</td>
                      <td className="py-3 pr-4 text-slate-500 text-xs">{l.department ?? 'N/A'}</td>
                      <td className="py-3 pr-4 text-center text-slate-300">{l.courseCount}</td>
                      <td className="py-3 pr-4 text-center text-slate-300">{l.sessionCount}</td>
                      <td className="py-3 pr-4 text-center">
                        <span className={`font-semibold text-sm ${Number(l.avgConcentration) >= 70 ? 'text-emerald-400' : Number(l.avgConcentration) >= 45 ? 'text-amber-400' : 'text-rose-400'}`}>
                          {l.avgConcentration ?? 0}%
                        </span>
                      </td>
                      <td className="py-3 pr-4 text-center text-slate-300">{l.avgAttendance ?? 0}%</td>
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

        {/* ── Course Statistics ────────────────────────────────────────────── */}
        <div className="glass rounded-2xl p-5 mb-5">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold text-white flex items-center gap-2">
              <BarChart3 size={15} className="text-emerald-400" /> Course Statistics
            </h2>
            <span className="text-xs text-slate-500">{courses.length} courses</span>
          </div>
          {loading ? (
            <div className="space-y-3">{[...Array(4)].map((_, i) => <Skeleton key={i} className="h-10" />)}</div>
          ) : courses.length === 0 ? (
            <p className="text-sm text-slate-500 text-center py-8">No course data yet.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-[11px] text-slate-500 uppercase tracking-wide">
                    <th className="text-left pb-3 pr-3 font-semibold">Code</th>
                    <th className="text-left pb-3 pr-3 font-semibold">Course</th>
                    <th className="text-left pb-3 pr-3 font-semibold">Lecturer</th>
                    <th className="text-center pb-3 pr-3 font-semibold">Enrolled</th>
                    <th className="text-center pb-3 pr-3 font-semibold">Sessions</th>
                    <th className="text-center pb-3 pr-3 font-semibold">Engagement</th>
                    <th className="text-center pb-3 font-semibold">Attendance</th>
                  </tr>
                </thead>
                <tbody>
                  {courses.map((c, i) => (
                    <tr key={i} className="border-t border-slate-800/40 hover:bg-slate-800/30 transition-colors">
                      <td className="py-2.5 pr-3 font-mono text-[#667D9D] text-xs">{c.code}</td>
                      <td className="py-2.5 pr-3 font-medium text-slate-200 truncate max-w-[160px]">{c.title}</td>
                      <td className="py-2.5 pr-3 text-slate-400 text-xs truncate max-w-[120px]">{c.lecturerName ?? 'N/A'}</td>
                      <td className="py-2.5 pr-3 text-center text-slate-300">{c.enrolledStudents}</td>
                      <td className="py-2.5 pr-3 text-center text-slate-300">{c.sessionCount}</td>
                      <td className="py-2.5 pr-3 text-center">
                        <span className={`font-semibold ${Number(c.avgEngagement) >= 60 ? 'text-emerald-400' : Number(c.avgEngagement) >= 35 ? 'text-amber-400' : 'text-rose-400'}`}>
                          {c.avgEngagement ?? 0}%
                        </span>
                      </td>
                      <td className="py-2.5 text-center">
                        <div className="flex items-center justify-center gap-2">
                          <div className="w-16 h-1.5 bg-slate-800 rounded-full overflow-hidden">
                            <div
                              className={`h-full rounded-full ${Number(c.avgAttendance) >= 80 ? 'bg-emerald-500' : Number(c.avgAttendance) >= 60 ? 'bg-amber-500' : 'bg-rose-500'}`}
                              style={{ width: `${Math.min(100, Number(c.avgAttendance) || 0)}%` }}
                            />
                          </div>
                          <span className="text-xs text-slate-400 tabular-nums">{c.avgAttendance ?? 0}%</span>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* ── Quick-access links ───────────────────────────────────────────── */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {[
            { label: 'Attendance Analytics',  desc: 'Absences, trends, heatmap',  path: '/dean/reports', tab: 'attendance', icon: TrendingUp,    color: 'text-[#667D9D]' },
            { label: 'Student Focus Report',  desc: 'Concentration & distraction', path: '/dean/reports', tab: 'focus',      icon: Brain,         color: 'text-violet-400' },
            { label: 'Lecturer Insights',     desc: 'Performance & engagement',   path: '/dean/reports', tab: 'lecturer',   icon: Award,         color: 'text-amber-400' },
            { label: 'Peak Activity',         desc: 'Best/worst times & slots',   path: '/dean/reports', tab: 'peak',       icon: Activity,      color: 'text-emerald-400' },
          ].map(link => {
            const Icon = link.icon
            return (
              <button
                key={link.label}
                onClick={() => navigate(link.path)}
                className="glass rounded-2xl p-4 text-left hover:border-[#667D9D]/30 transition-all group"
              >
                <div className={`mb-2 ${link.color}`}><Icon size={18} /></div>
                <p className="text-sm font-semibold text-white mb-0.5">{link.label}</p>
                <p className="text-xs text-slate-500">{link.desc}</p>
                <ChevronRight size={13} className="text-slate-600 group-hover:text-[#667D9D] mt-2 transition-colors" />
              </button>
            )
          })}
        </div>

      </div>
    </div>
  )
}
