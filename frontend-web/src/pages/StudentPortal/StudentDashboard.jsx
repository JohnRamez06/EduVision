import React, { useContext, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer } from 'recharts'
import {
  Brain, BookOpen, TrendingUp, Clock, Zap,
  CheckCircle, AlertTriangle, Info, ChevronRight,
  Calendar, Award,
} from 'lucide-react'
import StudentLayout from '../../layouts/StudentLayout'
import { AuthContext } from '../../context/AuthContext'
import studentService from '../../services/studentService'

// ── Helpers ──────────────────────────────────────────────────────────────────

const EMOTION_COLOR = {
  HAPPY:     '#10B981', EXCITED:  '#06B6D4', FOCUSED:   '#3B82F6',
  NEUTRAL:   '#94A3B8', CONFUSED: '#F59E0B', BORED:     '#8B5CF6',
  DISTRESSED:'#F43F5E', ANXIOUS:  '#FB923C', SURPRISED: '#EC4899',
}
const emotionColor  = e => EMOTION_COLOR[e?.toUpperCase()] ?? '#94A3B8'
const pct           = v => v != null ? `${Math.round((v ?? 0) * 100)}%` : '—'
const greet         = name => {
  const h = new Date().getHours()
  const prefix = h < 12 ? 'Good morning' : h < 17 ? 'Good afternoon' : 'Good evening'
  return `${prefix}, ${name?.split(' ')[0] ?? 'Student'} 👋`
}

const PRIORITY_STYLE = {
  HIGH:   'bg-rose-500/15 text-rose-300 border-rose-500/25',
  MEDIUM: 'bg-amber-500/15 text-amber-300 border-amber-500/25',
  LOW:    'bg-slate-700/40 text-slate-400 border-slate-700',
}
const PRIORITY_ICON = { HIGH: AlertTriangle, MEDIUM: Info, LOW: Info }

// ── Skeleton ─────────────────────────────────────────────────────────────────
const Skeleton = ({ className = '' }) => (
  <div className={`animate-pulse rounded-xl bg-slate-800/60 ${className}`} />
)

// ── Stat card ─────────────────────────────────────────────────────────────────
const StatCard = ({ icon: Icon, label, value, sub, color = 'blue' }) => {
  const colors = {
    blue:   'bg-blue-500/15 text-blue-400',
    emerald:'bg-emerald-500/15 text-emerald-400',
    violet: 'bg-violet-500/15 text-violet-400',
    amber:  'bg-amber-500/15 text-amber-400',
  }
  return (
    <div className="glass rounded-2xl p-5 flex items-center gap-4">
      <div className={`w-11 h-11 rounded-xl flex items-center justify-center shrink-0 ${colors[color]}`}>
        <Icon size={20} />
      </div>
      <div className="min-w-0">
        <p className="text-xs text-slate-500 font-medium mb-0.5">{label}</p>
        <p className="text-xl font-bold text-white leading-none">{value}</p>
        {sub && <p className="text-xs text-slate-500 mt-0.5">{sub}</p>}
      </div>
    </div>
  )
}

