import React, { useEffect, useState } from "react";
import { BookOpen, Clock, Users } from "lucide-react";
import lecturerService from "../../services/lecturerService";
import useWebSocket from "../../hooks/useWebSocket";
import LiveMoodGauge from "./LiveMoodGauge";
import ConcentrationChart from "./ConcentrationChart";
import StudentRiskList from "./StudentRiskList";
import SessionHistoryList from "./SessionHistoryList";
import DetectedStudentsList from "./DetectedStudentsList";

export default function LecturerDashboard() {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    lecturerService
      .getDashboard()
      .then(setData)
      .catch((caughtError) =>
        setError(
          caughtError.response?.data?.message ??
            "Failed to load lecturer dashboard.",
        ),
      );
  }, []);

  const activeSessionId = data?.activeSessionId ?? null;
  const { mood } = useWebSocket(activeSessionId);

  const activeCourse =
    (data?.courses ?? []).find((c) => c.activeSessions > 0) ??
    data?.courses?.[0] ??
    null;

  const chartData = [];

  return (
    <div className="grid gap-5 lg:grid-cols-3">
      <div className="lg:col-span-2 space-y-5">
        {error ? (
          <div className="rounded-2xl border border-rose-500/30 bg-rose-500/10 p-4 text-sm text-rose-300">
            {error}
          </div>
        ) : null}
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          <Stat
            label="Course"
            value={activeCourse?.title ?? "None"}
            icon={BookOpen}
          />
          <Stat
            label="Active Sessions"
            value={data?.activeSessions ?? 0}
            icon={Users}
          />
          <Stat
            label="Total Sessions"
            value={data?.totalSessions ?? 0}
            icon={Clock}
          />
        </div>
        <ConcentrationChart data={chartData} />
        <StudentRiskList students={[]} />
      </div>

      <div className="space-y-5">
        <LiveMoodGauge mood={mood} />
        <DetectedStudentsList sessionId={activeSessionId} />
        <SessionHistoryList sessions={data?.recentSessions ?? []} />
      </div>
    </div>
  );
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
  );
}
