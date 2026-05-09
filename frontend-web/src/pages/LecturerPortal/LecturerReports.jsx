import React, { useContext, useEffect, useState } from 'react'
import {
  FileText, Download, Loader2, AlertTriangle,
  CheckCircle, Clock, Users, ChevronDown, RefreshCw,
} from 'lucide-react'

import LecturerLayout from '../../layouts/LecturerLayout'
import { AuthContext } from '../../context/AuthContext'
import reportService from '../../services/reportService'
import lecturerService from '../../services/lecturerService'

// Academic weeks: semester started Saturday 14 Feb 2026, each week is Sat–Fri.
const SEMESTER_START = new Date(2026, 1, 14) // Feb 14 2026
const currentWeek = () => {
  const diffDays = Math.floor((Date.now() - SEMESTER_START.getTime()) / 86400000)
  if (diffDays < 0) return 1
  return Math.min(Math.floor(diffDays / 7) + 1, 52)
}

const Skeleton = ({ className = '' }) => (
  <div className={`animate-pulse rounded-xl bg-slate-800/60 ${className}`} />
)

// ── Reusable generate+download button ──────────────────────────────────────
function ReportButton({ onGenerate, label = 'Generate Report', disabled = false }) {
  const [state, setState] = useState('idle') // idle | loading | ready | error
  const [fileUrl, setFileUrl] = useState('')
  const [err, setErr] = useState('')

  const toError = (e) => e.response?.data?.message || e.message || 'Failed to generate report'

  const showError = (msg) => {
    setErr(msg)
    setState('error')
    setTimeout(() => setState('idle'), 5000)
  }

  const generate = async () => {
    setState('loading')
    setErr('')
    try {
      const result = await onGenerate()
      // Java HTML reports: onGenerate opens the tab and returns undefined/null
      if (result === undefined || result === null) {
        setState('idle')  // tab already opened, reset button
        return
      }
      if (result?.status === 'failed') {
        showError('Report generation failed — check that data exists for this week.')
        return
      }
      const url = result?.fileUrl || result?.file_url || ''
      if (!url) {
        showError('Report finished but no file was produced.')
        return
      }
      setFileUrl(url)
      setState('ready')
    } catch (e) {
      showError(toError(e))
    }
  }

  const openReport = () => {
    if (!fileUrl) return
    const fileName = fileUrl.split('/').pop()
    window.open(`http://localhost:8080/api/v1/reports/download/${fileName}`, '_blank')
  }

  if (state === 'idle')
    return (
      <button onClick={generate} disabled={disabled}
        className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-white text-xs font-medium transition-colors ${
          disabled
            ? 'bg-slate-700/80 text-slate-500 cursor-not-allowed'
            : 'btn-primary cursor-pointer'
        }`}>
        {disabled ? <Loader2 size={12} className="animate-spin" /> : <FileText size={12} />}
        {disabled ? 'Loading…' : label}
      </button>
    )

  if (state === 'loading')
    return (
      <button disabled
        className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-700 text-slate-400 text-xs cursor-not-allowed">
        <Loader2 size={12} className="animate-spin" /> Generating…
      </button>
    )

  if (state === 'ready')
    return (
      <div className="flex items-center gap-2">
        <span className="flex items-center gap-1 text-xs text-emerald-400">
          <CheckCircle size={12} /> Ready
        </span>
        <button onClick={openReport}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-medium transition-colors">
          <Download size={12} /> Open Report
        </button>
        <button onClick={() => setState('idle')}
          className="p-1.5 rounded-lg text-slate-500 hover:text-slate-300 hover:bg-slate-800 transition-colors">
          <RefreshCw size={11} />
        </button>
      </div>
    )

  return <span className="text-xs text-rose-400">{err}</span>
}

// ── Weekly Reports tab ──────────────────────────────────────────────────────
function WeeklyTab({ lecturerId }) {
  const [week, setWeek] = useState(currentWeek())

  const weeks = Array.from({ length: 52 }, (_, i) => i + 1)

  return (
    <div className="glass rounded-2xl p-6">
      <h2 className="font-semibold text-slate-800 dark:text-white mb-1">Weekly Performance Report</h2>
      <p className="text-xs text-slate-500 mb-5">
        Generate a PDF summary of your engagement, emotion trends, and alerts for a specific week.
      </p>

      <div className="flex items-center gap-3 mb-6">
        <div className="relative">
          <select
            value={week}
            onChange={e => setWeek(Number(e.target.value))}
            className="input-field appearance-none pl-3 pr-8 py-2 text-sm cursor-pointer"
          >
            {weeks.map(w => (
              <option key={w} value={w}>Week {w}</option>
            ))}
          </select>
          <ChevronDown size={13} className="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none" />
        </div>

        <ReportButton
          label="Generate Weekly Report"
          disabled={!lecturerId}
          onGenerate={() => reportService.generateLecturer(lecturerId, week)}
        />
      </div>

      <div className="rounded-xl bg-slate-100 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700 p-4">
        <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">What's included</p>
        <ul className="space-y-1.5 text-xs text-slate-400">
          {[
            'Session-by-session engagement scores',
            'Emotion distribution across all sessions',
            'Student at-risk count and alert breakdown',
            'Course comparison for the selected week',
          ].map(item => (
            <li key={item} className="flex items-center gap-2">
              <span className="w-1 h-1 rounded-full bg-[#667D9D] shrink-0" />
              {item}
            </li>
          ))}
        </ul>
      </div>
    </div>
  )
}

// ── Session Reports tab ─────────────────────────────────────────────────────
function SessionsTab() {
  const [sessions, setSessions] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    lecturerService.getDashboard()
      .then(d => setSessions(d.recentSessions ?? []))
      .catch(() => setError('Failed to load sessions.'))
      .finally(() => setLoading(false))
  }, [])

  if (loading)
    return (
      <div className="space-y-3">
        {[...Array(4)].map((_, i) => <Skeleton key={i} className="h-16" />)}
      </div>
    )

  if (error)
    return (
      <div className="flex items-center gap-2 px-4 py-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
        <AlertTriangle size={15} /> {error}
      </div>
    )

  if (sessions.length === 0)
    return (
      <div className="flex flex-col items-center justify-center py-20 text-slate-600">
        <Clock size={36} className="mb-3 opacity-30" />
        <p className="text-sm">No completed sessions yet.</p>
      </div>
    )

  return (
    <div className="space-y-2">
      {sessions.map(s => (
        <div key={s.sessionId}
          className="glass rounded-xl px-5 py-4 flex items-center gap-4">
          {/* Date */}
          <div className="w-12 text-center shrink-0">
            <p className="text-base font-bold text-slate-800 dark:text-white leading-none">
              {s.date ? new Date(s.date).getDate() : '—'}
            </p>
            <p className="text-[10px] text-slate-500 uppercase">
              {s.date ? new Date(s.date).toLocaleDateString('en-GB', { month: 'short' }) : ''}
            </p>
          </div>

          <div className="w-px h-8 bg-slate-800 shrink-0" />

          {/* Info */}
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-slate-700 dark:text-slate-200 truncate">{s.courseName ?? '—'}</p>
            <div className="flex items-center gap-3 mt-0.5">
              <span className="flex items-center gap-1 text-xs text-slate-500">
                <Users size={10} /> {s.studentCount} students
              </span>
              {s.alertCount > 0 && (
                <span className="flex items-center gap-1 text-xs text-amber-400">
                  <AlertTriangle size={10} /> {s.alertCount} alerts
                </span>
              )}
            </div>
          </div>

          {/* Action — opens Java HTML report, no R needed */}
          <ReportButton
            label="Session Report"
            onGenerate={() => reportService.openSessionReport(s.sessionId)}
          />
        </div>
      ))}
    </div>
  )
}

// ── History tab ─────────────────────────────────────────────────────────────
const TYPE_LABEL = {
  weekly_lecturer: 'Weekly',
  session_summary: 'Session',
  weekly_student:  'Student Weekly',
  weekly_dean:     'Dean Weekly',
}

function HistoryTab() {
  const [reports, setReports] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState('')

  const load = () => {
    setLoading(true)
    reportService.getMyReports()
      .then(setReports)
      .catch(() => setError('Failed to load report history.'))
      .finally(() => setLoading(false))
  }

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => { load() }, [])

  const download = (fileUrl) => {
    if (!fileUrl) return
    const fileName = fileUrl.split('/').pop()
    window.open(`http://localhost:8080/api/v1/reports/download/${fileName}`, '_blank')
  }

  if (loading) return <div className="space-y-3">{[...Array(4)].map((_, i) => <Skeleton key={i} className="h-14" />)}</div>
  if (error)   return <div className="flex items-center gap-2 px-4 py-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm"><AlertTriangle size={15}/>{error}</div>
  if (reports.length === 0)
    return (
      <div className="flex flex-col items-center justify-center py-20 text-slate-600">
        <FileText size={36} className="mb-3 opacity-30" />
        <p className="text-sm">No reports generated yet.</p>
      </div>
    )

  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between mb-3">
        <p className="text-xs text-slate-500">{reports.length} report{reports.length !== 1 ? 's' : ''}</p>
        <button onClick={load} className="flex items-center gap-1.5 text-xs text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 transition-colors">
          <RefreshCw size={12}/> Refresh
        </button>
      </div>

      {reports.map(r => (
        <div key={r.id} className="glass rounded-xl px-5 py-3.5 flex items-center gap-4">
          {/* Status dot */}
          <div className={`w-2 h-2 rounded-full shrink-0 ${
            r.status === 'ready'      ? 'bg-emerald-400' :
            r.status === 'failed'     ? 'bg-rose-400' :
            r.status === 'generating' ? 'bg-amber-400 animate-pulse' :
                                        'bg-slate-500'
          }`} />

          {/* Info */}
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-slate-700 dark:text-slate-200 truncate">{r.title}</p>
            <div className="flex items-center gap-3 mt-0.5">
              <span className="text-xs text-slate-500">{TYPE_LABEL[r.type] ?? r.type}</span>
              <span className="text-xs text-slate-600">
                {r.requestedAt ? new Date(r.requestedAt).toLocaleDateString('en-GB', { day:'numeric', month:'short', year:'numeric' }) : ''}
              </span>
            </div>
          </div>

          {/* Action */}
          {r.status === 'ready' && r.fileUrl ? (
            <button onClick={() => download(r.fileUrl)}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-medium transition-colors cursor-pointer">
              <Download size={12}/> Download
            </button>
          ) : r.status === 'failed' ? (
            <span className="inline-flex items-center gap-1 text-xs font-semibold text-rose-400 bg-rose-500/10 border border-rose-500/20 px-2.5 py-1 rounded-full">Failed</span>
          ) : (
            <span className="inline-flex items-center gap-1.5 text-xs font-semibold text-amber-400 bg-amber-500/10 border border-amber-500/20 px-2.5 py-1 rounded-full">
              <Loader2 size={10} className="animate-spin" /> Generating
            </span>
          )}
        </div>
      ))}
    </div>
  )
}

