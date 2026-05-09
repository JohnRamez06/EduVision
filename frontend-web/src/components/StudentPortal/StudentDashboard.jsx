import React, { useContext, useEffect, useState } from 'react'
import { AlertTriangle, BookOpen, Brain, Lightbulb } from 'lucide-react'
import studentService from '../../services/studentService'
import { AuthContext } from '../../context/AuthContext'
import MyCoursesList from './MyCoursesList'
import ConcentrationTimeline from './ConcentrationTimeline'
import EmotionBreakdown from './EmotionBreakdown'
import StudyRecommendations from './StudyRecommendations'
import SemesterTrend from './SemesterTrend'
import WeeklyReportViewer from './WeeklyReportViewer'
import ConsentManager from './ConsentManager'

export default function StudentDashboard() {
  const { user } = useContext(AuthContext)
  const [data, setData] = useState(null)
  const [consentStatus, setConsentStatus] = useState({ hasConsented: false })
  const [error, setError] = useState('')

  useEffect(() => {
    studentService.getDashboard()
      .then(setData)
      .catch((caughtError) => setError(caughtError.response?.data?.message ?? 'Failed to load student dashboard.'))
  }, [])

  useEffect(() => {
    if (!user?.email) return
    studentService.getConsentStatus(user.email)
      .then(setConsentStatus)
      .catch(() => {})
  }, [user])

  const emotionBreakdown = data?.overallStats?.emotionBreakdown ?? {}
  const trend = (data?.recentSummaries ?? []).map((summary, index) => ({
    label: summary.courseName ?? `S${index + 1}`,
    score: Math.round((summary.avgConcentration ?? 0) * 100),
  }))

  return (
    <div className="space-y-5">
      {error ? <div className="rounded-2xl border border-rose-500/30 bg-rose-500/10 p-4 text-sm text-rose-300"><AlertTriangle size={14} className="inline-block mr-2" />{error}</div> : null}

      <div className="grid gap-4 md:grid-cols-3">
        <Stat label="Average concentration" value={`${Math.round((data?.overallStats?.avgConcentration ?? 0) * 100)}%`} icon={Brain} />
        <Stat label="Attentiveness" value={`${Math.round((data?.overallStats?.avgAttentiveness ?? 0) * 100)}%`} icon={Lightbulb} />
        <Stat label="Courses" value={data?.enrolledCourses?.length ?? 0} icon={BookOpen} />
      </div>

      <div className="grid gap-5 xl:grid-cols-2">
        <MyCoursesList courses={data?.enrolledCourses ?? []} />
        <EmotionBreakdown breakdown={emotionBreakdown} />
      </div>

      <div className="grid gap-5 xl:grid-cols-2">
        <SemesterTrend data={trend} />
        <ConcentrationTimeline data={trend} />
      </div>

      <div className="grid gap-5 xl:grid-cols-2">
        <StudyRecommendations recommendations={data?.recommendations ?? []} />
        <ConsentManager status={consentStatus} />
      </div>

      <WeeklyReportViewer reports={data?.weeklyReports ?? []} />
    </div>
  )
}

function Stat({ label, value, icon: Icon }) {
  return (
    <div className="glass rounded-2xl p-4 flex items-center gap-3">
      <div className="w-10 h-10 rounded-xl bg-slate-800/70 flex items-center justify-center text-[#667D9D]">
        <Icon size={18} />
      </div>
      <div>
        <p className="text-xs text-slate-500">{label}</p>
        <p className="text-lg font-semibold text-white">{value}</p>
      </div>
    </div>
  )
}