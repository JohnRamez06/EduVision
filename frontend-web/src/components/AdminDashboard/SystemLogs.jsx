import React from 'react'
import { FileText } from 'lucide-react'

export default function SystemLogs({ logs = [] }) {
  return (
    <div className="glass rounded-2xl p-5">
      <h3 className="font-semibold text-white mb-4 flex items-center gap-2"><FileText size={16} className="text-[#667D9D]" /> System Logs</h3>
      <div className="space-y-2">
        {logs.length === 0 ? <div className="text-sm text-slate-600 py-8 text-center">No logs available.</div> : logs.map((log, index) => (
          <pre key={index} className="text-xs text-slate-300 rounded-xl bg-navy-900/50 border border-slate-800/60 p-3 overflow-auto">{typeof log === 'string' ? log : JSON.stringify(log, null, 2)}</pre>
        ))}
      </div>
    </div>
  )
}