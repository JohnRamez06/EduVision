import React from 'react'
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'

export default function SemesterTrend({ data = [] }) {
  return (
    <div className="glass rounded-2xl p-5">
      <h3 className="font-semibold text-white mb-4">Semester Trend</h3>
      {data.length === 0 ? (
        <div className="h-56 flex items-center justify-center text-slate-600 text-sm">No trend data yet.</div>
      ) : (
        <ResponsiveContainer width="100%" height={240}>
          <AreaChart data={data} margin={{ top: 5, right: 10, left: -16, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
            <XAxis dataKey="label" tick={{ fontSize: 11, fill: '#64748b' }} />
            <YAxis domain={[0, 100]} tick={{ fontSize: 11, fill: '#64748b' }} unit="%" />
            <Tooltip contentStyle={{ background: '#0f172a', border: '1px solid #334155', borderRadius: 12 }} />
            <Area type="monotone" dataKey="value" stroke="#38bdf8" fill="rgba(56, 189, 248, 0.18)" />
          </AreaChart>
        </ResponsiveContainer>
      )}
    </div>
  )
}