import React, {
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
} from "react";
import { useLocation } from "react-router-dom";
import {
  Video,
  VideoOff,
  StopCircle,
  Users,
  Zap,
  Brain,
  AlertTriangle,
  CheckCircle,
  ChevronDown,
  Wifi,
  WifiOff,
  MapPin,
  BookOpen,
  Clock,
  Activity,
  Camera,
} from "lucide-react";
import LecturerLayout from "../../layouts/LecturerLayout";
import { AuthContext } from "../../context/AuthContext";
import lecturerService from "../../services/lecturerService";
import sessionService from "../../services/sessionService";
import emotionService from "../../services/emotionService";
import { createSessionClient } from "../../services/websocket";
import alertService from "../../services/alertService";
import attendanceService from "../../services/attendanceService";
import StudentExitList from "./StudentExitList";

// Constants
const FRAME_INTERVAL_MS = 3000;
const SNAPSHOT_POLL_MS = 10000;
const ALERT_POLL_MS = 8000;   // poll for new alerts every 8 s
const EXIT_POLL_MS = 15000;   // refresh exit list every 15 s

const EMOTION_META = {
  happy: {
    label: "Happy",
    color: "text-emerald-400",
    bg: "bg-emerald-500/15",
    dot: "bg-emerald-400",
    emoji: "😊",
  },
  engaged: {
    label: "Engaged",
    color: "text-[#667D9D]",
    bg: "bg-[#667D9D]/15",
    dot: "bg-[#667D9D]",
    emoji: "🎯",
  },
  neutral: {
    label: "Neutral",
    color: "text-slate-300",
    bg: "bg-slate-700/40",
    dot: "bg-slate-500",
    emoji: "😐",
  },
  confused: {
    label: "Confused",
    color: "text-amber-400",
    bg: "bg-amber-500/15",
    dot: "bg-amber-400",
    emoji: "🤔",
  },
  surprised: {
    label: "Surprised",
    color: "text-teal-400",
    bg: "bg-teal-500/15",
    dot: "bg-teal-400",
    emoji: "😮",
  },
  sad: {
    label: "Sad",
    color: "text-[#667D9D]",
    bg: "bg-[#667D9D]/15",
    dot: "bg-[#667D9D]",
    emoji: "😔",
  },
  angry: {
    label: "Angry",
    color: "text-rose-400",
    bg: "bg-rose-500/15",
    dot: "bg-rose-400",
    emoji: "😠",
  },
  fearful: {
    label: "Fearful",
    color: "text-orange-400",
    bg: "bg-orange-500/15",
    dot: "bg-orange-400",
    emoji: "😨",
  },
  disgusted: {
    label: "Disgusted",
    color: "text-pink-400",
    bg: "bg-pink-500/15",
    dot: "bg-pink-400",
    emoji: "🤢",
  },
};

const SEVERITY_STYLES = {
  critical: "border-rose-500/40 bg-rose-500/10 text-rose-300",
  warning: "border-amber-500/40 bg-amber-500/10 text-amber-300",
  info: "border-[#667D9D]/40 bg-[#667D9D]/10 text-[#ACBBC6]",
};

// Helper function to convert concentration to level string
const getConcentrationLevel = (concentration) => {
  if (typeof concentration === "number") {
    if (concentration >= 0.7) return "high";
    if (concentration >= 0.4) return "medium";
    return "low";
  }
  if (typeof concentration === "string") {
    return concentration.toLowerCase();
  }
  return "medium";
};

const CONCENTRATION_STYLE = {
  high: "bg-emerald-500/20 text-emerald-300 border-emerald-500/30",
  medium: "bg-amber-500/20   text-amber-300 border-amber-500/30",
  low: "bg-rose-500/20    text-rose-300 border-rose-500/30",
  distracted: "bg-rose-500/20    text-rose-300 border-rose-500/30",
};

// Sub-components
const Skeleton = ({ className = "" }) => (
  <div className={`animate-pulse rounded-xl bg-slate-800/60 ${className}`} />
);

function StatCard({
  icon: Icon,
  label,
  value,
  sub,
  color = "emerald",
  large = false,
}) {
  const colors = {
    emerald: "bg-emerald-500/15 text-emerald-400",
    violet: "bg-[#667D9D]/15 text-[#667D9D]",
    blue: "bg-[#667D9D]/15 text-[#667D9D]",
    amber: "bg-amber-500/15 text-amber-400",
    rose: "bg-rose-500/15 text-rose-400",
  };
  return (
    <div className="glass rounded-2xl p-4 flex items-center gap-3">
      <div
        className={`rounded-xl flex items-center justify-center shrink-0 ${large ? "w-12 h-12" : "w-10 h-10"} ${colors[color]}`}
      >
        <Icon size={large ? 22 : 18} />
      </div>
      <div className="min-w-0">
        <p className="text-xs text-slate-500 font-medium">{label}</p>
        <p
          className={`font-bold text-white leading-tight ${large ? "text-2xl" : "text-xl"}`}
        >
          {value ?? "—"}
        </p>
        {sub && <p className="text-xs text-slate-500 mt-0.5">{sub}</p>}
      </div>
    </div>
  );
}