// ── Attendance pill ───────────────────────────────────────────────────────────
const AttBadge = ({ status }) => {
  const s = status?.toLowerCase()
  const cls = s === 'present' ? 'text-emerald-400 bg-emerald-500/10'
            : s === 'late'    ? 'text-amber-400 bg-amber-500/10'
            : s === 'absent'  ? 'text-rose-400 bg-rose-500/10'
            :                   'text-slate-400 bg-slate-700/40'
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium capitalize ${cls}`}>
      {status ?? '—'}
    </span>
  )
}

// ── Main component ────────────────────────────────────────────────────────────
export default function StudentDashboard() {
  const { user } = useContext(AuthContext)
  const navigate = useNavigate()
  const [data, setData]       = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState('')

  useEffect(() => {
    studentService.getDashboard()
      .then(setData)
      .catch(e => setError(e.response?.data?.message ?? 'Failed to load dashboard.'))
      .finally(() => setLoading(false))
  }, [])

  // emotion breakdown → recharts format
  const emotionPie = data?.overallStats?.emotionBreakdown
    ? Object.entries(data.overallStats.emotionBreakdown).map(([name, value]) => ({
        name, value: Math.round(value * 100), fill: emotionColor(name),
      }))
    : []

  const profile = data?.studentInfo
  const stats   = data?.overallStats

  return (
    <StudentLayout>
      {/* Greeting */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-white">
          {loading ? 'Loading…' : greet(profile?.fullName ?? user?.fullName)}
        </h1>
        <p className="text-sm text-slate-500 mt-1">
          {profile?.program
            ? `${profile.program} · Year ${profile.yearOfStudy} · ${profile.studentNumber}`
            : 'Your learning overview'}
        </p>
      </div>

      {/* Error */}
      {error && (
        <div className="flex items-center gap-2 px-4 py-3 mb-6 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
          <AlertTriangle size={15} className="shrink-0" /> {error}
        </div>
      )}

      {/* ── Stats Row ─────────────────────────────────────────────────────── */}
      {loading ? (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
          {[...Array(4)].map((_, i) => <Skeleton key={i} className="h-24" />)}
        </div>
      ) : (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
          <StatCard
            icon={Brain} color="blue" label="Avg Concentration"
            value={pct(stats?.avgConcentration)}
            sub="across all lectures"
          />
          <StatCard
            icon={TrendingUp} color="emerald" label="Avg Attentiveness"
            value={pct(stats?.avgAttentiveness)}
            sub="engagement score"
          />
          <StatCard
            icon={Calendar} color="violet" label="Lectures Attended"
            value={stats?.totalLecturesAttended ?? '—'}
            sub="total sessions"
          />
          <StatCard
            icon={Award} color="amber" label="Top Emotion"
            value={stats?.mostFrequentEmotion
              ? stats.mostFrequentEmotion.charAt(0) + stats.mostFrequentEmotion.slice(1).toLowerCase()
              : '—'}
            sub="most frequent"
          />
        </div>
      )}

      {/* ── Courses + Emotion Breakdown ───────────────────────────────────── */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-5 mb-5">

        {/* Enrolled Courses */}
        <div className="lg:col-span-2 glass rounded-2xl p-5">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold text-white flex items-center gap-2">
              <BookOpen size={16} className="text-blue-400" /> Enrolled Courses
            </h2>
            <span className="text-xs text-slate-500">{data?.enrolledCourses?.length ?? 0} courses</span>
          </div>

          {loading ? (
            <div className="space-y-3">{[...Array(3)].map((_, i) => <Skeleton key={i} className="h-16" />)}</div>
          ) : data?.enrolledCourses?.length === 0 ? (
            <p className="text-sm text-slate-500 text-center py-8">No enrolled courses yet.</p>
          ) : (
            <div className="space-y-3">
              {data?.enrolledCourses?.map(c => {
                const pct = c.totalSessions > 0
                  ? Math.round((c.attendedSessions / c.totalSessions) * 100)
                  : 0
                return (
                  <div key={c.courseId} onClick={() => navigate('/student/courses')} className="flex items-center gap-4 p-3.5 rounded-xl bg-navy-900/50 hover:bg-navy-900/80 transition-colors group cursor-pointer">
                    <div className="w-10 h-10 rounded-lg bg-blue-500/15 flex items-center justify-center shrink-0">
                      <BookOpen size={16} className="text-blue-400" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between mb-1">
                        <p className="text-sm font-medium text-slate-200 truncate">{c.title}</p>
                        <span className="text-xs text-slate-500 ml-2 shrink-0">{pct}%</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <div className="flex-1 h-1.5 bg-slate-700 rounded-full overflow-hidden">
                          <div
                            className="h-full rounded-full bg-gradient-to-r from-blue-500 to-violet-500 transition-all"
                            style={{ width: `${pct}%` }}
                          />
                        </div>
                        <span className="text-xs text-slate-600 shrink-0">
                          {c.attendedSessions}/{c.totalSessions} sessions
                        </span>
                      </div>
                      <p className="text-xs text-slate-600 mt-0.5">{c.code} · {c.department}</p>
                    </div>
                    <ChevronRight size={14} className="text-slate-600 group-hover:text-slate-400 shrink-0 transition-colors" />
                  </div>
                )
              })}
            </div>
          )}
        </div>

        {/* Emotion Breakdown */}
        <div className="glass rounded-2xl p-5">
          <h2 className="font-semibold text-white flex items-center gap-2 mb-4">
            <Zap size={16} className="text-violet-400" /> Emotion Breakdown
          </h2>
          {loading ? (
            <Skeleton className="h-48" />
          ) : emotionPie.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-48 text-slate-600 text-sm">
              <Brain size={28} className="mb-2 opacity-40" />
              No emotion data yet
            </div>
          ) : (
            <>
              <ResponsiveContainer width="100%" height={180}>
                <PieChart>
                  <Pie data={emotionPie} cx="50%" cy="50%" innerRadius={48} outerRadius={72} paddingAngle={3} dataKey="value">
                    {emotionPie.map((entry, i) => <Cell key={i} fill={entry.fill} />)}
                  </Pie>
                  <Tooltip
                    contentStyle={{ background: '#1E293B', border: '1px solid #334155', borderRadius: 10, fontSize: 12 }}
                    formatter={(v, n) => [`${v}%`, n.charAt(0) + n.slice(1).toLowerCase()]}
                  />
                </PieChart>
              </ResponsiveContainer>
              <div className="space-y-2 mt-2">
                {emotionPie.map(e => (
                  <div key={e.name} className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <span className="w-2.5 h-2.5 rounded-full shrink-0" style={{ background: e.fill }} />
                      <span className="text-xs text-slate-400 capitalize">{e.name.toLowerCase()}</span>
                    </div>
                    <span className="text-xs font-medium text-slate-300">{e.value}%</span>
                  </div>
                ))}
              </div>
            </>
          )}
        </div>
      </div>

      {/* ── Recent Lectures + Recommendations ────────────────────────────── */}
      <div className="grid grid-cols-1 lg:grid-cols-5 gap-5">

        {/* Recent Lectures table */}
        <div className="lg:col-span-3 glass rounded-2xl p-5">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold text-white flex items-center gap-2">
              <Clock size={16} className="text-emerald-400" /> Recent Lectures
            </h2>
          </div>

          {loading ? (
            <div className="space-y-3">{[...Array(4)].map((_, i) => <Skeleton key={i} className="h-12" />)}</div>
          ) : data?.recentSummaries?.length === 0 ? (
            <p className="text-sm text-slate-500 text-center py-8">No lectures yet.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-xs text-slate-500 border-b border-slate-800">
                    <th className="text-left pb-2 font-medium">Course</th>
                    <th className="text-left pb-2 font-medium">Date</th>
                    <th className="text-center pb-2 font-medium">Concentration</th>
                    <th className="text-center pb-2 font-medium">Attendance</th>
                    <th className="text-left pb-2 font-medium">Emotion</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60">
                  {data?.recentSummaries?.map(s => (
                    <tr key={s.sessionId} className="hover:bg-slate-800/30 transition-colors group">
                      <td className="py-3 pr-3 font-medium text-slate-200 truncate max-w-[120px]">{s.courseName}</td>
                      <td className="py-3 pr-3 text-slate-500 whitespace-nowrap">
                        {s.date ? new Date(s.date).toLocaleDateString('en-GB', { day: 'numeric', month: 'short' }) : '—'}
                      </td>
                      <td className="py-3 pr-3 text-center">
                        <span className={`font-semibold ${
                          (s.avgConcentration ?? 0) >= 0.7 ? 'text-emerald-400'
                          : (s.avgConcentration ?? 0) >= 0.4 ? 'text-amber-400'
                          : 'text-rose-400'
                        }`}>
                          {pct(s.avgConcentration)}
                        </span>
                      </td>
                      <td className="py-3 pr-3 text-center">
                        <AttBadge status={s.attendance} />
                      </td>
                      <td className="py-3">
                        {s.dominantEmotion ? (
                          <span className="inline-flex items-center gap-1.5 text-xs">
                            <span className="w-2 h-2 rounded-full" style={{ background: emotionColor(s.dominantEmotion) }} />
                            <span className="text-slate-400 capitalize">{s.dominantEmotion.toLowerCase()}</span>
                          </span>
                        ) : '—'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Recommendations */}
        <div className="lg:col-span-2 glass rounded-2xl p-5">
          <h2 className="font-semibold text-white flex items-center gap-2 mb-4">
            <Zap size={16} className="text-amber-400" /> AI Recommendations
          </h2>

          {loading ? (
            <div className="space-y-3">{[...Array(3)].map((_, i) => <Skeleton key={i} className="h-20" />)}</div>
          ) : data?.recommendations?.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-40 text-slate-600 text-sm">
              <CheckCircle size={24} className="mb-2 opacity-40" />
              All good — no recommendations yet
            </div>
          ) : (
            <div className="space-y-3">
              {data?.recommendations?.map((r, i) => {
                const Icon = PRIORITY_ICON[r.priority] ?? Info
                return (
                  <div key={i} className={`p-3.5 rounded-xl border ${PRIORITY_STYLE[r.priority] ?? PRIORITY_STYLE.LOW}`}>
                    <div className="flex items-center gap-2 mb-1">
                      <Icon size={13} className="shrink-0" />
                      <span className="text-xs font-semibold uppercase tracking-wide">{r.priority}</span>
                    </div>
                    <p className="text-sm font-medium text-slate-200 mb-0.5">{r.title}</p>
                    <p className="text-xs text-slate-500 leading-snug">{r.description}</p>
                  </div>
                )
              })}
            </div>
          )}
        </div>
      </div>
    </StudentLayout>
  )
}
