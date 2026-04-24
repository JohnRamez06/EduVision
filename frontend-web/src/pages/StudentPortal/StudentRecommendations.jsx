import React, { useEffect, useState } from 'react'
import { Lightbulb, AlertTriangle, Info, ChevronDown, ChevronUp, Filter } from 'lucide-react'
import StudentLayout from '../../layouts/StudentLayout'
import studentService from '../../services/studentService'

const Skeleton = ({ className = '' }) => (
  <div className={`animate-pulse rounded-xl bg-slate-800/60 ${className}`} />
)

const PRIORITY = {
  HIGH:   { label: 'High',   cls: 'bg-rose-500/15 text-rose-300 border-rose-500/25',   icon: AlertTriangle },
  MEDIUM: { label: 'Medium', cls: 'bg-amber-500/15 text-amber-300 border-amber-500/25', icon: Info },
  LOW:    { label: 'Low',    cls: 'bg-slate-700/40 text-slate-400 border-slate-700',     icon: Info },
}

function RecommendationCard({ rec }) {
  const [open, setOpen] = useState(false)
  const p = PRIORITY[rec.priority] ?? PRIORITY.LOW
  const Icon = p.icon

  return (
    <div className={`rounded-2xl border p-4 transition-all ${p.cls}`}>
      <button className="w-full flex items-start gap-3 text-left" onClick={() => setOpen(o => !o)}>
        <div className="mt-0.5 shrink-0">
          <Icon size={16} />
        </div>
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-1 flex-wrap">
            <span className="text-xs font-bold uppercase tracking-wide opacity-80">{p.label} Priority</span>
            {rec.type && (
              <span className="text-xs px-1.5 py-0.5 rounded-md bg-black/20 font-mono">{rec.type}</span>
            )}
          </div>
          <p className="font-semibold text-sm text-white leading-snug">{rec.title}</p>
        </div>
        <div className="shrink-0 mt-0.5">
          {open ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
        </div>
      </button>
      {open && (
        <p className="mt-3 text-sm leading-relaxed opacity-80 pl-7">{rec.description}</p>
      )}
    </div>
  )
}

export default function StudentRecommendations() {
  const [recs, setRecs]       = useState([])
  const [filter, setFilter]   = useState('ALL')
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState('')

  useEffect(() => {
    studentService.getRecommendations()
      .catch(() => studentService.getDashboard().then(d => d.recommendations ?? []))
      .then(setRecs)
      .catch(e => setError(e.response?.data?.message ?? 'Failed to load recommendations.'))
      .finally(() => setLoading(false))
  }, [])

  const visible = filter === 'ALL' ? recs : recs.filter(r => r.priority === filter)

  return (
    <StudentLayout>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-white flex items-center gap-2">
          <Lightbulb size={22} className="text-amber-400" /> AI Recommendations
        </h1>
        <p className="text-sm text-slate-500 mt-1">Personalised insights based on your learning patterns</p>
      </div>

      {error && (
        <div className="flex items-center gap-2 px-4 py-3 mb-6 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
          <AlertTriangle size={15} className="shrink-0" /> {error}
        </div>
      )}

      {/* Filter pills */}
      {!loading && recs.length > 0 && (
        <div className="flex items-center gap-2 mb-5 flex-wrap">
          <Filter size={13} className="text-slate-500" />
          {['ALL', 'HIGH', 'MEDIUM', 'LOW'].map(f => (
            <button
              key={f}
              onClick={() => setFilter(f)}
              className={`px-3 py-1 rounded-full text-xs font-medium transition-all ${
                filter === f
                  ? 'bg-blue-500/20 text-blue-300 border border-blue-500/30'
                  : 'text-slate-500 border border-slate-800 hover:text-slate-300'
              }`}
            >
              {f === 'ALL' ? `All (${recs.length})` : `${f.charAt(0) + f.slice(1).toLowerCase()} (${recs.filter(r => r.priority === f).length})`}
            </button>
          ))}
        </div>
      )}

      {loading ? (
        <div className="space-y-4">
          {[...Array(4)].map((_, i) => <Skeleton key={i} className="h-20" />)}
        </div>
      ) : visible.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-24 text-slate-600">
          <Lightbulb size={40} className="mb-3 opacity-30" />
          <p className="text-sm">
            {recs.length === 0 ? 'No recommendations yet — attend a lecture to get started.' : 'No recommendations for this priority.'}
          </p>
        </div>
      ) : (
        <div className="space-y-3">
          {visible.map((r, i) => <RecommendationCard key={i} rec={r} />)}
        </div>
      )}
    </StudentLayout>
  )
}
