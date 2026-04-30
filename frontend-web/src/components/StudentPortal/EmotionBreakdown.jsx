import React from 'react'
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts'

const COLORS = ['#38bdf8', '#34d399', '#f59e0b', '#fb7185', '#a78bfa', '#f97316']

export default function EmotionBreakdown({ breakdown = {} }) {
  const data = Object.entries(breakdown).map(([name, value], index) => ({ name, value: Math.round((value ?? 0) * 100), fill: COLORS[index % COLORS.length] }))

  return (
    <div className="glass rounded-2xl p-5">
      <h3 className="font-semibold text-white mb-4">Emotion Breakdown</h3>
      {data.length === 0 ? (
        <div className="h-56 flex items-center justify-center text-slate-600 text-sm">No emotion data yet.</div>
      ) : (
        <div className="space-y-4">
          <ResponsiveContainer width="100%" height={220}>
            <PieChart>
              <Pie data={data} dataKey="value" nameKey="name" cx="50%" cy="50%" innerRadius={50} outerRadius={80} paddingAngle={2}>
                {data.map((entry, index) => <Cell key={entry.name} fill={entry.fill ?? COLORS[index % COLORS.length]} />)}
              </Pie>
              <Tooltip contentStyle={{ background: '#0f172a', border: '1px solid #334155', borderRadius: 12 }} />
            </PieChart>
          </ResponsiveContainer>
          <div className="grid grid-cols-2 gap-2">
            {data.map((item) => (
              <div key={item.name} className="flex items-center gap-2 text-xs text-slate-400">
                <span className="w-2.5 h-2.5 rounded-full" style={{ background: item.fill }} />
                <span className="flex-1 capitalize">{item.name}</span>
                <span className="text-slate-300 font-medium">{item.value}%</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}