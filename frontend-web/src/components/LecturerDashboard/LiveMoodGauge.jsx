import React from "react";
import { Zap, Brain, Users } from "lucide-react";

const EMOTION_META = {
  happy: {
    label: "Happy",
    dot: "bg-emerald-400",
    color: "text-emerald-400",
    bg: "bg-emerald-500/15",
  },
  neutral: {
    label: "Neutral",
    dot: "bg-slate-400",
    color: "text-slate-300",
    bg: "bg-slate-700/40",
  },
  confused: {
    label: "Confused",
    dot: "bg-amber-400",
    color: "text-amber-400",
    bg: "bg-amber-500/15",
  },
  sad: {
    label: "Sad",
    dot: "bg-blue-400",
    color: "text-blue-400",
    bg: "bg-blue-500/15",
  },
  surprised: {
    label: "Surprised",
    dot: "bg-teal-400",
    color: "text-teal-400",
    bg: "bg-teal-500/15",
  },
  angry: {
    label: "Angry",
    dot: "bg-rose-400",
    color: "text-rose-400",
    bg: "bg-rose-500/15",
  },
  fearful: {
    label: "Fearful",
    dot: "bg-orange-400",
    color: "text-orange-400",
    bg: "bg-orange-500/15",
  },
  disgusted: {
    label: "Disgusted",
    dot: "bg-purple-400",
    color: "text-purple-400",
    bg: "bg-purple-500/15",
  },
  engaged: {
    label: "Engaged",
    dot: "bg-cyan-400",
    color: "text-cyan-400",
    bg: "bg-cyan-500/15",
  },
};

export default function LiveMoodGauge({ mood }) {
  const engagement = Math.max(
    0,
    Math.min(100, Math.round((mood?.engagementScore ?? 0) * 100)),
  );
  const concentration = Math.max(
    0,
    Math.min(
      100,
      Math.round((mood?.concentration ?? mood?.avgConcentration ?? 0) * 100),
    ),
  );
  const dominantEmotion = String(
    mood?.dominantEmotion ?? mood?.dominant_emotion ?? "neutral",
  ).toLowerCase();
  const studentCount =
    mood?.studentCount ?? mood?.student_count ?? mood?.totalFaces ?? 0;
  const emoMeta = EMOTION_META[dominantEmotion] || EMOTION_META.neutral;

  const gaugeColor =
    engagement >= 70 ? "#10b981" : engagement >= 45 ? "#f59e0b" : "#f43f5e";
  const r = 44;
  const circ = 2 * Math.PI * r;
  const dash = (engagement / 100) * circ;

  return (
    <div className="glass rounded-2xl p-5">
      <div className="flex items-center justify-between mb-4">
        <div>
          <p className="text-xs text-slate-500 uppercase tracking-wide">
            Live Mood
          </p>
          <h3 className="text-lg font-semibold text-white">{emoMeta.label}</h3>
        </div>
        <div
          className={`w-11 h-11 rounded-xl flex items-center justify-center shrink-0 ${emoMeta.bg}`}
        >
          <span className={`w-4 h-4 rounded-full ${emoMeta.dot}`} />
        </div>
      </div>

      <div className="grid grid-cols-2 gap-3 mb-4">
        <div className="rounded-xl bg-slate-800/60 p-3">
          <div className="flex items-center gap-1.5 text-xs text-slate-500 mb-1">
            <Zap size={11} className="text-emerald-400" />
            Engagement
          </div>
          <p className="text-xl font-bold text-emerald-400 tabular-nums">
            {engagement}%
          </p>
        </div>
        <div className="rounded-xl bg-slate-800/60 p-3">
          <div className="flex items-center gap-1.5 text-xs text-slate-500 mb-1">
            <Brain size={11} className="text-blue-400" />
            Concentration
          </div>
          <p className="text-xl font-bold text-blue-400 tabular-nums">
            {concentration}%
          </p>
        </div>
      </div>

      <div className="flex justify-center mb-3">
        <div className="relative w-28 h-28">
          <svg viewBox="0 0 100 100" className="w-full h-full -rotate-90">
            <circle
              cx="50"
              cy="50"
              r={r}
              fill="none"
              stroke="#1e293b"
              strokeWidth="8"
            />
            <circle
              cx="50"
              cy="50"
              r={r}
              fill="none"
              stroke={gaugeColor}
              strokeWidth="8"
              strokeLinecap="round"
              strokeDasharray={`${dash} ${circ}`}
              style={{ transition: "stroke-dasharray 0.6s ease" }}
            />
          </svg>
          <div className="absolute inset-0 flex flex-col items-center justify-center">
            <span className="text-2xl font-bold text-white tabular-nums">
              {engagement}%
            </span>
          </div>
        </div>
      </div>

      <div className="flex items-center justify-between text-xs text-slate-500">
        <span className="flex items-center gap-1.5">
          <Users size={11} /> {studentCount} students
        </span>
        <span className="flex items-center gap-1.5">
          <span
            className={`w-1.5 h-1.5 rounded-full ${mood ? "bg-emerald-400 animate-pulse" : "bg-slate-600"}`}
          />
          {mood ? "Live" : "Waiting…"}
        </span>
      </div>
    </div>
  );
}
