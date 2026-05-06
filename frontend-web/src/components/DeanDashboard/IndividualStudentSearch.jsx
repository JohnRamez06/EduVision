import React from 'react'
import { Activity, ArrowUpRight, Search, ShieldAlert, AlertTriangle } from 'lucide-react'

const formatPct = (value) => {
	const numeric = Number(value)
	if (!Number.isFinite(numeric)) return '—'
	return `${numeric <= 1 ? Math.round(numeric * 100) : Math.round(numeric)}%`
}

export default function IndividualStudentSearch({
	query,
	onQueryChange,
	onSubmit,
	loading = false,
	error = '',
	result = null,
}) {
	const studentProfile = result?.studentInfo ?? result?.profile ?? null
	const studentStats = result?.overallStats ?? result?.summary ?? null

	return (
		<div className="glass rounded-2xl p-5">
			<div className="flex items-center justify-between mb-4 gap-3">
				<h2 className="font-semibold text-slate-800 dark:text-white flex items-center gap-2">
					<Search size={16} className="text-sky-400" /> Student Lookup
				</h2>
				<span className="text-xs text-slate-500">Backend-assisted search</span>
			</div>

			<form onSubmit={onSubmit} className="space-y-4">
				<div className="flex flex-col sm:flex-row gap-3">
					<input
						value={query}
						onChange={(event) => onQueryChange?.(event.target.value)}
						placeholder="Student ID or student number"
						className="input-field flex-1"
					/>
					<button type="submit" className="btn-primary inline-flex items-center justify-center gap-2 min-w-32">
						Search <ArrowUpRight size={15} />
					</button>
				</div>
				{error && (
					<div className="flex items-center gap-2 px-3 py-2 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs">
						<AlertTriangle size={13} className="shrink-0" /> {error}
					</div>
				)}
				{loading && (
					<div className="animate-pulse rounded-xl bg-slate-800/60 h-28" />
				)}
				{!loading && result && (
					<div className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-slate-100 dark:bg-slate-950/50 p-4">
						<div className="flex items-start justify-between gap-4 mb-4">
							<div>
								<p className="text-lg font-semibold text-slate-800 dark:text-white">{studentProfile?.fullName ?? studentProfile?.name ?? 'Student record'}</p>
								<p className="text-sm text-slate-500">{studentProfile?.program ?? studentProfile?.department ?? 'Program not specified'}</p>
							</div>
							<div className="flex items-center gap-2 text-xs text-emerald-400 bg-emerald-500/10 px-2.5 py-1 rounded-full">
								<Activity size={12} /> Found
							</div>
						</div>
						<div className="grid grid-cols-2 gap-3 text-sm">
							<div className="rounded-xl bg-slate-50 dark:bg-slate-900/60 border border-slate-200 dark:border-slate-800 p-3">
								<p className="text-xs text-slate-500 mb-1">Concentration</p>
								<p className="text-lg font-semibold text-slate-800 dark:text-white">{formatPct(studentStats?.avgConcentration)}</p>
							</div>
							<div className="rounded-xl bg-slate-50 dark:bg-slate-900/60 border border-slate-200 dark:border-slate-800 p-3">
								<p className="text-xs text-slate-500 mb-1">Attentiveness</p>
								<p className="text-lg font-semibold text-slate-800 dark:text-white">{formatPct(studentStats?.avgAttentiveness)}</p>
							</div>
							<div className="rounded-xl bg-slate-50 dark:bg-slate-900/60 border border-slate-200 dark:border-slate-800 p-3">
								<p className="text-xs text-slate-500 mb-1">Lectures</p>
								<p className="text-lg font-semibold text-slate-800 dark:text-white">{studentStats?.totalLecturesAttended ?? result?.recentSummaries?.length ?? '—'}</p>
							</div>
							<div className="rounded-xl bg-slate-50 dark:bg-slate-900/60 border border-slate-200 dark:border-slate-800 p-3">
								<p className="text-xs text-slate-500 mb-1">Top Emotion</p>
								<p className="text-lg font-semibold text-slate-800 dark:text-white capitalize">{studentStats?.mostFrequentEmotion ?? '—'}</p>
							</div>
						</div>
						<div className="mt-4 inline-flex items-center gap-2 text-xs text-slate-500">
							<ShieldAlert size={12} className="text-sky-400" /> Opened through the dean backend lookup path.
						</div>
					</div>
				)}
			</form>
		</div>
	)
}
