import React from 'react'

const EMOTION_META = {
  happy: { label: 'Happy', emoji: '😊', color: 'text-emerald-400', bg: 'bg-emerald-500/15' },
  neutral: { label: 'Neutral', emoji: '😐', color: 'text-slate-300', bg: 'bg-slate-700/40' },
  confused: { label: 'Confused', emoji: '🤔', color: 'text-amber-400', bg: 'bg-amber-500/15' },
  sad: { label: 'Sad', emoji: '😔', color: 'text-blue-400', bg: 'bg-blue-500/15' },
  surprised: { label: 'Surprised', emoji: '😮', color: 'text-teal-400', bg: 'bg-teal-500/15' },
  angry: { label: 'Angry', emoji: '😠', color: 'text-rose-400', bg: 'bg-rose-500/15' },
  fearful: { label: 'Fearful', emoji: '😨', color: 'text-orange-400', bg: 'bg-orange-500/15' },
}

export default function LiveMoodGauge({ mood }) {
  const engagement = Math.max(0, Math.min(100, Math.round((mood?.engagementScore ?? 0) * 100)))
  const concentration = Math.max(0, Math.min(100, Math.round((mood?.concentration ?? mood?.avgConcentration ?? 0) * 100)))
  const dominantEmotion = String(mood?.dominantEmotion ?? mood?.dominant_emotion ?? 'neutral').toLowerCase()
  const studentCount = mood?.studentCount ?? mood?.student_count ?? mood?.totalFaces ?? 0
  const emoMeta = EMOTION_META[dominantEmotion] || EMOTION_META.neutral

  const gaugeColor = engagement >= 70 ? '#10b981' : engagement >= 45 ? '#f59e0b' : '#f43f5e'
  const r = 44
  const circ = 2 * Math.PI * r
  const dash = (engagement / 100) * circ

  return (
    <div className="glass rounded-2xl p-5">
      <div className="flex items-center justify-between mb-4">
        <div>
          <p className="text-xs text-slate-500 uppercase tracking-wide">Live Mood</p>
          <h3 className="text-lg font-semibold text-white">{emoMeta.label}</h3>
        </div>
        <div className="w-11 h-11 rounded-xl flex items-center justify-center text-2xl">
          {emoMeta.emoji}
        </div>
      </div>

      <div className="grid grid-cols-2 gap-3 mb-4">
        <div className="rounded-xl bg-slate-800/60 p-3">
          <div className="flex items-center gap-1.5 text-xs text-slate-500 mb-1">
            <span className="text-emerald-400">⚡</span>
            Engagement
          </div>
          <p className="text-xl font-bold text-emerald-400">{engagement}%</p>
        </div>
        <div className="rounded-xl bg-slate-800/60 p-3">
          <div className="flex items-center gap-1.5 text-xs text-slate-500 mb-1">
            <span className="text-blue-400">🧠</span>
            Concentration
          </div>
          <p className="text-xl font-bold text-blue-400">{concentration}%</p>
        </div>
      </div>

      <div className="flex justify-center mb-3">
        <div className="relative w-28 h-28">
          <svg viewBox="0 0 100 100" className="w-full h-full -rotate-90">
            <circle cx="50" cy="50" r={r} fill="none" stroke="#1e293b" strokeWidth="8" />
            <circle cx="50" cy="50" r={r} fill="none" stroke={gaugeColor} strokeWidth="8" strokeLinecap="round"
              strokeDasharray={`${dash} ${circ}`} style={{ transition: 'stroke-dasharray 0.6s ease' }} />
          </svg>
          <div className="absolute inset-0 flex flex-col items-center justify-center">
            <span className="text-2xl font-bold text-white">{engagement}%</span>
          </div>
        </div>
      </div>

      <div className="flex items-center justify-between text-xs text-slate-500">
        <span>👥 {studentCount} students</span>
        <span>Live</span>
      </div>
    </div>
  )
}