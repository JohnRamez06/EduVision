import React from 'react'
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts'
import { BarChart3 } from 'lucide-react'

const DEFAULT_COLORS = ['#3B82F6', '#10B981', '#8B5CF6', '#F59E0B', '#EC4899', '#06B6D4']

const tooltipStyle = {
	contentStyle: { background: '#0F172A', border: '1px solid #334155', borderRadius: 12, fontSize: 12 },
	labelStyle: { color: '#CBD5E1' },
}

export default function DepartmentOverview({ data = [], colors = DEFAULT_COLORS }) {
	const overview = Array.isArray(data) ? data : []
	const pieData = overview
		.map((entry, index) => ({
			name: entry.department ?? `Department ${index + 1}`,
			value: Number(entry.studentCount ?? entry.lecturerCount ?? entry.avgEngagement ?? 0),
			fill: colors[index % colors.length],
		}))
		.filter((entry) => Number.isFinite(entry.value) && entry.value > 0)

	return (
		<div className="glass rounded-2xl p-5">
			<div className="flex items-center justify-between mb-4 gap-3">
				<h2 className="font-semibold text-white flex items-center gap-2">
					<BarChart3 size={16} className="text-sky-400" /> Department Overview
				</h2>
				<span className="text-xs text-slate-500">Student distribution</span>
			</div>

			{overview.length === 0 ? (
				<div className="flex items-center justify-center h-72 text-slate-600 text-sm">No department data yet</div>
			) : (
				<div className="grid lg:grid-cols-[1fr_0.85fr] gap-5 items-center">
					<ResponsiveContainer width="100%" height={260}>
						<PieChart>
							<Pie data={pieData} dataKey="value" cx="50%" cy="50%" innerRadius={58} outerRadius={92} paddingAngle={2}>
								{pieData.map((entry) => <Cell key={entry.name} fill={entry.fill} />)}
							</Pie>
							<Tooltip {...tooltipStyle} />
						</PieChart>
					</ResponsiveContainer>

					<div className="space-y-3">
						{overview.map((entry, index) => {
							const color = colors[index % colors.length]
							const engagement = Number(entry.avgEngagement ?? 0)
							const pct = engagement <= 1 ? Math.round(engagement * 100) : Math.round(engagement)
							return (
								<div key={entry.department ?? index} className="rounded-xl border border-slate-800 bg-slate-950/50 p-3.5">
									<div className="flex items-center justify-between gap-3 mb-2">
										<p className="text-sm font-medium text-slate-200 truncate">{entry.department ?? 'General'}</p>
										<span className="text-xs text-slate-500">{pct > 0 ? `${pct}%` : '—'}</span>
									</div>
									<div className="h-1.5 bg-slate-800 rounded-full overflow-hidden">
										<div
											className="h-full rounded-full"
											style={{ width: `${pct}%`, background: color }}
										/>
									</div>
									<div className="flex items-center justify-between text-xs text-slate-500 mt-2">
										<span>{entry.studentCount ?? 0} students</span>
										<span>{entry.lecturerCount ?? 0} lecturers</span>
									</div>
								</div>
							)
						})}
					</div>
				</div>
			)}
		</div>
	)
}
