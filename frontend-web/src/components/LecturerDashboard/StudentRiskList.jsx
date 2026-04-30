import React from 'react'
import { AlertTriangle } from 'lucide-react'

export default function StudentRiskList({ students = [] }) {
	return (
		<div className="glass rounded-2xl p-5">
			<h3 className="font-semibold text-white mb-4 flex items-center gap-2">
				<AlertTriangle size={16} className="text-amber-400" /> Students at Risk
			</h3>
			<div className="space-y-2">
				{students.length === 0 ? (
					<div className="text-sm text-slate-600 py-8 text-center">No risk alerts yet.</div>
				) : students.map((student) => (
					<div key={student.studentId} className="rounded-xl bg-navy-900/50 border border-slate-800/60 p-3">
						<div className="flex items-center justify-between gap-3">
							<div>
								<p className="text-sm font-medium text-slate-200">{student.studentName ?? student.studentId}</p>
								<p className="text-xs text-slate-500 mt-1">Score {Math.round((student.concentrationScore ?? 0) * 100)}%</p>
							</div>
							<span className="text-xs px-2 py-1 rounded-full bg-amber-500/10 text-amber-300">
								{student.riskLevel ?? 'risk'}
							</span>
						</div>
					</div>
				))}
			</div>
		</div>
	)
}
