import React from 'react'
import { Clock, Users, AlertTriangle } from 'lucide-react'

export default function SessionHistoryList({ sessions = [], onSelect }) {
	return (
		<div className="glass rounded-2xl p-5">
			<h3 className="font-semibold text-white mb-4 flex items-center gap-2">
				<Clock size={16} className="text-teal-400" /> Session History
			</h3>
			<div className="space-y-3">
				{sessions.length === 0 ? (
					<div className="text-sm text-slate-600 py-8 text-center">No sessions available.</div>
				) : sessions.map((session) => (
					<button
						key={session.sessionId}
						type="button"
						onClick={() => onSelect?.(session)}
						className="w-full text-left rounded-xl bg-navy-900/50 border border-slate-800/60 p-3 hover:border-teal-500/30 transition-colors"
					>
						<div className="flex items-start justify-between gap-3">
							<div>
								<p className="text-sm font-medium text-slate-200">{session.courseName ?? 'Session'}</p>
								<p className="text-xs text-slate-500 mt-1">
									{session.date ? new Date(session.date).toLocaleString() : 'No date'}
								</p>
							</div>
							<div className="text-right text-xs text-slate-500 space-y-1">
								<div className="flex items-center gap-1 justify-end"><Users size={11} /> {session.studentCount ?? 0}</div>
								<div className="flex items-center gap-1 justify-end"><AlertTriangle size={11} /> {session.alertCount ?? 0}</div>
							</div>
						</div>
					</button>
				))}
			</div>
		</div>
	)
}
