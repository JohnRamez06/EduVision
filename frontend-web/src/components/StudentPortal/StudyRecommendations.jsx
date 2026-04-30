import React from 'react'
import { Lightbulb } from 'lucide-react'

export default function StudyRecommendations({ recommendations = [] }) {
  return (
    <div className="glass rounded-2xl p-5">
      <h3 className="font-semibold text-white mb-4 flex items-center gap-2"><Lightbulb size={16} className="text-amber-400" /> Study Recommendations</h3>
      <div className="space-y-3">
        {recommendations.length === 0 ? <div className="text-sm text-slate-600 py-8 text-center">No recommendations yet.</div> : recommendations.map((item, index) => (
          <div key={`${item.title}-${index}`} className="rounded-xl bg-navy-900/50 border border-slate-800/60 p-3">
            <p className="text-sm font-medium text-slate-200">{item.title}</p>
            <p className="text-xs text-slate-500 mt-1 leading-relaxed">{item.description}</p>
          </div>
        ))}
      </div>
    </div>
  )
}