function EngagementArc({ value = 0 }) {
  const pct = Math.min(100, Math.max(0, Math.round(value * 100)));
  const color = pct >= 70 ? "#10b981" : pct >= 45 ? "#f59e0b" : "#f43f5e";
  const r = 44;
  const circ = 2 * Math.PI * r;
  const dash = (pct / 100) * circ;

  return (
    <div className="glass rounded-2xl p-5 flex flex-col items-center justify-center gap-2 border-t-2 border-[#16254F]/60">
      <p className="text-xs text-[#ACBBC6] font-medium">Engagement</p>
      <div className="relative w-28 h-28">
        <svg viewBox="0 0 100 100" className="w-full h-full -rotate-90">
          <circle
            cx="50"
            cy="50"
            r={r}
            fill="none"
            stroke="rgba(22,37,79,0.6)"
            strokeWidth="8"
          />
          <circle
            cx="50"
            cy="50"
            r={r}
            fill="none"
            stroke={color}
            strokeWidth="8"
            strokeLinecap="round"
            strokeDasharray={`${dash} ${circ}`}
            style={{ transition: "stroke-dasharray 0.6s ease" }}
          />
        </svg>
        <div className="absolute inset-0 flex flex-col items-center justify-center">
          <span className="text-2xl font-bold text-white">{pct}%</span>
        </div>
      </div>
    </div>
  );
}

