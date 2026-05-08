import React from 'react'
import { BarChart3 } from 'lucide-react'

const formatDate = (value) => {
	if (!value) return '—'
	const date = new Date(value)
	if (Number.isNaN(date.getTime())) return String(value)
	return date.toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })
}

export default function WeeklyReportViewer({ reports = [] }) {
	const items = Array.isArray(reports) ? reports : []

	return (
		<div className="glass rounded-2xl p-5">
			<div className="flex items-center justify-between mb-4 gap-3">
				<h2 className="font-semibold text-slate-800 dark:text-white flex items-center gap-2">
					<BarChart3 size={16} className="text-[#667D9D]" /> Recent Reports
				</h2>
				<span className="text-xs text-slate-500">Latest generated summaries</span>
			</div>

			{items.length === 0 ? (
				<div className="flex items-center justify-center h-40 text-slate-600 text-sm">No recent reports available</div>
			) : (
				<div className="overflow-x-auto">
					<table className="w-full text-sm">
						<thead>
							<tr className="text-xs text-slate-500 border-b border-slate-200 dark:border-slate-800">
								<th className="text-left pb-2.5 font-medium">Report</th>
								<th className="text-left pb-2.5 font-medium">Owner</th>
								<th className="text-left pb-2.5 font-medium">Date</th>
								<th className="text-left pb-2.5 font-medium">Status</th>
							</tr>
						</thead>
						<tbody className="divide-y divide-slate-200 dark:divide-slate-800/60">
							{items.slice(0, 6).map((report, index) => {
								const status = String(report.status ?? report.state ?? 'ready').toLowerCase()
								const statusClass = status === 'ready' || status === 'completed'
									? 'bg-emerald-500/10 text-emerald-400'
									: status === 'processing'
										? 'bg-amber-500/10 text-amber-400'
										: 'bg-slate-200 dark:bg-slate-700/40 text-slate-500 dark:text-slate-400'

								return (
									<tr key={report.reportId ?? report.id ?? index} className="hover:bg-slate-100 dark:hover:bg-slate-800/20 transition-colors">
										<td className="py-3 pr-4 text-slate-700 dark:text-slate-200 font-medium">{report.title ?? report.name ?? `Report ${index + 1}`}</td>
										<td className="py-3 pr-4 text-slate-500 dark:text-slate-400 text-xs">{report.lecturerName ?? report.owner ?? '—'}</td>
										<td className="py-3 pr-4 text-slate-500 dark:text-slate-400 text-xs whitespace-nowrap">{formatDate(report.generatedAt ?? report.date ?? report.createdAt)}</td>
										<td className="py-3 pr-4">
											<span className={`text-xs px-2 py-0.5 rounded-full font-medium capitalize ${statusClass}`}>
												{report.status ?? report.state ?? 'ready'}
											</span>
										</td>
									</tr>
								)
							})}
						</tbody>
					</table>
				</div>
			)}
		</div>
	)
}
