import React from 'react'
import { BarChart, Bar, Cell, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { TrendingUp } from 'lucide-react'

const DEFAULT_COLORS = ['#3B82F6', '#10B981', '#8B5CF6', '#F59E0B', '#EC4899', '#06B6D4']

const tooltipStyle = {
	contentStyle: { background: '#0F172A', border: '1px solid #334155', borderRadius: 12, fontSize: 12 },
	labelStyle: { color: '#CBD5E1' },
}

const toPct = (value) => {
	const numeric = Number(value)
	if (!Number.isFinite(numeric)) return null
	return numeric <= 1 ? Math.round(numeric * 100) : Math.round(numeric)
}

export default function LecturerComparison({ data = [], colors = DEFAULT_COLORS }) {
	const lecturers = Array.isArray(data) ? data : []
	const chartData = lecturers
		.map((entry) => ({
			name: entry.fullName ?? entry.lecturer ?? entry.name ?? 'Lecturer',
			value: toPct(entry.avgEngagement ?? entry.engagement),
			sessions: Number(entry.sessions ?? entry.sessionCount ?? 0),
		}))
		.filter((entry) => Number.isFinite(entry.value))
		.sort((a, b) => (b.value ?? 0) - (a.value ?? 0))
		.slice(0, 6)

	return (
		<div className="glass rounded-2xl p-5">
			<div className="flex items-center justify-between mb-4 gap-3">
				<h2 className="font-semibold text-slate-800 dark:text-white flex items-center gap-2">
					<TrendingUp size={16} className="text-sky-400" /> Lecturer Comparison
				</h2>
				<span className="text-xs text-slate-500">Top engagement scores</span>
			</div>

			{chartData.length === 0 ? (
				<div className="flex items-center justify-center h-72 text-slate-600 text-sm">No lecturer comparison data yet</div>
			) : (
				<ResponsiveContainer width="100%" height={280}>
					<BarChart data={chartData} margin={{ top: 5, right: 10, left: -20, bottom: 5 }}>
						<CartesianGrid strokeDasharray="3 3" stroke="#1E293B" />
						<XAxis dataKey="name" tick={{ fontSize: 11, fill: '#64748B' }} interval={0} angle={-20} textAnchor="end" />
						<YAxis domain={[0, 100]} tick={{ fontSize: 11, fill: '#64748B' }} unit="%" />
						<Tooltip {...tooltipStyle} formatter={(value) => [`${value}%`, 'Engagement']} />
						<Bar dataKey="value" radius={[8, 8, 0, 0]}>
							{chartData.map((entry, index) => (
								<Cell key={entry.name} fill={colors[index % colors.length]} />
							))}
						</Bar>
					</BarChart>
				</ResponsiveContainer>
			)}
		</div>
	)
}