function ConcentrationBars({ high = 0, medium = 0, low = 0 }) {
  const total = high + medium + low || 1;
  const bars = [
    {
      label: "High",
      value: high,
      pct: Math.round((high / total) * 100),
      color: "bg-emerald-500",
    },
    {
      label: "Medium",
      value: medium,
      pct: Math.round((medium / total) * 100),
      color: "bg-amber-500",
    },
    {
      label: "Low",
      value: low,
      pct: Math.round((low / total) * 100),
      color: "bg-rose-500",
    },
  ];
  return (
    <div className="rounded-2xl p-4 border border-[#667D9D]/20" style={{ background: 'rgba(22,37,79,0.45)', backdropFilter: 'blur(14px)' }}>
      <p className="text-xs font-medium mb-3 flex items-center gap-1.5 text-[#ACBBC6]">
        <Brain size={12} className="text-[#667D9D]" /> Concentration
      </p>
      <div className="space-y-2.5">
        {bars.map((b) => (
          <div key={b.label}>
            <div className="flex justify-between text-xs mb-1">
              <span className="text-[#ACBBC6]">{b.label}</span>
              <span className="text-[#667D9D]">{b.value} students</span>
            </div>
            <div className="h-1.5 bg-[#060817]/60 rounded-full overflow-hidden">
              <div
                className={`h-full rounded-full ${b.color} transition-all duration-700`}
                style={{ width: `${b.pct}%` }}
              />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function AlertItem({ alert }) {
  const style = SEVERITY_STYLES[alert.severity] ?? SEVERITY_STYLES.info;
  const Icon =
    alert.severity === "critical"
      ? AlertTriangle
      : alert.severity === "warning"
        ? AlertTriangle
        : CheckCircle;
  return (
    <div
      className={`flex items-start gap-2.5 px-3 py-2.5 rounded-xl border text-sm ${style}`}
    >
      <Icon size={14} className="shrink-0 mt-0.5" />
      <div className="min-w-0">
        <p className="font-medium text-xs leading-none mb-0.5">{alert.title}</p>
        <p className="text-xs opacity-80 leading-snug">{alert.message}</p>
      </div>
      <span className="text-xs opacity-50 shrink-0 ml-auto">
        {alert.timestamp
          ? new Date(alert.timestamp).toLocaleTimeString([], {
              hour: "2-digit",
              minute: "2-digit",
            })
          : ""}
      </span>
    </div>
  );
}

function StudentRow({ student }) {
  const meta =
    EMOTION_META[student.emotion?.toLowerCase()] ?? EMOTION_META.neutral;
  const concentrationLevel = getConcentrationLevel(student.concentration);
  const concStyle =
    CONCENTRATION_STYLE[concentrationLevel] ??
    "bg-slate-500/20 text-slate-300 border-slate-500/30";
  const initials = (student.studentName ?? "?")
    .split(" ")
    .map((p) => p[0])
    .join("")
    .toUpperCase()
    .slice(0, 2);

  return (
    <div className="flex items-center gap-3 px-3 py-2.5 rounded-xl bg-slate-800/40 border border-slate-700/40">
      <div className="w-8 h-8 rounded-full bg-[#667D9D]/20 text-[#ACBBC6] flex items-center justify-center text-xs font-bold shrink-0">
        {initials}
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-sm font-medium text-slate-200 truncate">
          {student.studentName}
        </p>
        <span
          className={`inline-flex items-center gap-1.5 text-xs mt-0.5 ${meta.color}`}
        >
          <span className={`w-1.5 h-1.5 rounded-full shrink-0 ${meta.dot}`} />
          <span>{meta.label}</span>
        </span>
      </div>
      <span
        className={`text-xs px-2.5 py-0.5 rounded-full capitalize shrink-0 border font-semibold ${concStyle}`}
      >
        {concentrationLevel}
      </span>
    </div>
  );
}

// Duration hook
function useDuration(startTime) {
  const [elapsed, setElapsed] = useState(0);
  useEffect(() => {
    if (!startTime) return;
    const tick = () =>
      setElapsed(
        Math.floor((Date.now() - new Date(startTime).getTime()) / 1000),
      );
    tick();
    const id = setInterval(tick, 1000);
    return () => clearInterval(id);
  }, [startTime]);
  const h = String(Math.floor(elapsed / 3600)).padStart(2, "0");
  const m = String(Math.floor((elapsed % 3600) / 60)).padStart(2, "0");
  const s = String(elapsed % 60).padStart(2, "0");
  return `${h}:${m}:${s}`;
}

// Main component
export default function LecturerLiveSession() {
  const location = useLocation();
  const { user } = useContext(AuthContext);
  const cameraConfig = location.state?.camera;

  const [courses, setCourses] = useState([]);
  const [loadingCourses, setLoadingCourses] = useState(true);
  const [form, setForm] = useState({ courseId: "", room: "", duration: "2" });
  const [starting, setStarting] = useState(false);
  const [setupError, setSetupError] = useState("");

  const [session, setSession] = useState(null);
  const [checkingActive, setCheckingActive] = useState(true);

  const [mood, setMood] = useState(null);
  const [alerts, setAlerts] = useState([]);
  const [snapshot, setSnapshot] = useState(null);
  const [detectedStudents, setDetectedStudents] = useState(new Map());

  const [cameraOn, setCameraOn] = useState(false);
  const [cameraError, setCameraError] = useState("");
  const [wsConnected, setWsConnected] = useState(false);
  const [availableCameras, setAvailableCameras] = useState([]);
  const [showCameraSelector, setShowCameraSelector] = useState(false);
  const [videoReady, setVideoReady] = useState(false);

  const [exits, setExits] = useState([]);
  const [seenAlertIds, setSeenAlertIds] = useState(new Set());

  const videoRef = useRef(null);
  const streamRef = useRef(null);
  const wsClientRef = useRef(null);
  const frameTimer = useRef(null);
  const pollTimer = useRef(null);
  const alertTimer = useRef(null);
  const exitTimer = useRef(null);

  const duration = useDuration(session?.startTime);

  // Load available cameras
  useEffect(() => {
    const loadCameras = async () => {
      try {
        const devices = await navigator.mediaDevices.enumerateDevices();
        const videoDevices = devices.filter(
          (device) => device.kind === "videoinput",
        );
        setAvailableCameras(videoDevices);
      } catch (err) {
        console.error("Error loading cameras:", err);
      }
    };
    loadCameras();
  }, []);

  // Start camera with selected config
  const startCamera = useCallback(
    async (deviceId = null) => {
      setCameraError("");
      try {
        let constraints = {
          video: {
            width: { ideal: 1280 },
            height: { ideal: 720 },
            facingMode: "user",
          },
          audio: false,
        };

        if (cameraConfig && cameraConfig.deviceId) {
          constraints = {
            video: {
              deviceId: { exact: cameraConfig.deviceId },
              width: {
                ideal: parseInt(cameraConfig.resolution?.split("x")[0]) || 1280,
              },
              height: {
                ideal: parseInt(cameraConfig.resolution?.split("x")[1]) || 720,
              },
              frameRate: { ideal: cameraConfig.fps || 30 },
            },
            audio: false,
          };
        } else if (deviceId) {
          constraints = {
            video: {
              deviceId: { exact: deviceId },
              width: { ideal: 1280 },
              height: { ideal: 720 },
            },
            audio: false,
          };
        }

        const stream = await navigator.mediaDevices.getUserMedia(constraints);
        streamRef.current = stream;
        if (videoRef.current) {
          videoRef.current.srcObject = stream;
          await videoRef.current.play().catch(() => {});
        }
        setCameraOn(true);
        setShowCameraSelector(false);
      } catch (e) {
        console.error("Camera error:", e);
        setCameraError("Camera access denied — frame capture disabled.");
        setCameraOn(false);
      }
    },
    [cameraConfig],
  );

  // 🔥 FIX: Start camera with delay to ensure DOM is ready
  useEffect(() => {
    if (session && !cameraOn && !cameraError) {
      const timer = setTimeout(() => {
        startCamera();
      }, 500);
      return () => clearTimeout(timer);
    }
  }, [session, cameraOn, cameraError, startCamera]);

  // Switch camera
  const switchCamera = async (deviceId) => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach((track) => track.stop());
    }
    await startCamera(deviceId);
  };

  // On mount
  useEffect(() => {
    lecturerService
      .getDashboard()
      .then((d) => setCourses(d.courses ?? []))
      .catch(() => {})
      .finally(() => setLoadingCourses(false));

    sessionService
      .getActive()
      .then((list) => {
        if (list?.length > 0) activateSession(list[0]);
      })
      .catch(() => {})
      .finally(() => setCheckingActive(false));

    return () => cleanup();
  }, []);

  const activateSession = useCallback(
    (sess) => {
      setSession(sess);
      // 🔥 FIX: Add delay before starting camera
      setTimeout(() => {
        startCamera();
        // connectWebSocket(sess.sessionId)
        startSnapshotPoll(sess.sessionId);
      }, 300);
    },
    [startCamera],
  );

  const cleanup = () => {
    clearInterval(frameTimer.current);
    clearInterval(pollTimer.current);
    clearInterval(alertTimer.current);
    clearInterval(exitTimer.current);
    wsClientRef.current?.deactivate();
    if (streamRef.current) {
      streamRef.current.getTracks().forEach((t) => t.stop());
      streamRef.current = null;
    }
  };

  const connectWebSocket = (sessionId) => {
    const client = createSessionClient({
      sessionId,
      onMood: (data) => setMood(data),
      onAlert: (data) => setAlerts((prev) => [data, ...prev].slice(0, 20)),
      onConnect: () => setWsConnected(true),
      onDisconnect: () => setWsConnected(false),
    });
    wsClientRef.current = client;
  };

  const parseConcentration = (concentration) => {
    if (typeof concentration === "number") return concentration;
    if (typeof concentration === "string") {
      const map = { low: 0.3, medium: 0.6, high: 0.9 };
      return map[concentration.toLowerCase()] || 0.6;
    }
    if (typeof concentration === "object" && concentration?.level) {
      return parseConcentration(concentration.level);
    }
    return 0.6;
  };

  // Frame capture loop
  const startFrameCapture = useCallback((sessionId) => {
    clearInterval(frameTimer.current);
    frameTimer.current = setInterval(() => {
      const video = videoRef.current;
      if (!video || !video.videoWidth) return;
      const canvas = document.createElement("canvas");
      canvas.width = video.videoWidth;
      canvas.height = video.videoHeight;
      canvas.getContext("2d").drawImage(video, 0, 0);
      canvas.toBlob(
        (blob) => {
          if (!blob) return;
          const formData = new FormData();
          formData.append("session_id", sessionId);
          formData.append("store", "true");
          formData.append("file", blob, "frame.jpg");

          fetch("http://localhost:8000/analyze/frame", {
            method: "POST",
            body: formData,
          })
            .then((r) => (r.ok ? r.json() : Promise.reject(r.status)))
            .then((data) => {
              const people = data.people ?? [];

              // Build emotion counts from people array (Python omits emotion_counts from response)
              const counts = {};
              let totalConc = 0;
              people.forEach((p) => {
                const e = (
                  p.dominant_emotion ??
                  p.emotion ??
                  "neutral"
                ).toLowerCase();
                counts[e] = (counts[e] ?? 0) + 1;
                totalConc +=
                  typeof p.concentration === "number" ? p.concentration : 0.5;
              });
              const n = people.length || 1;
              const avgConc = totalConc / n;
              const weightedPositive =
                (counts.happy ?? 0) +
                (counts.surprised ?? 0) +
                (counts.neutral ?? 0) * 0.5;
              const totalEmotions =
                Object.values(counts).reduce((s, v) => s + v, 0) || 1;
              const emotionEngagement = weightedPositive / totalEmotions;
              const engagementScore = (emotionEngagement + avgConc) / 2;

              const dominant =
                Object.keys(counts).length > 0
                  ? Object.entries(counts).sort((a, b) => b[1] - a[1])[0][0]
                  : "neutral";
              setMood({
                studentCount: data.student_count ?? 0,
                engagementScore,
                dominantEmotion: dominant,
                concentration: avgConc,
              });

              const recognized = (data.people ?? []).filter(
                (p) => p.student_id,
              );
              const physicalCount = data.student_count ?? 0;

              if (recognized.length > 0) {
                setDetectedStudents((prev) => {
                  if (physicalCount === 1 && prev.size >= 1) {
                    const locked = [...prev.values()][0];
                    const latest = recognized[0];
                    const next = new Map();
                    next.set(locked.studentId, {
                      ...locked,
                      emotion: latest.dominant_emotion ?? locked.emotion,
                      concentration: parseConcentration(latest.concentration),
                      lastSeen: Date.now(),
                    });
                    return next;
                  }

                  const next = new Map(prev);
                  recognized.forEach((p) => {
                    next.set(p.student_id, {
                      studentId: p.student_id,
                      studentName: p.student_name ?? p.student_id,
                      emotion: p.dominant_emotion ?? "neutral",
                      concentration: parseConcentration(p.concentration),
                      lastSeen: Date.now(),
                    });
                  });
                  return next;
                });
              }
            })
            .catch((err) => console.error("Frame send error:", err));
        },
        "image/jpeg",
        0.8,
      );
    }, FRAME_INTERVAL_MS);
  }, []);

  const startSnapshotPoll = (sessionId) => {
    // Emotion snapshot
    clearInterval(pollTimer.current);
    const pollSnapshot = () =>
      emotionService.getLatest(sessionId).then(setSnapshot).catch(() => {});
    pollSnapshot();
    pollTimer.current = setInterval(pollSnapshot, SNAPSHOT_POLL_MS);

    // Alert polling — only prepend alerts we haven't seen yet
    clearInterval(alertTimer.current);
    const pollAlerts = () =>
      alertService.getSessionAlerts(sessionId).then((fetched) => {
        if (!Array.isArray(fetched)) return;
        setAlerts((prev) => {
          const prevIds = new Set(prev.map((a) => a.id));
          const fresh = fetched.filter((a) => !prevIds.has(a.id));
          if (fresh.length === 0) return prev;
          return [...fresh, ...prev].slice(0, 30);
        });
      }).catch(() => {});
    pollAlerts();
    alertTimer.current = setInterval(pollAlerts, ALERT_POLL_MS);

    // Exit log polling
    clearInterval(exitTimer.current);
    const pollExits = () =>
      attendanceService.getSessionExits(sessionId).then(setExits).catch(() => {});
    pollExits();
    exitTimer.current = setInterval(pollExits, EXIT_POLL_MS);
  };

  useEffect(() => {
    if (session && cameraOn) startFrameCapture(session.sessionId);
    return () => clearInterval(frameTimer.current);
  }, [session, cameraOn, startFrameCapture]);

  const handleStart = async (e) => {
    e.preventDefault();
    if (!form.courseId) {
      setSetupError("Please select a course.");
      return;
    }
    if (!form.room) {
      setSetupError("Please enter a room location.");
      return;
    }
    setStarting(true);
    setSetupError("");
    const now = new Date();
    const end = new Date(
      now.getTime() + Number(form.duration) * 60 * 60 * 1000,
    );
    try {
      const sess = await sessionService.start({
        courseId: form.courseId,
        roomLocation: form.room,
        cameraType: "webcam",
        scheduledStart: now.toISOString().slice(0, 19),
        scheduledEnd: end.toISOString().slice(0, 19),
      });
      activateSession(sess);
    } catch (e) {
      setSetupError(e.response?.data?.message ?? "Failed to start session.");
    } finally {
      setStarting(false);
    }
  };

  const handleEnd = async () => {
    try {
      await sessionService.end(session.sessionId);
    } catch {}
    cleanup();
    setSession(null);
    setCameraOn(false);
    setWsConnected(false);
    setMood(null);
    setAlerts([]);
    setSnapshot(null);
    setDetectedStudents(new Map());
    setExits([]);
  };

  const engagement =
    mood?.engagementScore ?? snapshot?.classSnapshot?.engagementScore ?? 0;
  const dominantEmotion =
    mood?.dominantEmotion ??
    snapshot?.classSnapshot?.dominantEmotion ??
    "neutral";
  const studentCount = mood?.studentCount ?? session?.studentCount ?? 0;
  const emoMeta =
    EMOTION_META[dominantEmotion?.toLowerCase()] ?? EMOTION_META.neutral;

  const concCounts = (() => {
    const students = snapshot?.studentSnapshots ?? [];
    return {
      high: students.filter(
        (s) => getConcentrationLevel(s.concentration) === "high",
      ).length,
      medium: students.filter(
        (s) => getConcentrationLevel(s.concentration) === "medium",
      ).length,
      low: students.filter(
        (s) => getConcentrationLevel(s.concentration) === "low",
      ).length,
    };
  })();

  if (checkingActive) {
    return (
      <LecturerLayout>
        <div className="flex items-center justify-center h-64 text-slate-500 text-sm">
          Checking for active sessions…
        </div>
      </LecturerLayout>
    );
  }

  if (!session) {
    return (
      <LecturerLayout>
        <div className="max-w-lg mx-auto mt-10">
          <div className="flex items-center gap-3 mb-6">
            <div className="w-11 h-11 rounded-xl bg-[#667D9D]/15 flex items-center justify-center">
              <Video size={20} className="text-[#667D9D]" />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-white">
                Start Live Session
              </h1>
              <p className="text-sm text-slate-500 mt-0.5">
                Open camera and begin real-time emotion detection
              </p>
            </div>
          </div>

          <div className="glass rounded-2xl p-6">
            {setupError && (
              <div className="flex items-center gap-2 px-3 py-2.5 mb-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
                <AlertTriangle size={14} /> {setupError}
              </div>
            )}

            <form onSubmit={handleStart} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1.5">
                  Course
                </label>
                {loadingCourses ? (
                  <Skeleton className="h-10" />
                ) : (
                  <div className="relative">
                    <BookOpen
                      size={14}
                      className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none"
                    />
                    <select
                      value={form.courseId}
                      onChange={(e) =>
                        setForm((f) => ({ ...f, courseId: e.target.value }))
                      }
                      required
                      className="input-field w-full pl-9 pr-8 py-2.5 text-sm appearance-none"
                    >
                      <option value="">Select a course…</option>
                      {courses.map((c) => (
                        <option key={c.courseId} value={c.courseId}>
                          {c.code} — {c.title}
                        </option>
                      ))}
                    </select>
                    <ChevronDown
                      size={13}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none"
                    />
                  </div>
                )}
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1.5">
                  Room / Location
                </label>
                <div className="relative">
                  <MapPin
                    size={14}
                    className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none"
                  />
                  <input
                    value={form.room}
                    onChange={(e) =>
                      setForm((f) => ({ ...f, room: e.target.value }))
                    }
                    placeholder="e.g. Hall B, Lab 3…"
                    required
                    className="input-field w-full pl-9 pr-4 py-2.5 text-sm placeholder-slate-600"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1.5">
                  Planned Duration
                </label>
                <div className="relative">
                  <Clock
                    size={14}
                    className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none"
                  />
                  <select
                    value={form.duration}
                    onChange={(e) =>
                      setForm((f) => ({ ...f, duration: e.target.value }))
                    }
                    className="input-field w-full pl-9 pr-8 py-2.5 text-sm appearance-none"
                  >
                    {[1, 1.5, 2, 2.5, 3].map((h) => (
                      <option key={h} value={h}>
                        {h} {h === 1 ? "hour" : "hours"}
                      </option>
                    ))}
                  </select>
                  <ChevronDown
                    size={13}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none"
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={starting || loadingCourses}
                className="w-full py-3 rounded-xl bg-[#16254F] hover:bg-[#667D9D] disabled:opacity-60 text-white font-semibold text-sm transition-colors flex items-center justify-center gap-2 mt-2"
              >
                {starting ? (
                  "Starting…"
                ) : (
                  <>
                    <Video size={15} /> Start Live Session
                  </>
                )}
              </button>
            </form>
          </div>

          <p className="text-xs text-slate-600 text-center mt-4">
            Your browser camera will be used to detect student emotions in real
            time. Frames are analyzed every {FRAME_INTERVAL_MS / 1000}s.
          </p>
        </div>
      </LecturerLayout>
    );
  }

  return (
    <LecturerLayout>
      <div className="flex items-center justify-between mb-5">
        <div className="flex items-center gap-3 min-w-0">
          <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-rose-500/15 border border-rose-500/30 shrink-0">
            <span className="w-2 h-2 rounded-full bg-rose-500 animate-pulse" />
            <span className="text-xs font-semibold text-rose-400">LIVE</span>
          </div>
          <div className="min-w-0">
            <h1 className="text-lg font-bold text-white truncate">
              {session.courseName ?? "Live Session"}
            </h1>
            <div className="flex items-center gap-3 text-xs text-slate-500 mt-0.5">
              <span className="flex items-center gap-1">
                <Clock size={11} /> {duration}
              </span>
              {wsConnected ? (
                <span className="flex items-center gap-1 text-emerald-500">
                  <Wifi size={11} /> Connected
                </span>
              ) : (
                <span className="flex items-center gap-1 text-slate-600">
                  <WifiOff size={11} /> Disconnected
                </span>
              )}
              {cameraError && (
                <span className="flex items-center gap-1 text-amber-500">
                  <VideoOff size={11} /> {cameraError}
                </span>
              )}
            </div>
          </div>
        </div>

        <div className="flex gap-2">
          <button
            onClick={() => setShowCameraSelector(!showCameraSelector)}
            className="flex items-center gap-2 px-3 py-2 rounded-xl bg-slate-700/50 hover:bg-slate-700 text-slate-300 text-sm font-medium transition-all"
          >
            <Camera size={14} /> Switch Camera
          </button>
          <button
            onClick={handleEnd}
            className="flex items-center gap-2 px-4 py-2 rounded-xl bg-rose-600/20 hover:bg-rose-600/40 border border-rose-500/30 text-rose-400 text-sm font-medium transition-all shrink-0"
          >
            <StopCircle size={15} /> End Session
          </button>
        </div>
      </div>

      {/* Camera Selector Modal */}
      {showCameraSelector && availableCameras.length > 0 && (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50">
          <div className="glass rounded-xl p-6 max-w-md w-full mx-4">
            <h3 className="text-white font-bold mb-4">Select Camera</h3>
            <div className="space-y-2 mb-6">
              {availableCameras.map((camera, idx) => (
                <button
                  key={camera.deviceId}
                  onClick={() => {
                    switchCamera(camera.deviceId);
                  }}
                  className="w-full p-3 text-left bg-slate-700 hover:bg-slate-600 rounded-lg text-white transition"
                >
                  {camera.label || `Camera ${idx + 1}`}
                </button>
              ))}
            </div>
            <button
              onClick={() => setShowCameraSelector(false)}
              className="w-full py-2 bg-slate-600 hover:bg-slate-500 rounded-lg text-white transition"
            >
              Cancel
            </button>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-5 gap-5">
        <div className="lg:col-span-2 flex flex-col gap-4">
          <div className="glass rounded-2xl overflow-hidden aspect-video relative bg-slate-900/80">
            {cameraOn ? (
              <video
                ref={videoRef}
                autoPlay
                muted
                playsInline
                onCanPlay={() => setVideoReady(true)}
                className="w-full h-full object-cover scale-x-[-1]"
              />
            ) : (
              <div className="absolute inset-0 flex flex-col items-center justify-center gap-2 text-slate-600">
                <VideoOff size={32} className="opacity-40" />
                <p className="text-xs">Camera unavailable</p>
              </div>
            )}
            {cameraOn && studentCount > 0 && (
              <div className="absolute bottom-2 left-2 flex items-center gap-1.5 px-2 py-1 rounded-lg bg-black/60 backdrop-blur-sm text-xs text-white">
                <Users size={12} className="text-emerald-400" />
                {studentCount} detected
              </div>
            )}
          </div>

          <ConcentrationBars {...concCounts} />
        </div>

        <div className="lg:col-span-3 flex flex-col gap-4">
          <div className="grid grid-cols-3 gap-3">
            <div className="rounded-2xl p-4 flex items-center gap-3 border border-[#667D9D]/25" style={{ background: 'rgba(22,37,79,0.35)', backdropFilter: 'blur(14px)' }}>
              <div className="w-10 h-10 rounded-xl bg-[#667D9D]/20 flex items-center justify-center shrink-0">
                <Users size={18} className="text-[#ACBBC6]" />
              </div>
              <div className="min-w-0">
                <p className="text-xs text-[#667D9D] font-medium">Students</p>
                <p className="text-xl font-bold text-[#ECECEC] leading-tight">{studentCount ?? "—"}</p>
                <p className="text-xs text-[#667D9D] mt-0.5">detected</p>
              </div>
            </div>
            <EngagementArc value={engagement} />
            <div
              className={`glass rounded-2xl p-4 flex flex-col items-center justify-center gap-1.5 border-t-2 border-[#667D9D]/40 ${emoMeta.bg}`}
            >
              <p className="text-xs text-[#ACBBC6] font-medium">Mood</p>
              <span
                className={`w-9 h-9 rounded-full flex items-center justify-center shrink-0 ${emoMeta.dot}`}
              />
              <span className={`text-sm font-semibold ${emoMeta.color}`}>
                {emoMeta.label}
              </span>
            </div>
          </div>

          {/* Detected Students */}
          <div className="glass rounded-2xl p-4 border-l-2 border-[#667D9D]/50">
            <div className="flex items-center justify-between mb-3">
              <p className="text-xs text-[#ACBBC6] font-medium flex items-center gap-1.5">
                <Users size={12} className="text-[#667D9D]" /> Detected Students
              </p>
              {detectedStudents.size > 0 && (
                <span className="text-xs px-1.5 py-0.5 rounded-full bg-[#667D9D]/20 text-[#ACBBC6] font-medium">
                  {detectedStudents.size}
                </span>
              )}
            </div>
            {detectedStudents.size === 0 ? (
              <div className="flex flex-col items-center justify-center py-5 text-slate-700">
                <Users size={20} className="mb-1.5 opacity-30" />
                <p className="text-xs">No faces recognized yet</p>
              </div>
            ) : (
              <div className="space-y-2 max-h-52 overflow-y-auto pr-1">
                {[...detectedStudents.values()].map((s) => (
                  <StudentRow key={s.studentId} student={s} />
                ))}
              </div>
            )}
          </div>

          {snapshot?.studentSnapshots?.length > 0 && (
            <div className="glass rounded-2xl p-4 border-l-2 border-[#ACBBC6]/40">
              <p className="text-xs text-[#ACBBC6] font-medium mb-3 flex items-center gap-1.5">
                <Activity size={12} className="text-[#667D9D]" /> Emotion Distribution
              </p>
              <div className="flex flex-wrap gap-2">
                {Object.entries(
                  snapshot.studentSnapshots.reduce((acc, s) => {
                    const k = s.emotion?.toLowerCase() ?? "neutral";
                    acc[k] = (acc[k] ?? 0) + 1;
                    return acc;
                  }, {}),
                )
                  .sort(([, a], [, b]) => b - a)
                  .map(([emotion, count]) => {
                    const m = EMOTION_META[emotion] ?? EMOTION_META.neutral;
                    return (
                      <div
                        key={emotion}
                        className={`flex items-center gap-1.5 px-2.5 py-1.5 rounded-xl ${m.bg}`}
                      >
                        <span
                          className={`w-2 h-2 rounded-full shrink-0 ${m.dot}`}
                        />
                        <span className={`text-xs font-medium ${m.color}`}>
                          {m.label}
                        </span>
                        <span className="text-xs text-slate-500">{count}</span>
                      </div>
                    );
                  })}
              </div>
            </div>
          )}

          <div className="glass rounded-2xl p-4 flex-1 min-h-0 border-l-2 border-rose-500/30">
            <p className="text-xs text-[#ACBBC6] font-medium mb-3 flex items-center gap-1.5">
              <AlertTriangle size={12} className="text-rose-400" /> Live Alerts
              {alerts.length > 0 && (
                <span className="ml-auto text-xs px-1.5 py-0.5 rounded-full bg-rose-500/20 text-rose-400">
                  {alerts.length}
                </span>
              )}
            </p>
            {alerts.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-6 text-slate-700">
                <CheckCircle size={22} className="mb-1.5 opacity-40" />
                <p className="text-xs">No alerts yet — class is going well</p>
              </div>
            ) : (
              <div className="space-y-2 max-h-48 overflow-y-auto pr-1">
                {alerts.map((a, i) => (
                  <AlertItem key={a.id || i} alert={a} />
                ))}
              </div>
            )}
          </div>

          {/* Student Exits — live-updating via poll */}
          <div className="glass rounded-2xl p-4 border-l-2 border-amber-500/30">
            <p className="text-xs text-[#ACBBC6] font-medium mb-3 flex items-center gap-1.5">
              🚪 Student Exits
              {exits.length > 0 && (
                <span className="ml-auto text-xs px-1.5 py-0.5 rounded-full bg-amber-500/20 text-amber-400">
                  {exits.length}
                </span>
              )}
            </p>
            {exits.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-4 text-slate-700">
                <p className="text-xs">No exits recorded yet</p>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-xs">
                  <thead>
                    <tr className="text-slate-500 border-b border-slate-700/50">
                      <th className="text-left py-2 pr-3">Student</th>
                      <th className="text-center py-2 px-2">Left at</th>
                      <th className="text-center py-2 px-2">Returned</th>
                    </tr>
                  </thead>
                  <tbody>
                    {exits.map((exit, i) => {
                      const fmt = (dt) => dt ? new Date(dt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '—';
                      return (
                        <tr key={i} className={`border-b border-slate-700/30 ${!exit.returnTime ? 'bg-rose-500/5' : ''}`}>
                          <td className="py-2 pr-3 text-slate-300">
                            {exit.studentName || 'Unknown'}
                            {!exit.returnTime && <span className="text-rose-400 ml-1">(still out)</span>}
                          </td>
                          <td className="text-center py-2 px-2 text-amber-400">{fmt(exit.exitTime)}</td>
                          <td className="text-center py-2 px-2 text-emerald-400">{fmt(exit.returnTime)}</td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </div>
    </LecturerLayout>
  );
}