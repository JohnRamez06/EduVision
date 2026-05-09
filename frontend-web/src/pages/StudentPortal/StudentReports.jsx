import React, { useEffect, useState } from 'react'
import {
  FileText, Download, RefreshCw, Clock, CheckCircle,
  AlertTriangle, ChevronRight, BookOpen, Loader2,
} from 'lucide-react'

import StudentLayout from '../../layouts/StudentLayout'
import studentService from '../../services/studentService'

const Skeleton = ({ className = '' }) => (
  <div className={`animate-pulse rounded-xl bg-slate-800/60 ${className}`} />
)

const STATUS_STYLE = {
  ready:      { cls: 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20', label: 'Ready',      icon: CheckCircle },
  generating: { cls: 'text-amber-400 bg-amber-500/10 border-amber-500/20',       label: 'Generating', icon: RefreshCw   },
  failed:     { cls: 'text-rose-400 bg-rose-500/10 border-rose-500/20',           label: 'Failed',     icon: AlertTriangle },
}

function ReportCard({ report, onDownload }) {
  const s = STATUS_STYLE[report.status] ?? STATUS_STYLE.failed
  const Icon = s.icon
  const date = report.completedAt
    ? new Date(report.completedAt).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })
    : new Date(report.requestedAt).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })

  return (
    <div className="glass rounded-2xl p-5 flex items-center gap-4 hover:border-slate-700/60 transition-all group">
      <div className="w-11 h-11 rounded-xl bg-[#667D9D]/10 flex items-center justify-center shrink-0">
        <FileText size={18} className="text-[#667D9D]" />
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-sm font-semibold text-slate-200 truncate">{report.title}</p>
        <div className="flex items-center gap-2 mt-1 flex-wrap">
          <span className={`inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded-full border font-medium ${s.cls}`}>
            <Icon size={10} className={report.status === 'generating' ? 'animate-spin' : ''} />
            {s.label}
          </span>
          <span className="text-xs text-slate-600 flex items-center gap-1">
            <Clock size={10} /> {date}
          </span>
        </div>
      </div>
      {report.status === 'ready' && report.fileUrl && (
        <a
          href={report.fileUrl}
          download
          onClick={e => { e.preventDefault(); onDownload(report) }}
          className="shrink-0 flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-medium bg-[#667D9D]/15 text-[#ACBBC6] border border-[#667D9D]/20 hover:bg-[#667D9D]/25 transition-all"
        >
          <Download size={13} /> Download
        </a>
      )}
    </div>
  )
}

