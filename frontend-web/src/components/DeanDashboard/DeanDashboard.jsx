import React, { useEffect, useState, useCallback } from "react";
import { getDashboard, getLecturerPerformance, getCourseStats, getWeeklyTrends } from "../../services/deanService";
import { LineChart, Line, PieChart, Pie, Cell, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer } from "recharts";
import { RefreshCw, Download, Users, School, BarChart2, UserCheck } from "lucide-react";
import clsx from "clsx";
import dayjs from "dayjs";

const glassCard = "bg-white/40 backdrop-blur-md shadow-lg rounded-xl border border-white/20";

const statCards = [
  { label: "Total Students", icon: Users, key: "totalStudents" },
  { label: "Total Lecturers", icon: School, key: "totalLecturers" },
  { label: "Total Courses", icon: BarChart2, key: "totalCourses" },
  { label: "Avg Engagement", icon: UserCheck, key: "avgEngagement", format: v => `${Math.round(v)}%` },
];

const COLORS = [
  "#82ca9d", "#8884d8", "#ffc658", "#ff8042", "#0088FE", "#00C49F", "#FFBB28", "#FF4444",
];

function Skeleton({ className }) {
  return (
    <div className={clsx("animate-pulse bg-gray-200/50 rounded", className)} />
  );
}

function formatEngagement(v) {
  return `${Math.round(v || 0)}%`;
}

function exportCSV(data, name) {
  const csv =
    Array.isArray(data) && data.length
      ? [
          Object.keys(data[0]).join(","),
          ...data.map((row) => Object.values(row).join(",")),
        ].join("\n")
      : "";
  const blob = new Blob([csv], { type: "text/csv" });
  const a = document.createElement("a");
  a.href = URL.createObjectURL(blob);
  a.download = name + ".csv";
  a.click();
}

