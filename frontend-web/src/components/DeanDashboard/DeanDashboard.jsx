import React, { useContext, useEffect, useState } from 'react'
import { AlertTriangle, ExternalLink, Sparkles, Users, GraduationCap, BookOpen, Activity } from 'lucide-react'
import { AuthContext } from '../../context/AuthContext'
import { SHINY_BASE_URL } from '../../config/api'
import deanService from '../../services/deanService'
import ThemeToggle from '../common/ThemeToggle'
import DepartmentOverview from './DepartmentOverview'
import CourseEngagementChart from './CourseEngagementChart'
import LecturerComparison from './LecturerComparison'
import WeeklyReportViewer from './WeeklyReportViewer'
import IndividualStudentSearch from './IndividualStudentSearch'

const Skeleton = ({ className = '' }) => <div className={`animate-pulse rounded-xl bg-slate-800/60 ${className}`} />

const greet = (name) => {
	const hour = new Date().getHours()
	const prefix = hour < 12 ? 'Good morning' : hour < 17 ? 'Good afternoon' : 'Good evening'
	return `${prefix}, ${name?.split(' ')[0] ?? 'Dean'}`
}

const formatPct = (value) => {
	const numeric = Number(value)
	if (!Number.isFinite(numeric)) return '—'
	return `${numeric <= 1 ? Math.round(numeric * 100) : Math.round(numeric)}%`
}

const statTone = {
	blue: 'bg-blue-500/15 text-blue-400',
	emerald: 'bg-emerald-500/15 text-emerald-400',
	violet: 'bg-violet-500/15 text-violet-400',
	amber: 'bg-amber-500/15 text-amber-400',
}

const StatCard = ({ icon: Icon, label, value, sub, tone = 'blue' }) => (
	<div className="glass rounded-2xl p-5 flex items-center gap-4">
		<div className={`w-11 h-11 rounded-xl flex items-center justify-center shrink-0 ${statTone[tone]}`}>
			<Icon size={20} />
		</div>
		<div className="min-w-0">
			<p className="text-xs text-slate-500 font-medium mb-0.5">{label}</p>
			<p className="text-2xl font-bold text-slate-800 dark:text-white leading-none">{value ?? '—'}</p>
			{sub && <p className="text-xs text-slate-500 mt-0.5">{sub}</p>}
		</div>
	</div>
)

