import React, { useEffect, useState } from 'react'
import { BookOpen, Clock, Users, AlertTriangle } from 'lucide-react'
import lecturerService from '../../services/lecturerService'
import LiveMoodGauge from './LiveMoodGauge'
import ConcentrationChart from './ConcentrationChart'
import StudentRiskList from './StudentRiskList'
import SessionHistoryList from './SessionHistoryList'

export default function LecturerDashboard() {
	const [data, setData] = useState(null)
	const [error, setError] = useState('')

	useEffect(() => {
		lecturerService.getDashboard()
			.then(setData)
			.catch((caughtError) => setError(caughtError.response?.data?.message ?? 'Failed to load lecturer dashboard.'))
	}, [])

	const sessionInfo = data?.sessionInfo ?? {}
	const mood = {
		dominantEmotion: data?.currentMood,
		engagementScore: data?.concentrationTrend?.[data.concentrationTrend.length - 1] ?? 0,
		concentration: data?.concentrationTrend?.[data.concentrationTrend.length - 1] ?? 0,
		studentCount: sessionInfo.studentCount ?? 0,
		timestamp: new Date().toISOString(),
	}

	const chartData = (data?.concentrationTrend ?? []).map((value, index) => ({
		label: `T${index + 1}`,
		concentration: Math.round((value ?? 0) * 100),
		engagement: Math.round((value ?? 0) * 100),
	}))

	return (
		<div className="grid gap-5 lg:grid-cols-3">
			<div className="lg:col-span-2 space-y-5">
				{error ? <div className="rounded-2xl border border-rose-500/30 bg-rose-500/10 p-4 text-sm text-rose-300">{error}</div> : null}
				<div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
					<Stat label="Course" value={sessionInfo.courseName ?? 'None'} icon={BookOpen} />
					<Stat label="Students" value={sessionInfo.studentCount ?? 0} icon={Users} />
					<Stat label="Active Time" value={sessionInfo.activeTime ?? 0} icon={Clock} />
				</div>
				<ConcentrationChart data={chartData} />
				<StudentRiskList students={data?.atRiskStudents ?? []} />
			</div>

			<div className="space-y-5">
				<LiveMoodGauge mood={mood} />
				<SessionHistoryList sessions={data?.recentSessions ?? []} />
				<div className="glass rounded-2xl p-5">
					<p className="text-sm font-semibold text-white mb-2">Recent Alerts</p>
					<div className="space-y-2 text-sm text-slate-400">
						{(data?.recentAlerts ?? []).length === 0 ? <p>No alerts yet.</p> : (data?.recentAlerts ?? []).map((alert, index) => <p key={index}>{alert}</p>)}
					</div>
				</div>
			</div>
		</div>
	)
}

function Stat({ label, value, icon: Icon }) {
	return (
		<div className="glass rounded-2xl p-4 flex items-center gap-3">
			<div className="w-10 h-10 rounded-xl bg-slate-800/70 flex items-center justify-center text-violet-400">
				<Icon size={18} />
			</div>
			<div>
				<p className="text-xs text-slate-500">{label}</p>
				<p className="text-lg font-semibold text-white">{value}</p>
			</div>
		</div>
	)
}
