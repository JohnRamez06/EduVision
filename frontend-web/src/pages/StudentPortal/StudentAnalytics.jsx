import React, { useEffect, useState } from 'react'
import {
  LineChart, Line, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend,
} from 'recharts'
import { BarChart3, Brain, TrendingUp, Zap, AlertTriangle, BookOpen } from 'lucide-react'
import StudentLayout from '../../layouts/StudentLayout'
import studentService from '../../services/studentService'

const Skeleton = ({ className = '' }) => (
  <div className={`animate-pulse rounded-xl bg-slate-800/60 ${className}`} />
)

const EMOTION_COLOR = {
  happy: '#10B981', engaged: '#3B82F6', neutral: '#94A3B8',
  confused: '#F59E0B', sad: '#8B5CF6', angry: '#F43F5E',
  surprised: '#EC4899', fearful: '#FB923C', disgusted: '#6366F1',
}

const CHART_TOOLTIP = {
  contentStyle: { background: '#1E293B', border: '1px solid #334155', borderRadius: 10, fontSize: 12 },
  labelStyle:   { color: '#94A3B8' },
}

export default function StudentAnalytics() {
  const [summaries, setSummaries] = useState([])
  const [dashboard, setDashboard] = useState(null)
  const [loading, setLoading]     = useState(true)
  const [error, setError]         = useState('')

  useEffect(() => {
    studentService.getDashboard()
      .then(d => {
        setDashboard(d)
        return studentService.getSummaries()
          .catch(() => d.recentSummaries ?? [])
      })
      .then(setSummaries)
      .catch(e => setError(e.response?.data?.message ?? 'Failed to load analytics.'))
      .finally(() => setLoading(false))
  }, [])

  // Concentration trend — chronological, last 20 sessions
  const concentrationData = [...summaries]
    .sort((a, b) => new Date(a.date) - new Date(b.date))
    .slice(-20)
    .map(s => ({
      date: s.date ? new Date(s.date).toLocaleDateString('en-GB', { day: 'numeric', month: 'short' }) : '—',
      concentration: s.avgConcentration != null ? Math.round(Number(s.avgConcentration) * 100) : null,
      attentiveness: s.attentivePercentage != null ? Math.round(Number(s.attentivePercentage) * 100) : null,
      engagement:    s.overallEngagement   != null ? Math.round(Number(s.overallEngagement)   * 100) : null,
    }))

  // Emotion breakdown pie
  const emotionPie = dashboard?.overallStats?.emotionBreakdown
    ? Object.entries(dashboard.overallStats.emotionBreakdown)
        .filter(([, v]) => v > 0)
        .map(([name, value]) => ({ name, value: Math.round(Number(value) * 100), fill: EMOTION_COLOR[name] ?? '#94A3B8' }))
        .sort((a, b) => b.value - a.value)
    : []

  // Attendance per course bar
  const attendanceBar = (dashboard?.enrolledCourses ?? []).map(c => ({
    name: c.code ?? c.title,
    fullName: c.title,
    attended: c.attendedSessions,
    total: c.totalSessions,
    pct: c.totalSessions > 0 ? Math.round((c.attendedSessions / c.totalSessions) * 100) : 0,
  }))

  // Per-course concentration — group summaries by course, average concentration
  const courseConcentration = Object.values(
    summaries.reduce((acc, s) => {
      const key = s.courseId ?? s.courseName
      if (!key) return acc
      if (!acc[key]) acc[key] = { name: s.courseName ?? key, total: 0, count: 0 }
      if (s.avgConcentration != null) {
        acc[key].total += Number(s.avgConcentration)
        acc[key].count += 1
      }
      return acc
    }, {})
  )
    .filter(c => c.count > 0)
    .map(c => ({ name: c.name, avg: Math.round((c.total / c.count) * 100) }))
    .sort((a, b) => b.avg - a.avg)

  const stats = dashboard?.overallStats

  return (
    <StudentLayout>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-white flex items-center gap-2">
          <BarChart3 size={22} className="text-[#667D9D]" /> Analytics
        </h1>
        <p className="text-sm text-slate-500 mt-1">Your learning performance over time</p>
      </div>

      {error && (
        <div className="flex items-center gap-2 px-4 py-3 mb-6 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
          <AlertTriangle size={15} className="shrink-0" /> {error}
        </div>
      )}

      {/* KPI row */}
      {loading ? (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
          {[...Array(4)].map((_, i) => <Skeleton key={i} className="h-20" />)}
        </div>
      ) : (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
          {[
            { icon: Brain,      color: 'text-[#667D9D]',   bg: 'bg-[#667D9D]/10',   label: 'Avg Concentration', value: stats?.avgConcentration != null ? `${Math.round(stats.avgConcentration * 100)}%` : '—' },
            { icon: TrendingUp, color: 'text-emerald-400', bg: 'bg-emerald-500/10', label: 'Avg Attentiveness', value: stats?.avgAttentiveness != null ? `${Math.round(stats.avgAttentiveness * 100)}%` : '—' },
            { icon: Zap,        color: 'text-amber-400',   bg: 'bg-amber-500/10',   label: 'Top Emotion',       value: stats?.mostFrequentEmotion ? (stats.mostFrequentEmotion.charAt(0).toUpperCase() + stats.mostFrequentEmotion.slice(1).toLowerCase()) : '—' },
            { icon: BookOpen,   color: 'text-[#667D9D]',   bg: 'bg-[#667D9D]/10',   label: 'Sessions Recorded', value: summaries.length },
          ].map(({ icon: Icon, color, bg, label, value }) => (
            <div key={label} className="glass rounded-2xl p-4 flex items-center gap-3">
              <div className={`w-10 h-10 rounded-xl ${bg} flex items-center justify-center shrink-0`}>
                <Icon size={18} className={color} />
              </div>
              <div>
                <p className="text-xs text-slate-500 mb-0.5">{label}</p>
                <p className="text-lg font-bold text-white">{value}</p>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Concentration trend */}
      <div className="glass rounded-2xl p-5 mb-5">
        <h2 className="font-semibold text-white flex items-center gap-2 mb-5">
          <Brain size={16} className="text-[#667D9D]" /> Concentration & Attentiveness Over Time
        </h2>
        {loading ? <Skeleton className="h-56" /> : concentrationData.length === 0 ? (
          <div className="flex items-center justify-center h-40 text-slate-600 text-sm">No session data yet</div>
        ) : (
          <ResponsiveContainer width="100%" height={220}>
            <LineChart data={concentrationData} margin={{ top: 5, right: 10, left: -20, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#1E293B" />
              <XAxis dataKey="date" tick={{ fontSize: 11, fill: '#64748B' }} />
              <YAxis domain={[0, 100]} tick={{ fontSize: 11, fill: '#64748B' }} unit="%" />
              <Tooltip {...CHART_TOOLTIP} formatter={v => [`${v}%`]} />
              <Legend wrapperStyle={{ fontSize: 12, color: '#94A3B8' }} />
              <Line type="monotone" dataKey="concentration" name="Concentration" stroke="#3B82F6" strokeWidth={2} dot={{ r: 3 }} activeDot={{ r: 5 }} connectNulls />
              <Line type="monotone" dataKey="attentiveness" name="Attentiveness" stroke="#10B981" strokeWidth={2} dot={{ r: 3 }} strokeDasharray="4 2" connectNulls />
              <Line type="monotone" dataKey="engagement"    name="Engagement"    stroke="#F59E0B" strokeWidth={2} dot={{ r: 3 }} strokeDasharray="2 3" connectNulls />
            </LineChart>
          </ResponsiveContainer>
        )}
      </div>

      {/* Emotion + Attendance row */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5 mb-5">

        {/* Emotion breakdown */}
        <div className="glass rounded-2xl p-5">
          <h2 className="font-semibold text-white flex items-center gap-2 mb-4">
            <Zap size={16} className="text-[#667D9D]" /> Emotion Breakdown
          </h2>
          {loading ? <Skeleton className="h-52" /> : emotionPie.length === 0 ? (
            <div className="flex items-center justify-center h-40 text-slate-600 text-sm">No emotion data yet</div>
          ) : (
            <>
              <ResponsiveContainer width="100%" height={180}>
                <PieChart>
                  <Pie data={emotionPie} cx="50%" cy="50%" innerRadius={50} outerRadius={74} paddingAngle={2} dataKey="value">
                    {emotionPie.map((e, i) => <Cell key={i} fill={e.fill} />)}
                  </Pie>
                  <Tooltip {...CHART_TOOLTIP} formatter={(v, n) => [`${v}%`, n]} />
                </PieChart>
              </ResponsiveContainer>
              <div className="grid grid-cols-2 gap-x-4 gap-y-2 mt-2">
                {emotionPie.map(e => (
                  <div key={e.name} className="flex items-center gap-2">
                    <span className="w-2.5 h-2.5 rounded-full shrink-0" style={{ background: e.fill }} />
                    <span className="text-xs text-slate-400 capitalize flex-1">{e.name}</span>
                    <span className="text-xs font-medium text-slate-300">{e.value}%</span>
                  </div>
                ))}
              </div>
            </>
          )}
        </div>

        {/* Attendance per course */}
        <div className="glass rounded-2xl p-5">
          <h2 className="font-semibold text-white flex items-center gap-2 mb-4">
            <TrendingUp size={16} className="text-emerald-400" /> Attendance by Course
          </h2>
          {loading ? <Skeleton className="h-52" /> : attendanceBar.length === 0 ? (
            <div className="flex items-center justify-center h-40 text-slate-600 text-sm">No course data yet</div>
          ) : (
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={attendanceBar} margin={{ top: 5, right: 10, left: -20, bottom: 20 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1E293B" />
                <XAxis dataKey="name" tick={{ fontSize: 10, fill: '#64748B' }} angle={-30} textAnchor="end" interval={0} />
                <YAxis domain={[0, 100]} tick={{ fontSize: 11, fill: '#64748B' }} unit="%" />
                <Tooltip
                  {...CHART_TOOLTIP}
                  formatter={(v, n, p) => [`${v}% (${p.payload.attended}/${p.payload.total} sessions)`, 'Attendance']}
                />
                <Bar dataKey="pct" name="Attendance %" radius={[4, 4, 0, 0]}>
                  {attendanceBar.map((entry, i) => (
                    <Cell key={i} fill={entry.pct >= 80 ? '#10B981' : entry.pct >= 60 ? '#F59E0B' : '#F43F5E'} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>

      {/* Per-course concentration */}
      {!loading && courseConcentration.length > 0 && (
        <div className="glass rounded-2xl p-5">
          <h2 className="font-semibold text-white flex items-center gap-2 mb-4">
            <BookOpen size={16} className="text-[#667D9D]" /> Average Concentration by Course
          </h2>
          <div className="space-y-3">
            {courseConcentration.map(c => {
              const barColor = c.avg >= 70 ? 'from-emerald-500 to-teal-500'
                             : c.avg >= 45 ? 'from-amber-500 to-orange-400'
                             :               'from-rose-500 to-pink-500'
              return (
                <div key={c.name}>
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-xs text-slate-300 truncate max-w-[70%]">{c.name}</span>
                    <span className={`text-xs font-semibold tabular-nums ${
                      c.avg >= 70 ? 'text-emerald-400' : c.avg >= 45 ? 'text-amber-400' : 'text-rose-400'
                    }`}>{c.avg}%</span>
                  </div>
                  <div className="h-2 bg-slate-800 rounded-full overflow-hidden">
                    <div
                      className={`h-full rounded-full bg-gradient-to-r ${barColor} transition-all duration-700`}
                      style={{ width: `${c.avg}%` }}
                    />
                  </div>
                </div>
              )
            })}
          </div>
        </div>
      )}
    </StudentLayout>
  )
}