export default function DeanDashboard() {
  const [stats, setStats] = useState(null);
  const [lecturers, setLecturers] = useState([]);
  const [courses, setCourses] = useState([]);
  const [trend, setTrend] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errors, setErrors] = useState({});
  const [refreshing, setRefreshing] = useState(false);

  const fetchAll = useCallback(async () => {
    setLoading(true);
    setErrors({});
    try {
      const [dashboard, lecPerf, courseStats, weeklyTrends] = await Promise.all([
        getDashboard(),
        getLecturerPerformance(),
        getCourseStats(),
        getWeeklyTrends(),
      ]);
      setStats(dashboard);
      setLecturers(lecPerf);
      setCourses(courseStats);
      setTrend(weeklyTrends.reverse());
      setLoading(false);
    } catch (e) {
      setErrors({ load: e.message || "Error loading data" });
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchAll(); }, [fetchAll]);

  const handleRefresh = async () => {
    setRefreshing(true);
    await fetchAll();
    setRefreshing(false);
  };

  const now = dayjs();
  const department = "Faculty of Engineering"; // or pull from user context/store

  // Emotion distribution for chart (sum across session stats if available)
  let emotionDist = {};
  lecturers.forEach(l => {
    // assuming extra fields for demonstration; replace as needed
    if (l.emotionDistribution) {
      for (const [emo, cnt] of Object.entries(l.emotionDistribution))
        emotionDist[emo] = (emotionDist[emo] || 0) + cnt;
    }
  });
  // If not available, make a dummy one so pie chart doesn't error.
  if (!Object.keys(emotionDist).length) {
    emotionDist = {
      happy: 40, neutral: 30, sad: 10, angry: 8, surprised: 6, fearful: 4, disgust: 2,
    };
  }
  const emotionPieData = Object.entries(emotionDist).map(([k, v]) => ({
    name: k.charAt(0).toUpperCase() + k.slice(1),
    value: v,
  }));

  return (
    <div className={clsx("p-6 md:p-10", "bg-gradient-to-br from-[#f0f4ff] via-white to-[#faeef6] min-h-screen")}>
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-end mb-6 md:mb-8">
        <div className="flex-1">
          <h2 className="text-2xl font-extrabold text-gray-800 mb-1">Dean Dashboard</h2>
          <div className="text-lg text-gray-600">{department}</div>
          <div className="text-sm text-gray-400">{now.format("dddd, MMM D, YYYY")}</div>
        </div>
        <div className="flex items-center gap-3 mt-4 md:mt-0">
          <button
            className={clsx("px-4 py-2 rounded-lg flex items-center gap-2 font-medium text-[#16254F] hover:bg-[#ECECEC] transition", refreshing && "opacity-60 pointer-events-none")}
            onClick={handleRefresh}
            disabled={refreshing}
          >
            <RefreshCw className={clsx("w-5 h-5 animate-spin", !refreshing && "hidden")} />
            <RefreshCw className={clsx("w-5 h-5", refreshing && "hidden")} />
            {refreshing ? "Refreshing..." : "Refresh"}
          </button>
          <button
            className="px-4 py-2 rounded-lg flex items-center gap-2 font-medium text-green-700 hover:bg-green-50"
            onClick={() => exportCSV(lecturers, "lecturer_performance")}
          >
            <Download className="w-5 h-5" />
            Download Lecturers
          </button>
          <button
            className="px-4 py-2 rounded-lg flex items-center gap-2 font-medium text-green-700 hover:bg-green-50"
            onClick={() => exportCSV(courses, "course_stats")}
          >
            <Download className="w-5 h-5" />
            Download Courses
          </button>
        </div>
      </div>

      {/* Stat Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-6 mb-10">
        {statCards.map(({ label, icon: Icon, key, format }) => (
          <div key={key} className={clsx(glassCard, "px-6 py-5 flex flex-col items-center")}>
            <Icon className="w-7 h-7 text-[#667D9D] mb-2" />
            <div className="text-lg font-semibold">
              {loading ? <Skeleton className="w-16 h-6" /> :
                format ? format(stats[key]) : stats[key]}
            </div>
            <div className="text-xs text-gray-500 mt-1">{label}</div>
          </div>
        ))}
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-7 mb-10">
        {/* Weekly Attendance Trend */}
        <div className={clsx(glassCard, "p-5")}>
          <div className="font-semibold mb-2">Weekly Attendance Trend</div>
          {loading ? <Skeleton className="w-full h-48" /> :
            <ResponsiveContainer width="100%" height={220}>
              <LineChart data={trend}>
                <XAxis dataKey="label" tick={{ fontSize: 12 }} />
                <YAxis domain={[0, 100]} unit="%" />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="avgAttendance" name="Avg Attendance" stroke="#3182ce" strokeWidth={3} />
                <Line type="monotone" dataKey="studentCount" name="Students" stroke="#8884d8" strokeDasharray="4 4" />
              </LineChart>
            </ResponsiveContainer>}
        </div>
        {/* Emotion Pie */}
        <div className={clsx(glassCard, "p-5")}>
          <div className="font-semibold mb-2">Emotion Distribution</div>
          {loading ? <Skeleton className="w-full h-48" /> :
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie
                  data={emotionPieData}
                  dataKey="value"
                  nameKey="name"
                  cx="50%"
                  cy="50%"
                  outerRadius={70}
                  fill="#8884d8"
                  label={({ name, percent }) =>
                    `${name}: ${(percent * 100).toFixed(0)}%`
                  }
                >
                  {emotionPieData.map((entry, i) => (
                    <Cell key={i} fill={COLORS[i % COLORS.length]} />
                  ))}
                </Pie>
                <Legend />
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>}
        </div>
      </div>

      {/* Lecturer Performance */}
      <div className={clsx(glassCard, "p-6 mb-7")}>
        <div className="font-semibold text-lg mb-3">Lecturer Performance</div>
        {loading ? (
          <Skeleton className="w-full h-40" />
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full text-sm">
              <thead>
                <tr className="text-xs font-bold text-left bg-white/30">
                  <th className="py-2 px-3">Name</th>
                  <th className="py-2 px-3">Department</th>
                  <th className="py-2 px-3">Courses</th>
                  <th className="py-2 px-3">Sentiment</th>
                  <th className="py-2 px-3">Concentration</th>
                  <th className="py-2 px-3">Attendance</th>
                  <th className="py-2 px-3">Rating</th>
                </tr>
              </thead>
              <tbody>
                {lecturers.map((l, i) => (
                  <tr key={i} className="border-b border-gray-200/40 hover:bg-gray-50/40">
                    <td className="py-2 px-3">{l.name}</td>
                    <td className="py-2 px-3">{l.department}</td>
                    <td className="py-2 px-3">{l.coursesTaught}</td>
                    <td className="py-2 px-3">{l.sentimentScore}</td>
                    <td className="py-2 px-3">{l.avgConcentration}</td>
                    <td className="py-2 px-3">{l.avgAttendance}</td>
                    <td className="py-2 px-3">
                      <span className={clsx("px-2 py-1 rounded", {
                        "bg-green-100 text-green-800": l.rating === "Excellent",
                        "bg-[#ECECEC] text-[#060817]": l.rating === "Good",
                        "bg-yellow-100 text-yellow-800": l.rating === "Average",
                        "bg-red-100 text-red-800": l.rating === "Needs Improvement",
                      })}>{l.rating}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Course Statistics */}
      <div className={clsx(glassCard, "p-6")}>
        <div className="font-semibold text-lg mb-3">Course Statistics</div>
        {loading ? (
          <Skeleton className="w-full h-32" />
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full text-sm">
              <thead>
                <tr className="text-xs font-bold text-left bg-white/30">
                  <th className="py-2 px-3">Code</th>
                  <th className="py-2 px-3">Title</th>
                  <th className="py-2 px-3">Lecturer</th>
                  <th className="py-2 px-3">Enrolled</th>
                  <th className="py-2 px-3">Sessions</th>
                  <th className="py-2 px-3">Engagement</th>
                  <th className="py-2 px-3">Attendance</th>
                </tr>
              </thead>
              <tbody>
                {courses.map((c, i) => (
                  <tr key={i} className="border-b border-gray-200/40 hover:bg-gray-50/40">
                    <td className="py-2 px-3">{c.code}</td>
                    <td className="py-2 px-3">{c.title}</td>
                    <td className="py-2 px-3">{c.lecturer}</td>
                    <td className="py-2 px-3">{c.enrolledStudents}</td>
                    <td className="py-2 px-3">{c.sessionCount}</td>
                    <td className="py-2 px-3">{formatEngagement(c.avgEngagement)}</td>
                    <td className="py-2 px-3">{formatEngagement(c.avgAttendance)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}