// ── Page ────────────────────────────────────────────────────────────────────
const TABS = ['Weekly', 'Sessions', 'History']

export default function LecturerReports() {
  const { user } = useContext(AuthContext)
  const [tab, setTab] = useState('Weekly')
  // userId may be missing from sessions cached before the backend started
  // returning it in the login response — fall back to a profile fetch.
  const [lecturerId, setLecturerId] = useState(user?.userId ?? null)

  useEffect(() => {
    if (lecturerId) return
    lecturerService.getProfile()
      .then(profile => {
        if (profile?.id) {
          setLecturerId(profile.id)
          // Patch userId into stored user object so refreshes resolve instantly
          try {
            const stored = JSON.parse(localStorage.getItem('eduvision.user') || '{}')
            if (!stored.userId) {
              stored.userId = profile.id
              localStorage.setItem('eduvision.user', JSON.stringify(stored))
            }
          } catch (_) {}
        }
      })
      .catch(() => {}) // non-fatal
  }, [lecturerId])

  return (
    <LecturerLayout>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-slate-800 dark:text-white flex items-center gap-2">
          <FileText size={22} className="text-[#667D9D]" /> Reports
        </h1>
        <p className="text-sm text-slate-500 mt-1">Generate and download PDF analytics reports</p>
      </div>

      {/* Tab bar */}
      <div className="flex gap-1 p-1 rounded-xl bg-slate-100 dark:bg-slate-800/70 border border-slate-200 dark:border-slate-700/80 w-fit mb-6">
        {TABS.map(t => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-all duration-150 cursor-pointer ${
              tab === t
                ? 'bg-[#16254F] text-white force-white shadow-sm shadow-[#667D9D]/30 border border-[#667D9D]/30'
                : 'text-slate-600 dark:text-slate-400 hover:text-slate-800 dark:hover:text-slate-200 hover:bg-slate-200 dark:hover:bg-slate-800/50'
            } focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#667D9D]/60`}
          >
            {t}
          </button>
        ))}
      </div>

      {tab === 'Weekly'   && <WeeklyTab lecturerId={lecturerId} />}
      {tab === 'Sessions' && <SessionsTab />}
      {tab === 'History'  && <HistoryTab />}
    </LecturerLayout>
  )
}
