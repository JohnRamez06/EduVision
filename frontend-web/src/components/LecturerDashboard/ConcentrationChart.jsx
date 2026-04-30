import React from 'react'
import {
	LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
} from 'recharts'

const tooltipStyle = {
	contentStyle: { background: '#0f172a', border: '1px solid #334155', borderRadius: 12 },
	labelStyle: { color: '#94a3b8' },
}

export default function ConcentrationChart({ data = [] }) {
	const points = Array.isArray(data) ? data : []

	return (
		<div className="glass rounded-2xl p-5">
			<h3 className="font-semibold text-white mb-4">Concentration Trend</h3>
			{points.length === 0 ? (
				<div className="h-56 flex items-center justify-center text-slate-600 text-sm">No concentration history yet.</div>
			) : (
				<ResponsiveContainer width="100%" height={260}>
					<LineChart data={points} margin={{ top: 5, right: 10, left: -16, bottom: 0 }}>
						<CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
						<XAxis dataKey="label" tick={{ fontSize: 11, fill: '#64748b' }} />
						<YAxis domain={[0, 100]} tick={{ fontSize: 11, fill: '#64748b' }} unit="%" />
						<Tooltip {...tooltipStyle} />
						<Line type="monotone" dataKey="concentration" stroke="#38bdf8" strokeWidth={2.5} dot={{ r: 3 }} />
						<Line type="monotone" dataKey="engagement" stroke="#34d399" strokeWidth={2.5} dot={{ r: 3 }} strokeDasharray="4 3" />
					</LineChart>
				</ResponsiveContainer>
			)}
		</div>
	)
}
