import React, { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  LineChart, Line, BarChart, Bar, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend,
} from 'recharts'
import {
  BarChart3, Brain, AlertTriangle, Users, Activity,
  TrendingDown, TrendingUp, Cpu, ChevronLeft, BookOpen, Printer,
} from 'lucide-react'
import lecturerService from '../../services/lecturerService'

// ── Layout ────────────────────────────────────────────────────────────────────
function LecturerLayout({ children }) {
  const navigate = useNavigate()
  return (
    <div className="min-h-screen p-6" style={{ background: 'var(--bg-app, #0f172a)' }}>
      <div className="max-w-7xl mx-auto">
        <div className="flex items-center justify-between mb-6">
          <button
            onClick={() => navigate(-1)}
            className="flex items-center gap-2 text-slate-500 hover:text-slate-300 text-sm transition-colors"
          >
            <ChevronLeft size={16} /> Back
          </button>
          <button
            onClick={() => window.print()}
            className="flex items-center gap-2 px-3 py-1.5 rounded-xl text-xs font-medium text-slate-400 border border-slate-800 hover:text-slate-200 hover:border-slate-700 transition-all"
          >
            <Printer size={13} /> Print / Save as PDF
          </button>
        </div>
        {children}
      </div>
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

const RISK_BADGE = {
  HIGH:   'bg-rose-500/15 text-rose-300 border border-rose-500/25',
  MEDIUM: 'bg-amber-500/15 text-amber-300 border border-amber-500/25',
  LOW:    'bg-emerald-500/15 text-emerald-300 border border-emerald-500/25',
}

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

// ── Session Focus Timeline ────────────────────────────────────────────────────
function SessionTimeline({ sessionId }) {
  const [data, setData]     = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError]   = useState('')

  useEffect(() => {
    if (!sessionId) return
    lecturerService.getSessionFocusTimeline(sessionId)
      .then(d => setData(d.map(r => ({ ...r, avgFocus: Number(r.avgFocus) }))))
      .catch(e => setError(e.response?.data?.message ?? 'Failed to load timeline.'))
      .finally(() => setLoading(false))
  }, [sessionId])

  if (!sessionId) return <div className="glass rounded-2xl p-5"><p className="text-slate-500 text-sm">Select a session from the comparison table to view its focus timeline.</p></div>
  if (loading)   return <Skeleton className="h-64" />
  if (error)     return <ErrorBanner msg={error} />

  // Detect when focus first drops below 50%
  const dropPoint = data.find(d => d.avgFocus < 50)

  return (
    <div className="glass rounded-2xl p-5">
      <h3 className="font-semibold text-white mb-1 flex items-center gap-2">
        <Activity size={15} className="text-[#667D9D]" /> Focus Timeline
      </h3>
      {dropPoint && (
        <p className="text-xs text-amber-400 mb-3">
          ⚠️ Focus dropped below 50% at <strong>{dropPoint.timeBucket}</strong>
        </p>
      )}
      {data.length === 0 ? <Empty /> : (
        <ResponsiveContainer width="100%" height={240}>
          <LineChart data={data}>
            <CartesianGrid strokeDasharray="3 3" stroke="#1E293B" />
            <XAxis dataKey="timeBucket" tick={{ fontSize: 10, fill: '#64748B' }} interval="preserveStartEnd" />
            <YAxis domain={[0, 100]} tick={{ fontSize: 11, fill: '#64748B' }} unit="%" />
            <Tooltip {...TOOLTIP} formatter={(v, n) => [`${v}%`, n]} />
            <Legend wrapperStyle={{ fontSize: 12, color: '#94A3B8' }} />
            <Line type="monotone" dataKey="avgFocus" name="Avg Focus" stroke="#3B82F6" strokeWidth={2} dot={false} connectNulls />
            <Line type="monotone" dataKey="studentCount" name="Students" stroke="#10B981" strokeWidth={1.5} dot={false} strokeDasharray="4 2" connectNulls yAxisId={1} />
          </LineChart>
        </ResponsiveContainer>
      )}
    </div>
  )
}

// ── At-Risk Students ──────────────────────────────────────────────────────────
function AtRiskTable({ courseId }) {
  const [data, setData]     = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError]   = useState('')

  useEffect(() => {
    lecturerService.getCourseAtRiskStudents(courseId)
      .then(setData)
      .catch(e => setError(e.response?.data?.message ?? 'Failed to load at-risk data.'))
      .finally(() => setLoading(false))
  }, [courseId])

  if (loading) return <Skeleton className="h-64" />
  if (error)   return <ErrorBanner msg={error} />

  const highRisk   = data.filter(s => s.riskLevel === 'HIGH')
  const otherRisk  = data.filter(s => s.riskLevel !== 'HIGH')

  return (
    <div className="space-y-4">
      {highRisk.length > 0 && (
        <div className="flex items-center gap-2 px-4 py-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-300 text-sm">
          <AlertTriangle size={14} className="shrink-0" />
          <strong>{highRisk.length} student{highRisk.length > 1 ? 's' : ''}</strong> require immediate attention
        </div>
      )}
      <div className="glass rounded-2xl p-5">
        <h3 className="font-semibold text-white mb-4 flex items-center gap-2">
          <TrendingDown size={15} className="text-rose-400" /> At-Risk Students
        </h3>
        {data.length === 0 ? <Empty msg="All students are on track" /> : (
          <div className="overflow-x-auto">
            <table className="w-full text-xs">
              <thead><tr className="text-slate-500 uppercase tracking-wide text-left">
                <th className="pb-3 pr-4">Student</th>
                <th className="pb-3 pr-4 text-center">Risk</th>
                <th className="pb-3 pr-4 text-center">Avg Focus</th>
                <th className="pb-3 pr-4 text-center">Attentive</th>
                <th className="pb-3 pr-4 text-center">Absences</th>
                <th className="pb-3 pr-4 text-center">Absence %</th>
                <th className="pb-3">Dominant Emotion</th>
              </tr></thead>
              <tbody>
                {data.map((s, i) => (
                  <tr key={i} className="border-t border-slate-800/40 hover:bg-slate-800/30">
                    <td className="py-2.5 pr-4 font-medium text-slate-200">{s.studentName}</td>
                    <td className="py-2.5 pr-4 text-center">
                      <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${RISK_BADGE[s.riskLevel] ?? RISK_BADGE.LOW}`}>
                        {s.riskLevel}
                      </span>
                    </td>
                    <td className="py-2.5 pr-4 text-center">
                      <span className={`font-semibold ${Number(s.avgFocus) < 40 ? 'text-rose-400' : Number(s.avgFocus) < 60 ? 'text-amber-400' : 'text-emerald-400'}`}>
                        {s.avgFocus ?? '—'}%
                      </span>
                    </td>
                    <td className="py-2.5 pr-4 text-center text-slate-300">{s.avgAttentiveness ?? '—'}%</td>
                    <td className="py-2.5 pr-4 text-center text-rose-400 font-semibold">{s.absences}</td>
                    <td className="py-2.5 pr-4 text-center text-slate-400">{s.absenceRate}%</td>
                    <td className="py-2.5 text-slate-400 capitalize">{s.dominantEmotion ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}

// ── Lecture Comparison ────────────────────────────────────────────────────────
function LectureComparison({ courseId, onSelectSession }) {
  const [data, setData]     = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError]   = useState('')

  useEffect(() => {
    lecturerService.getCourseLectureComparison(courseId)
      .then(setData)
      .catch(e => setError(e.response?.data?.message ?? 'Failed to load lecture comparison.'))
      .finally(() => setLoading(false))
  }, [courseId])

  if (loading) return <Skeleton className="h-64" />
  if (error)   return <ErrorBanner msg={error} />

  const chartData = data.map(s => ({
    name: s.title ?? s.date,
    focus: Number(s.avgFocus),
    attendance: Number(s.attendancePct),
    attentiveness: Number(s.avgAttentiveness),
  }))

  return (
    <div className="space-y-5">
      {/* Chart */}
      {chartData.length > 0 && (
        <div className="glass rounded-2xl p-5">
          <h3 className="font-semibold text-white mb-4 flex items-center gap-2">
            <BarChart3 size={15} className="text-[#667D9D]" /> Lecture Trend (Focus & Attendance)
          </h3>
          <ResponsiveContainer width="100%" height={220}>
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#1E293B" />
              <XAxis dataKey="name" tick={{ fontSize: 9, fill: '#64748B' }} angle={-20} textAnchor="end" height={40} />
              <YAxis domain={[0, 100]} tick={{ fontSize: 11, fill: '#64748B' }} unit="%" />
              <Tooltip {...TOOLTIP} formatter={v => [`${v}%`]} />
              <Legend wrapperStyle={{ fontSize: 12, color: '#94A3B8' }} />
              <Line type="monotone" dataKey="focus" name="Avg Focus" stroke="#8B5CF6" strokeWidth={2} dot={{ r: 3 }} connectNulls />
              <Line type="monotone" dataKey="attendance" name="Attendance" stroke="#10B981" strokeWidth={2} dot={{ r: 3 }} strokeDasharray="4 2" connectNulls />
            </LineChart>
          </ResponsiveContainer>
        </div>
      )}

      {/* Table */}
      <div className="glass rounded-2xl p-5">
        <h3 className="font-semibold text-white mb-4 flex items-center gap-2">
          <BookOpen size={15} className="text-[#667D9D]" /> Lecture-by-Lecture Breakdown
        </h3>
        {data.length === 0 ? <Empty /> : (
          <div className="overflow-x-auto">
            <table className="w-full text-xs">
              <thead><tr className="text-slate-500 uppercase tracking-wide text-left">
                <th className="pb-3 pr-4">Lecture</th>
                <th className="pb-3 pr-4">Date</th>
                <th className="pb-3 pr-4 text-center">Attendance</th>
                <th className="pb-3 pr-4 text-center">Avg Focus</th>
                <th className="pb-3 pr-4 text-center">Attentiveness</th>
                <th className="pb-3 pr-4 text-center">Alerts</th>
                <th className="pb-3 text-center">Timeline</th>
              </tr></thead>
              <tbody>
                {data.map((s, i) => (
                  <tr key={i} className="border-t border-slate-800/40 hover:bg-slate-800/30">
                    <td className="py-2.5 pr-4 text-slate-300 truncate max-w-[120px]">{s.title ?? `Lecture ${i + 1}`}</td>
                    <td className="py-2.5 pr-4 text-slate-500 whitespace-nowrap">
                      {s.date ? new Date(s.date).toLocaleDateString('en-GB', { day: 'numeric', month: 'short' }) : '—'}
                    </td>
                    <td className="py-2.5 pr-4 text-center">
                      <span className={`font-semibold ${Number(s.attendancePct) >= 80 ? 'text-emerald-400' : Number(s.attendancePct) >= 60 ? 'text-amber-400' : 'text-rose-400'}`}>
                        {s.attendancePct ?? 0}%
                      </span>
                      <span className="text-slate-600 ml-1">({s.studentsPresent}/{s.totalEnrolled})</span>
                    </td>
                    <td className="py-2.5 pr-4 text-center">
                      <span className={`font-semibold ${Number(s.avgFocus) >= 70 ? 'text-emerald-400' : Number(s.avgFocus) >= 45 ? 'text-amber-400' : Number(s.avgFocus) > 0 ? 'text-rose-400' : 'text-slate-600'}`}>
                        {s.avgFocus > 0 ? `${s.avgFocus}%` : '—'}
                      </span>
                    </td>
                    <td className="py-2.5 pr-4 text-center text-slate-300">{s.avgAttentiveness > 0 ? `${s.avgAttentiveness}%` : '—'}</td>
                    <td className="py-2.5 pr-4 text-center">
                      {s.alertCount > 0 && <span className="text-rose-400 font-semibold">{s.alertCount} ⚠️</span>}
                      {s.alertCount === 0 && <span className="text-slate-600">—</span>}
                    </td>
                    <td className="py-2.5 text-center">
                      <button
                        onClick={() => onSelectSession(s.sessionId)}
                        className="text-xs px-2 py-0.5 rounded-lg bg-[#667D9D]/15 text-[#ACBBC6] border border-[#667D9D]/20 hover:bg-[#667D9D]/25 transition-all"
                      >
                        View
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}

// ── Behavioral Patterns ───────────────────────────────────────────────────────
function BehavioralPatterns({ courseId }) {
  const [data, setData]     = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError]   = useState('')

  useEffect(() => {
    lecturerService.getCourseBehavioralPatterns(courseId)
      .then(setData)
      .catch(e => setError(e.response?.data?.message ?? 'Failed to load behavioral patterns.'))
      .finally(() => setLoading(false))
  }, [courseId])

  if (loading) return <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">{[...Array(2)].map((_, i) => <Skeleton key={i} className="h-48" />)}</div>
  if (error)   return <ErrorBanner msg={error} />

  const halves  = data?.focusBySessionHalf ?? []
  const exits   = data?.exitPatterns ?? []
  const trend   = (data?.engagementTrend ?? []).map(r => ({ ...r, avgFocus: Number(r.avgFocus), avgAttentiveness: Number(r.avgAttentiveness) }))
  const emotions = data?.studentEmotionPatterns ?? []

  return (
    <div className="space-y-5">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
        {/* Session half focus */}
        <div className="glass rounded-2xl p-5">
          <h3 className="font-semibold text-white mb-4 flex items-center gap-2">
            <Activity size={15} className="text-[#667D9D]" /> Focus: First vs Second Half
          </h3>
          {halves.length === 0 ? <Empty /> : (
            <div className="space-y-4">
              {halves.map(h => (
                <div key={h.sessionHalf}>
                  <div className="flex justify-between text-xs mb-1">
                    <span className="text-slate-300">{h.sessionHalf}</span>
                    <span className={`font-semibold ${Number(h.avgFocus) >= 70 ? 'text-emerald-400' : Number(h.avgFocus) >= 45 ? 'text-amber-400' : 'text-rose-400'}`}>{h.avgFocus}%</span>
                  </div>
                  <div className="h-3 bg-slate-800 rounded-full overflow-hidden">
                    <div
                      className={`h-full rounded-full ${Number(h.avgFocus) >= 70 ? 'bg-emerald-500' : Number(h.avgFocus) >= 45 ? 'bg-amber-500' : 'bg-rose-500'}`}
                      style={{ width: `${h.avgFocus}%` }}
                    />
                  </div>
                </div>
              ))}
              {halves.length === 2 && (
                <p className="text-xs text-slate-500 mt-2">
                  {Number(halves[0]?.avgFocus) > Number(halves[1]?.avgFocus)
                    ? '📉 Focus drops in the second half — consider a mid-session break.'
                    : '📈 Students warm up — engagement improves as the session progresses.'}
                </p>
              )}
            </div>
          )}
        </div>

        {/* Exit patterns */}
        <div className="glass rounded-2xl p-5">
          <h3 className="font-semibold text-white mb-4 flex items-center gap-2">
            <TrendingDown size={15} className="text-rose-400" /> Student Exit Patterns
          </h3>
          {exits.length === 0 ? <Empty msg="No exit data recorded" /> : (
            <div className="space-y-2">
              {exits.map((e, i) => (
                <div key={i} className="flex items-center justify-between py-2 border-b border-slate-800/40 last:border-0">
                  <span className="text-sm text-slate-300">{e.studentName}</span>
                  <div className="flex items-center gap-3 text-xs text-slate-500">
                    <span><strong className="text-rose-400">{e.totalExits}</strong> exits</span>
                    <span><strong className="text-amber-400">{e.avgAbsenceMinutes}m</strong> avg</span>
                    <span>{e.sessionsWithExits} sessions</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Engagement trend */}
      {trend.length > 0 && (
        <div className="glass rounded-2xl p-5">
          <h3 className="font-semibold text-white mb-4 flex items-center gap-2">
            <TrendingUp size={15} className="text-emerald-400" /> Engagement Trend Across Lectures
          </h3>
          <ResponsiveContainer width="100%" height={200}>
            <LineChart data={trend}>
              <CartesianGrid strokeDasharray="3 3" stroke="#1E293B" />
              <XAxis dataKey="date" tick={{ fontSize: 10, fill: '#64748B' }} />
              <YAxis domain={[0, 100]} tick={{ fontSize: 11, fill: '#64748B' }} unit="%" />
              <Tooltip {...TOOLTIP} formatter={v => [`${v}%`]} />
              <Legend wrapperStyle={{ fontSize: 12, color: '#94A3B8' }} />
              <Line type="monotone" dataKey="avgFocus" name="Focus" stroke="#3B82F6" strokeWidth={2} dot={{ r: 3 }} connectNulls />
              <Line type="monotone" dataKey="avgAttentiveness" name="Attentiveness" stroke="#10B981" strokeWidth={2} dot={{ r: 3 }} strokeDasharray="4 2" connectNulls />
            </LineChart>
          </ResponsiveContainer>
        </div>
      )}
    </div>
  )
}

// ── AI Predictions ────────────────────────────────────────────────────────────
function AIPredictions({ courseId }) {
  const [data, setData]     = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError]   = useState('')

  useEffect(() => {
    lecturerService.getCourseAIPredictions(courseId)
      .then(setData)
      .catch(e => setError(e.response?.data?.message ?? 'Failed to load AI predictions.'))
      .finally(() => setLoading(false))
  }, [courseId])

  if (loading) return <Skeleton className="h-64" />
  if (error)   return <ErrorBanner msg={error} />

  const RISK_COLOR = { 'High Risk': 'text-rose-400', 'Moderate': 'text-amber-400', 'Low Risk': 'text-emerald-400' }

  return (
    <div className="glass rounded-2xl p-5">
      <h3 className="font-semibold text-white mb-2 flex items-center gap-2">
        <Cpu size={15} className="text-[#667D9D]" /> AI Risk Predictions
      </h3>
      <p className="text-xs text-slate-500 mb-4">Based on focus, attendance and distraction patterns. Model: weighted rule-based scoring.</p>
      {data.length === 0 ? <Empty /> : (
        <div className="overflow-x-auto">
          <table className="w-full text-xs">
            <thead><tr className="text-slate-500 uppercase tracking-wide text-left">
              <th className="pb-3 pr-4">Student</th>
              <th className="pb-3 pr-4 text-center">Risk Score</th>
              <th className="pb-3 pr-4 text-center">Failing Risk</th>
              <th className="pb-3 pr-4 text-center">Likely to Absent</th>
              <th className="pb-3 text-center">Engagement Dropping</th>
            </tr></thead>
            <tbody>
              {data.map((s, i) => (
                <tr key={i} className="border-t border-slate-800/40 hover:bg-slate-800/30">
                  <td className="py-2.5 pr-4 font-medium text-slate-200">{s.studentName}</td>
                  <td className="py-2.5 pr-4 text-center">
                    <div className="inline-flex items-center gap-2">
                      <div className="w-16 h-2 bg-slate-800 rounded-full overflow-hidden">
                        <div
                          className={`h-full rounded-full ${Number(s.riskScore) >= 50 ? 'bg-rose-500' : Number(s.riskScore) >= 30 ? 'bg-amber-500' : 'bg-emerald-500'}`}
                          style={{ width: `${Math.min(100, Number(s.riskScore))}%` }}
                        />
                      </div>
                      <span className={RISK_COLOR[s.atRiskOfFailing] ?? 'text-slate-300'}>{s.riskScore}</span>
                    </div>
                  </td>
                  <td className="py-2.5 pr-4 text-center">
                    <span className={`font-medium ${RISK_COLOR[s.atRiskOfFailing] ?? 'text-slate-300'}`}>{s.atRiskOfFailing}</span>
                  </td>
                  <td className="py-2.5 pr-4 text-center">
                    <span className={s.likelyToAbsent === 'Yes' ? 'text-rose-400' : s.likelyToAbsent === 'Maybe' ? 'text-amber-400' : 'text-emerald-400'}>
                      {s.likelyToAbsent}
                    </span>
                  </td>
                  <td className="py-2.5 text-center">
                    <span className={s.engagementDecreasing === 'Yes' ? 'text-rose-400' : s.engagementDecreasing === 'Possibly' ? 'text-amber-400' : 'text-emerald-400'}>
                      {s.engagementDecreasing}
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

// ── Main page ─────────────────────────────────────────────────────────────────
const TABS = [
  { id: 'comparison',  label: 'Lecture Comparison', icon: BarChart3     },
  { id: 'at-risk',     label: 'At-Risk Students',   icon: AlertTriangle },
  { id: 'patterns',    label: 'Behavioral Patterns', icon: Activity      },
  { id: 'ai',          label: 'AI Predictions',      icon: Cpu           },
]

export default function LecturerCourseAnalytics() {
  const { courseId } = useParams()
  const [tab, setTab]             = useState('comparison')
  const [selectedSession, setSelectedSession] = useState(null)
  const [courseInfo, setCourseInfo] = useState(null)

  useEffect(() => {
    lecturerService.getDashboard()
      .then(d => {
        const course = (d.courses ?? []).find(c => c.courseId === courseId)
        if (course) setCourseInfo(course)
      })
      .catch(() => {})
  }, [courseId])

  return (
    <LecturerLayout>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-white flex items-center gap-2">
          <BarChart3 size={22} className="text-[#667D9D]" />
          {courseInfo ? `${courseInfo.code} – ${courseInfo.title}` : 'Course Analytics'}
        </h1>
        <p className="text-sm text-slate-500 mt-1">Detailed analytics, at-risk students, and AI predictions for this course</p>
      </div>

      {/* Tab bar */}
      <div className="flex gap-2 mb-6 flex-wrap">
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

      {tab === 'comparison' && (
        <div className="space-y-5">
          <LectureComparison courseId={courseId} onSelectSession={setSelectedSession} />
          <SessionTimeline sessionId={selectedSession} />
        </div>
      )}
      {tab === 'at-risk'    && <AtRiskTable courseId={courseId} />}
      {tab === 'patterns'   && <BehavioralPatterns courseId={courseId} />}
      {tab === 'ai'         && <AIPredictions courseId={courseId} />}
    </LecturerLayout>
  )
}
