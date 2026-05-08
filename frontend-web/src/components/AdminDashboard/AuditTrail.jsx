import React from 'react'
import { ShieldAlert } from 'lucide-react'

export default function AuditTrail({ items = [] }) {
  return (
    <div className="glass rounded-2xl p-5">
      <h3 className="font-semibold text-white mb-4 flex items-center gap-2"><ShieldAlert size={16} className="text-[#667D9D]" /> Audit Trail</h3>
      <div className="space-y-2">
        {items.length === 0 ? <div className="text-sm text-slate-600 py-8 text-center">No audit entries available.</div> : items.map((item, index) => (
          <div key={item.id ?? index} className="rounded-xl bg-navy-900/50 border border-slate-800/60 p-3">
            <p className="text-sm font-medium text-slate-200">{item.action ?? 'Action'}</p>
            <p className="text-xs text-slate-500 mt-1">{item.details ?? JSON.stringify(item)}</p>
          </div>
        ))}
      </div>
    </div>
  )
}