export default function StudentReports() {
  const [reports, setReports]         = useState([])
  const [sessions, setSessions]       = useState([])
  const [loading, setLoading]         = useState(true)
  const [generating, setGenerating]   = useState(null)
  const [error, setError]             = useState('')
  const [success, setSuccess]         = useState('')

  const loadReports = () => {
    setLoading(true)
    Promise.all([
      studentService.getMyReports().catch(() => []),
      studentService.getSummaries().catch(() => []),
    ])
      .then(([reps, summaries]) => {
        setReports(reps)
        setSessions(summaries)
      })
      .catch(e => setError(e.response?.data?.message ?? 'Failed to load reports.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { loadReports() }, [])

  const handleGenerate = async (sessionId, courseName) => {
    setGenerating(sessionId)
    setError('')
    setSuccess('')
    try {
      // Open the Java-generated HTML report directly — no R or pandoc needed
      studentService.openSessionReport(sessionId)
      setSuccess(`Report for "${courseName}" opened in a new tab.`)
    } catch (e) {
      setError(e.response?.data?.message ?? 'Failed to generate report.')
    } finally {
      setGenerating(null)
    }
  }

  const handleDownload = (report) => {
    // Try to extract session ID from fileUrl and open the Java HTML report
    const fileUrl = report.fileUrl ?? ''
    const sessionMatch = fileUrl.match(/session_([^.]+)/)
    if (sessionMatch) {
      studentService.openSessionReport(sessionMatch[1])
    } else {
      const fileName = fileUrl.split('/').pop()
      if (fileName) window.open(`http://localhost:8080/api/v1/reports/download/${fileName}`, '_blank')
    }
  }

  // Sessions that don't already have a report
  const generatedSessionIds = new Set(
    reports
      .filter(r => r.fileUrl?.includes('_session_'))
      .map(r => r.fileUrl?.split('_session_')[1]?.replace('.pdf', ''))
      .filter(Boolean)
  )
  const unreportedSessions = sessions.filter(s => !generatedSessionIds.has(s.sessionId))

  return (
    <StudentLayout>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-white flex items-center gap-2">
          <FileText size={22} className="text-[#667D9D]" /> My Reports
        </h1>
        <p className="text-sm text-slate-500 mt-1">
          Generate and download your personalised session analytics reports
        </p>
      </div>

      {error && (
        <div className="flex items-center gap-2 px-4 py-3 mb-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
          <AlertTriangle size={14} className="shrink-0" /> {error}
        </div>
      )}
      {success && (
        <div className="flex items-center gap-2 px-4 py-3 mb-4 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-sm">
          <CheckCircle size={14} className="shrink-0" /> {success}
        </div>
      )}

      {/* Generate new report */}
      {!loading && unreportedSessions.length > 0 && (
        <div className="glass rounded-2xl p-5 mb-6">
          <h2 className="font-semibold text-white flex items-center gap-2 mb-4">
            <BookOpen size={16} className="text-[#667D9D]" /> Generate a Session Report
          </h2>
          <p className="text-xs text-slate-500 mb-4">
            Select a session to generate your personalised PDF report with concentration, emotion,
            and attendance analytics.
          </p>
          <div className="space-y-2 max-h-64 overflow-y-auto pr-1">
            {unreportedSessions.map(s => (
              <div
                key={s.sessionId}
                className="flex items-center justify-between gap-3 px-4 py-3 rounded-xl bg-slate-800/50 hover:bg-slate-800/80 transition-colors"
              >
                <div className="min-w-0">
                  <p className="text-sm font-medium text-slate-200 truncate">{s.courseName}</p>
                  {s.date && (
                    <p className="text-xs text-slate-500 mt-0.5">
                      {new Date(s.date).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })}
                    </p>
                  )}
                </div>
                <button
                  onClick={() => handleGenerate(s.sessionId, s.courseName)}
                  disabled={generating === s.sessionId}
                  className="shrink-0 flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold bg-[#667D9D]/15 text-[#ACBBC6] border border-[#667D9D]/20 hover:bg-[#667D9D]/25 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {generating === s.sessionId
                    ? <><Loader2 size={12} className="animate-spin" /> Generating…</>
                    : <><ChevronRight size={12} /> Generate</>
                  }
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Existing reports */}
      <div className="glass rounded-2xl p-5">
        <div className="flex items-center justify-between mb-4">
          <h2 className="font-semibold text-white flex items-center gap-2">
            <FileText size={16} className="text-emerald-400" /> My Generated Reports
          </h2>
          <button
            onClick={loadReports}
            className="text-xs text-slate-500 hover:text-slate-300 flex items-center gap-1 transition-colors"
          >
            <RefreshCw size={12} /> Refresh
          </button>
        </div>

        {loading ? (
          <div className="space-y-3">
            {[...Array(3)].map((_, i) => <Skeleton key={i} className="h-20" />)}
          </div>
        ) : reports.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 text-slate-600">
            <FileText size={36} className="mb-3 opacity-30" />
            <p className="text-sm">No reports yet — generate one from a session above.</p>
          </div>
        ) : (
          <div className="space-y-3">
            {reports.map(r => (
              <ReportCard key={r.id} report={r} onDownload={handleDownload} />
            ))}
          </div>
        )}
      </div>
    </StudentLayout>
  )
}
