import React from 'react'
import { BarChart, Bar, Cell, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { Target } from 'lucide-react'

const DEFAULT_COLORS = ['#38BDF8', '#8B5CF6', '#10B981', '#F59E0B', '#EC4899']

const tooltipStyle = {
	contentStyle: { background: '#0F172A', border: '1px solid #334155', borderRadius: 12, fontSize: 12 },
	labelStyle: { color: '#CBD5E1' },
}

const toPct = (value) => {
	const numeric = Number(value)
	if (!Number.isFinite(numeric)) return null
	return numeric <= 1 ? Math.round(numeric * 100) : Math.round(numeric)
}

export default function CourseEngagementChart({ data = [], colors = DEFAULT_COLORS }) {
	const courses = Array.isArray(data) ? data : []
	const chartData = courses.map((entry, index) => ({
		name: entry.code ?? entry.course ?? entry.title ?? `Course ${index + 1}`,
		engagement: toPct(entry.avgEngagement ?? entry.engagement),
		attendance: toPct(entry.attendanceRate ?? entry.attendedRate),
		alerts: Number(entry.alertCount ?? 0),
	}))

	return (
		<div className="glass rounded-2xl p-5">
			<div className="flex items-center justify-between mb-4 gap-3">
				<h2 className="font-semibold text-slate-800 dark:text-white flex items-center gap-2">
					<Target size={16} className="text-[#667D9D]" /> Course Engagement
				</h2>
				<span className="text-xs text-slate-500">Latest tracked courses</span>
			</div>

			{chartData.length === 0 ? (
				<div className="flex items-center justify-center h-72 text-slate-600 text-sm">No course analytics yet</div>
			) : (
				<>
					<ResponsiveContainer width="100%" height={280}>
						<BarChart data={chartData} margin={{ top: 5, right: 10, left: -20, bottom: 5 }}>
							<CartesianGrid strokeDasharray="3 3" stroke="#1E293B" />
							<XAxis dataKey="name" tick={{ fontSize: 11, fill: '#64748B' }} interval={0} angle={-20} textAnchor="end" />
							<YAxis domain={[0, 100]} tick={{ fontSize: 11, fill: '#64748B' }} unit="%" />
							<Tooltip {...tooltipStyle} formatter={(value) => [`${value}%`, 'Engagement']} />
							<Bar dataKey="engagement" radius={[8, 8, 0, 0]}>
								{chartData.map((entry, index) => (
									<Cell key={entry.name} fill={colors[index % colors.length]} />
								))}
							</Bar>
						</BarChart>
					</ResponsiveContainer>

					<div className="space-y-3 mt-4">
						{chartData.slice(0, 6).map((entry, index) => (
							<div key={entry.name} className="rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-100 dark:bg-slate-950/50 p-3.5">
								<div className="flex items-start justify-between gap-4 mb-2">
									<div>
										<p className="text-sm font-medium text-slate-700 dark:text-slate-200">{entry.name}</p>
										<p className="text-xs text-slate-500 mt-0.5">Session intensity and attendance</p>
									</div>
									<div className="text-right">
										<p className="text-xs text-slate-500">Engagement</p>
										<p className="text-sm font-semibold text-slate-700 dark:text-slate-100">{entry.engagement ?? '—'}%</p>
									</div>
								</div>
								<div className="grid grid-cols-[1fr_auto] gap-3 items-center">
									<div className="h-1.5 bg-slate-300 dark:bg-slate-800 rounded-full overflow-hidden">
										<div className="h-full rounded-full bg-gradient-to-r from-[#667D9D] to-[#667D9D]" style={{ width: `${entry.engagement ?? 0}%` }} />
									</div>
									<span className="text-xs text-slate-500 w-14 text-right">{entry.attendance ?? '—'}%</span>
								</div>
								<div className="flex items-center justify-between mt-2 text-xs text-slate-500">
									<span>{entry.alerts ?? 0} alerts</span>
									<span>#{index + 1}</span>
								</div>
							</div>
						))}
					</div>
				</>
			)}
		</div>
	)
}