export default function DeanDashboard() {
	const { user } = useContext(AuthContext)
	const [dashboard, setDashboard] = useState(null)
	const [recentReports, setRecentReports] = useState([])
	const [loading, setLoading] = useState(true)
	const [error, setError] = useState('')
	const [studentId, setStudentId] = useState('')
	const [studentLookup, setStudentLookup] = useState(null)
	const [lookupLoading, setLookupLoading] = useState(false)
	const [lookupError, setLookupError] = useState('')

	useEffect(() => {
		Promise.all([
			deanService.getDashboard(),
			deanService.getRecentReports(),
		])
			.then(([dashboardData, reports]) => {
				setDashboard(dashboardData)
				setRecentReports(reports)
			})
			.catch((caughtError) => setError(caughtError.response?.data?.message ?? 'Failed to load dean dashboard.'))
			.finally(() => setLoading(false))
	}, [])

	const summary = dashboard?.summary ?? dashboard ?? {}
	const departmentOverview = Array.isArray(dashboard?.departmentOverview) ? dashboard.departmentOverview : []
	const lecturerComparison = Array.isArray(dashboard?.lecturerComparison) ? dashboard.lecturerComparison : []
	const courseEngagement = Array.isArray(dashboard?.courseEngagement) ? dashboard.courseEngagement : []
	const fallbackRecentReports = Array.isArray(dashboard?.recentReports) ? dashboard.recentReports : []
	const reportsData = recentReports.length > 0 ? recentReports : fallbackRecentReports

	const handleStudentSearch = async (event) => {
		event.preventDefault()
		const query = studentId.trim()
		if (!query) return

		setLookupLoading(true)
		setLookupError('')
		setStudentLookup(null)

		try {
			const result = await deanService.searchStudent(query)
			if (!result) {
				setLookupError('No matching student record found.')
				return
			}
			setStudentLookup(result)
		} catch (caughtError) {
			setLookupError(caughtError.response?.data?.message ?? 'Student lookup failed.')
		} finally {
			setLookupLoading(false)
		}
	}

	return (
		<div className="min-h-screen text-slate-100 relative overflow-hidden" style={{ backgroundColor: 'var(--bg-app)' }}>
			<div className="orb w-[520px] h-[520px] bg-sky-600/12 -top-36 -left-40" />
			<div className="orb w-[420px] h-[420px] bg-violet-600/10 bottom-0 right-0" />

			<div className="relative z-10 max-w-7xl mx-auto px-5 md:px-7 py-6 md:py-8 space-y-6">
				<div className="flex justify-end">
					<ThemeToggle />
				</div>
				<section className="glass-dark rounded-3xl p-6 md:p-8 overflow-hidden relative">
					<div className="absolute inset-0 bg-gradient-to-br from-sky-500/10 via-transparent to-violet-500/10 pointer-events-none" />
					<div className="relative grid gap-6 lg:grid-cols-[1.3fr_0.9fr] items-center">
						<div>
							<div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full text-xs font-semibold text-sky-300 bg-sky-500/10 border border-sky-500/20 mb-4">
								<Sparkles size={12} /> Faculty intelligence hub
							</div>
							<h1 className="text-3xl md:text-4xl font-black tracking-tight text-slate-800 dark:text-white mb-3">
								{loading ? 'Loading dean dashboard…' : greet(user?.fullName)}
							</h1>
							<p className="text-slate-400 max-w-2xl leading-relaxed">
								Department health, lecturer performance, and student risk signals in one view.
								This dashboard is wired to the backend facade and the Analytics-R service for deeper reporting.
							</p>

							<div className="flex flex-wrap gap-3 mt-5">
								<a href={SHINY_BASE_URL} target="_blank" rel="noreferrer" className="btn-primary inline-flex items-center gap-2">
									Open Analytics-R <ExternalLink size={15} />
								</a>
								<span className="inline-flex items-center gap-2 px-3.5 py-2 rounded-xl bg-slate-100 dark:bg-slate-900/70 border border-slate-200 dark:border-slate-800 text-xs text-slate-500 dark:text-slate-400">
									<AlertTriangle size={13} className="text-emerald-400" /> Connected to /facade/dean/dashboard
								</span>
							</div>
						</div>

						<div className="grid grid-cols-2 gap-3">
							<div className="rounded-2xl p-4 bg-slate-100 dark:bg-slate-950/60 border border-slate-200 dark:border-slate-800">
								<p className="text-xs text-slate-500 mb-1">Average Engagement</p>
								<p className="text-3xl font-black text-slate-800 dark:text-white">{formatPct(summary.avgEngagement)}</p>
								<p className="text-xs text-slate-500 mt-1">Faculty-wide learning attention</p>
							</div>
							<div className="rounded-2xl p-4 bg-slate-100 dark:bg-slate-950/60 border border-slate-200 dark:border-slate-800">
								<p className="text-xs text-slate-500 mb-1">Average Attendance</p>
								<p className="text-3xl font-black text-slate-800 dark:text-white">{formatPct(summary.avgAttendance)}</p>
								<p className="text-xs text-slate-500 mt-1">Across observed sessions</p>
							</div>
							<div className="rounded-2xl p-4 bg-slate-100 dark:bg-slate-950/60 border border-slate-200 dark:border-slate-800">
								<p className="text-xs text-slate-500 mb-1">Tracked Courses</p>
								<p className="text-3xl font-black text-slate-800 dark:text-white">{summary.totalCourses ?? courseEngagement.length ?? 0}</p>
								<p className="text-xs text-slate-500 mt-1">Course streams included</p>
							</div>
							<div className="rounded-2xl p-4 bg-slate-100 dark:bg-slate-950/60 border border-slate-200 dark:border-slate-800">
								<p className="text-xs text-slate-500 mb-1">Active Sessions</p>
								<p className="text-3xl font-black text-slate-800 dark:text-white">{summary.activeSessions ?? 0}</p>
								<p className="text-xs text-slate-500 mt-1">Currently live classes</p>
							</div>
						</div>
					</div>
				</section>

				{error && (
					<div className="flex items-center gap-2 px-4 py-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
						<AlertTriangle size={15} className="shrink-0" /> {error}
					</div>
				)}

				<div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
					{loading
						? [...Array(4)].map((_, index) => <Skeleton key={index} className="h-24" />)
						: [
							{ icon: Users, label: 'Students', value: summary.totalStudents ?? 0, sub: 'enrolled across departments', tone: 'blue' },
							{ icon: GraduationCap, label: 'Lecturers', value: summary.totalLecturers ?? 0, sub: 'active teaching staff', tone: 'emerald' },
							{ icon: BookOpen, label: 'Roles', value: summary.totalRoles ?? 0, sub: 'permission groups', tone: 'violet' },
							{ icon: Activity, label: 'Sessions', value: summary.totalSessions ?? 0, sub: 'tracked sessions', tone: 'amber' },
						].map((stat) => <StatCard key={stat.label} {...stat} />)}
				</div>

				<div className="grid grid-cols-1 xl:grid-cols-2 gap-5">
					<DepartmentOverview data={departmentOverview} />
					<LecturerComparison data={lecturerComparison} />
				</div>

				<div className="grid grid-cols-1 xl:grid-cols-2 gap-5">
					<CourseEngagementChart data={courseEngagement} />
					<WeeklyReportViewer reports={reportsData} />
				</div>

				<div className="grid grid-cols-1 xl:grid-cols-2 gap-5">
					<IndividualStudentSearch
						query={studentId}
						onQueryChange={setStudentId}
						onSubmit={handleStudentSearch}
						loading={lookupLoading}
						error={lookupError}
						result={studentLookup}
					/>
					<div className="glass rounded-2xl p-5">
						<h2 className="font-semibold text-slate-800 dark:text-white mb-4">Backend Connection</h2>
						<div className="space-y-3 text-sm text-slate-400 leading-relaxed">
							<p>This page loads dean overview data from /facade/dean/dashboard and recent reports from /facade/dean/reports/recent.</p>
							<p>If the recent reports facade is unavailable, the UI falls back to existing report sources without breaking the dashboard.</p>
						</div>
					</div>
				</div>
			</div>
		</div>
	)
}
