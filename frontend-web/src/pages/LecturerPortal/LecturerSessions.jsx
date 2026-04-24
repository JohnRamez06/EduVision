import React, { useEffect, useState } from 'react'
import { Clock, Users, AlertTriangle, CheckCircle, TrendingUp } from 'lucide-react'
import LecturerLayout from '../../layouts/LecturerLayout'
import lecturerService from '../../services/lecturerService'

const Skeleton = ({ className = '' }) => (
  <div className={`animate-pulse rounded-xl bg-slate-800/60 ${className}`} />
)

function EngagementBadge({ value }) {
  const pct = Math.round(value ?? 0)
  if (pct >= 70) return <span className="text-xs font-semibold text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded-full">{pct}%</span>
  if (pct >= 45) return <span className="text-xs font-semibold text-amber-400 bg-amber-500/10 px-2 py-0.5 rounded-full">{pct}%</span>
  return <span className="text-xs font-semibold text-rose-400 bg-rose-500/10 px-2 py-0.5 rounded-full">{pct}%</span>
}

export default function LecturerSessions() {
  const [sessions, setSessions] = useState([])
  const [selected, setSelected] = useState(null)
  const [students, setStudents] = useState([])
  const [loadingStudents, setLoadingStudents] = useState(false)
  const [loading, setLoading]   = useState(true)
  const [error, setError]       = useState('')

  useEffect(() => {
    lecturerService.getDashboard()
      .then(d => setSessions(d.recentSessions ?? []))
      .catch(e => setError(e.response?.data?.message ?? 'Failed to load sessions.'))
      .finally(() => setLoading(false))
  }, [])

  const openSession = async (session) => {
    if (selected?.sessionId === session.sessionId) {
      setSelected(null)
      setStudents([])
      return
    }
    setSelected(session)
    setLoadingStudents(true)
    try {
      const data = await lecturerService.getSessionStudents(session.sessionId)
      setStudents(data)
    } catch {
      setStudents([])
    } finally {
      setLoadingStudents(false)
    }
  }

  return (
    <LecturerLayout>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-white flex items-center gap-2">
          <Clock size={22} className="text-teal-400" /> Session History
        </h1>
        <p className="text-sm text-slate-500 mt-1">Your completed lecture sessions</p>
      </div>

      {error && (
        <div className="flex items-center gap-2 px-4 py-3 mb-6 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
          <AlertTriangle size={15} className="shrink-0" /> {error}
        </div>
      )}

      {loading ? (
        <div className="space-y-3">
          {[...Array(4)].map((_, i) => <Skeleton key={i} className="h-20" />)}
        </div>
      ) : sessions.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-24 text-slate-600">
          <Clock size={40} className="mb-3 opacity-30" />
          <p className="text-sm">No completed sessions yet.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {sessions.map(s => (
            <div key={s.sessionId}>
              <button
                onClick={() => openSession(s)}
                className="w-full glass rounded-2xl p-5 flex items-center gap-5 hover:border-teal-500/30 transition-all text-left group"
              >
                {/* Date block */}
                <div className="w-14 text-center shrink-0">
                  <p className="text-lg font-bold text-white leading-none">
                    {s.date ? new Date(s.date).getDate() : '—'}
                  </p>
                  <p className="text-xs text-slate-500 uppercase">
                    {s.date ? new Date(s.date).toLocaleDateString('en-GB', { month: 'short' }) : ''}
                  </p>
                </div>

                <div className="w-px h-10 bg-slate-800 shrink-0" />

                {/* Course */}
                <div className="flex-1 min-w-0">
                  <p className="font-medium text-slate-200 truncate">{s.courseName ?? 'Unknown course'}</p>
                  <p className="text-xs text-slate-500 mt-0.5">
                    {s.date ? new Date(s.date).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' }) : ''}
                  </p>
                </div>

                {/* Stats */}
                <div className="flex items-center gap-5 shrink-0">
                  <div className="flex items-center gap-1.5 text-sm text-slate-400">
                    <Users size={14} className="text-slate-500" />
                    {s.studentCount}
                  </div>
                  <div className="flex items-center gap-1.5">
                    {s.alertCount > 0
                      ? <AlertTriangle size={14} className="text-amber-400" />
                      : <CheckCircle size={14} className="text-emerald-500" />
                    }
                    <span className="text-xs text-slate-400">{s.alertCount} alerts</span>
                  </div>
                  <EngagementBadge value={s.avgEngagement} />
                </div>
              </button>

              {/* Expanded student list */}
              {selected?.sessionId === s.sessionId && (
                <div className="mt-2 ml-4 glass rounded-2xl p-4">
                  <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-3">Students</p>
                  {loadingStudents ? (
                    <div className="space-y-2">
                      {[...Array(3)].map((_, i) => <Skeleton key={i} className="h-9" />)}
                    </div>
                  ) : students.length === 0 ? (
                    <p className="text-xs text-slate-500 text-center py-4">No attendance records for this session.</p>
                  ) : (
                    <div className="space-y-1.5">
                      {students.map(st => (
                        <div key={st.studentId} className="flex items-center justify-between py-2 px-3 rounded-xl bg-navy-900/50">
                          <p className="text-sm text-slate-200">{st.studentName}</p>
                          <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${
                            st.status === 'present' ? 'bg-emerald-500/15 text-emerald-400'
                            : st.status === 'late'  ? 'bg-amber-500/15 text-amber-400'
                            :                         'bg-rose-500/15 text-rose-400'
                          }`}>
                            {st.status}
                          </span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </LecturerLayout>
  )
}
