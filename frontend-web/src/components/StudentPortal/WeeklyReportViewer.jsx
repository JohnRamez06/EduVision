import React from 'react'
import { Download } from 'lucide-react'
import ReportDownloadButton from '../common/ReportDownloadButton'

export default function WeeklyReportViewer({ reports = [] }) {
  return (
    <div className="glass rounded-2xl p-5">
      <h3 className="font-semibold text-white mb-4 flex items-center gap-2"><Download size={16} className="text-[#667D9D]" /> Weekly Reports</h3>
      <div className="space-y-3">
        {reports.length === 0 ? <div className="text-sm text-slate-600 py-8 text-center">No weekly reports yet.</div> : reports.map((report) => (
          <div key={report.reportId ?? report.id} className="rounded-xl bg-navy-900/50 border border-slate-800/60 p-3 flex items-center justify-between gap-3">
            <div>
              <p className="text-sm font-medium text-slate-200">{report.title ?? 'Weekly report'}</p>
              <p className="text-xs text-slate-500 mt-1">{report.type ?? 'report'}</p>
            </div>
            <ReportDownloadButton reportId={report.reportId ?? report.id} label="Download" />
          </div>
        ))}
      </div>
    </div>
  )
}