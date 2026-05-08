import React, { useState, useEffect, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Save, ArrowLeft, Calendar, Search, Users, CheckCircle, XCircle, Clock } from 'lucide-react';
import LecturerLayout from '../../layouts/LecturerLayout';
import attendanceService from '../../services/attendanceService';

// ─── Status config ────────────────────────────────────────────────────────────
const STATUS = {
  present: {
    label: 'P',
    full: 'Present',
    ring: 'ring-emerald-500',
    bg: 'bg-emerald-500',
    text: 'text-emerald-400',
    badge: 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30',
  },
  absent: {
    label: 'A',
    full: 'Absent',
    ring: 'ring-red-500',
    bg: 'bg-red-500',
    text: 'text-red-400',
    badge: 'bg-red-500/20 text-red-300 border-red-500/30',
  },
  late: {
    label: 'L',
    full: 'Late',
    ring: 'ring-amber-500',
    bg: 'bg-amber-500',
    text: 'text-amber-400',
    badge: 'bg-amber-500/20 text-amber-300 border-amber-500/30',
  },
};

// ─── Avatar ────────────────────────────────────────────────────────────────────
function Avatar({ src, name, status }) {
  const [imgFailed, setImgFailed] = useState(false);

  const initials = name
    ? name.split(' ').map(n => n[0]).slice(0, 2).join('').toUpperCase()
    : '?';

  const ringClass = STATUS[status]?.ring ?? 'ring-slate-600';
  const showPhoto = src && !imgFailed;

  return (
    <div className={`relative w-20 h-20 mx-auto rounded-full ring-2 ${ringClass} ring-offset-2 ring-offset-slate-800 overflow-hidden`}>
      {showPhoto ? (
        <img
          src={src}
          alt={name}
          className="w-full h-full object-cover"
          onError={() => setImgFailed(true)}
        />
      ) : (
        <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-[#1e3a6e] to-[#0f1f3d] text-white font-bold text-lg">
          {initials}
        </div>
      )}
    </div>
  );
}

// ─── PAL Toggle ───────────────────────────────────────────────────────────────
function PALToggle({ current, onChange }) {
  return (
    <div className="flex gap-1 mt-3 justify-center">
      {['present', 'absent', 'late'].map(s => {
        const cfg = STATUS[s];
        const active = current === s;
        return (
          <button
            key={s}
            onClick={() => onChange(s)}
            className={`w-9 h-9 rounded-full text-xs font-bold transition-all duration-150 border
              ${active
                ? `${cfg.bg} border-transparent text-white shadow-lg scale-110`
                : 'border-slate-600 text-slate-400 hover:border-slate-400 bg-slate-700/40'
              }`}
            title={cfg.full}
          >
            {cfg.label}
          </button>
        );
      })}
    </div>
  );
}

// ─── Student Card ─────────────────────────────────────────────────────────────
function StudentCard({ student, onStatusChange }) {
  const effectiveStatus = student.pendingStatus ?? student.finalStatus ?? 'absent';
  const isModified = student.isModified;
  const cfg = STATUS[effectiveStatus] ?? STATUS.absent;

  return (
    <div
      className={`relative flex flex-col items-center bg-slate-800/60 backdrop-blur rounded-2xl p-4 border transition-all duration-200
        ${isModified ? 'border-[#2d4a8a] shadow-[0_0_12px_rgba(45,74,138,0.4)]' : 'border-slate-700/50 hover:border-slate-600'}`}
    >
      {/* Modified indicator */}
      {isModified && (
        <span className="absolute top-2 right-2 w-2 h-2 rounded-full bg-blue-400 animate-pulse" title="Unsaved change" />
      )}

      <Avatar src={student.photoUrl} name={student.studentName} status={effectiveStatus} />

      {/* Name + ID */}
      <p className="mt-2 text-sm font-semibold text-white text-center leading-tight line-clamp-2">
        {student.studentName}
      </p>
      <p className="text-xs text-slate-500 mt-0.5">{student.studentNumber}</p>

      {/* Attendance rate pill */}
      <span className={`mt-2 px-2 py-0.5 rounded-full text-xs border ${cfg.badge}`}>
        {effectiveStatus === 'present' ? '✓' : effectiveStatus === 'late' ? '⏱' : '✗'}&nbsp;{cfg.full}
        {student.attendanceRate != null && (
          <span className="ml-1 opacity-70">· {Math.round(student.attendanceRate)}%</span>
        )}
      </span>

      {/* PAL buttons */}
      <PALToggle
        current={effectiveStatus}
        onChange={status => onStatusChange(student.studentId, status)}
      />
    </div>
  );
}

// ─── Main Component ───────────────────────────────────────────────────────────
export default function ManualAttendance() {
  const { courseId } = useParams();
  const navigate = useNavigate();

  const [students, setStudents] = useState([]);
  const [weeks, setWeeks] = useState([]);
  const [selectedWeek, setSelectedWeek] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [courseName, setCourseName] = useState('');
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [filterStatus, setFilterStatus] = useState('all');

  // ── Load weeks ──────────────────────────────────────────────────────────────
  useEffect(() => {
    if (!courseId) { setError('Course ID not found'); setLoading(false); return; }
    fetchWeeks();
  }, [courseId]);

  useEffect(() => {
    if (selectedWeek && courseId) fetchStudents();
  }, [selectedWeek, courseId]);

  const fetchWeeks = async () => {
    try {
      setLoading(true);
      const data = await attendanceService.getAvailableWeeks(courseId);
      const weeksArray = Array.isArray(data) ? data : data?.weeks || [];
      setWeeks(weeksArray);
      const target = weeksArray.find(w => w.period_status === 'current') || weeksArray[0];
      if (target) setSelectedWeek(target.week_id);
    } catch (e) {
      console.error(e);
      setError('Failed to load weeks');
    } finally {
      setLoading(false);
    }
  };

  const fetchStudents = async () => {
    setLoading(true);
    try {
      const data = await attendanceService.getStudentsForManualAttendance(courseId, selectedWeek);
      const raw = Array.isArray(data) ? data : data?.students || [];
      const mapped = raw.map(s => ({
        ...s,
        autoStatus: normaliseStatus(s.autoStatus),
        manualStatus: s.manualStatus ? normaliseStatus(s.manualStatus) : null,
        finalStatus: normaliseStatus(s.finalStatus ?? s.autoStatus ?? 'absent'),
        pendingStatus: null,   // user's unsaved selection
        isModified: false,
        // Build photo URL from enrollment folder via backend endpoint
        photoUrl: s.studentNumber
          ? `http://localhost:8080/api/v1/students/${s.studentNumber}/photo`
          : null,
      }));
      setStudents(mapped);
      if (mapped[0]?.courseName) setCourseName(mapped[0].courseName);
    } catch (e) {
      console.error(e);
      setError('Failed to load students');
    }
    setLoading(false);
  };

  const normaliseStatus = s => {
    if (!s) return 'absent';
    if (s === 'regular') return 'present';
    if (['present', 'absent', 'late', 'excused'].includes(s)) return s;
    return 'absent';
  };

  // ── Update card ─────────────────────────────────────────────────────────────
  const handleStatusChange = (studentId, newStatus) => {
    setStudents(prev => prev.map(s =>
      s.studentId === studentId
        ? { ...s, pendingStatus: newStatus, finalStatus: newStatus, isModified: true }
        : s
    ));
  };

  // ── Save ────────────────────────────────────────────────────────────────────
  const saveAttendance = async () => {
    const modified = students.filter(s => s.isModified);
    if (!modified.length) {
      alert('No changes to save.');
      return;
    }
    setSaving(true);
    const payload = {
      courseId,
      weekId: selectedWeek,
      students: modified.map(s => ({
        studentId: s.studentId,
        status: s.pendingStatus === 'present' ? 'regular' : s.pendingStatus,
        notes: s.notes || '',
      })),
    };
    try {
      await attendanceService.saveManualAttendance(payload);
      alert(`Saved ${modified.length} record(s) ✓`);
      fetchStudents();
    } catch (e) {
      console.error(e);
      alert('Failed to save. Check console.');
    }
    setSaving(false);
  };

  // ── Derived data ─────────────────────────────────────────────────────────────
  const filtered = useMemo(() => {
    return students.filter(s => {
      const nameMatch = s.studentName?.toLowerCase().includes(search.toLowerCase());
      const statusMatch = filterStatus === 'all' || (s.pendingStatus ?? s.finalStatus) === filterStatus;
      return nameMatch && statusMatch;
    });
  }, [students, search, filterStatus]);


  // Summary counts
  const counts = useMemo(() => ({
    present: students.filter(s => (s.pendingStatus ?? s.finalStatus) === 'present').length,
    absent: students.filter(s => (s.pendingStatus ?? s.finalStatus) === 'absent').length,
    late: students.filter(s => (s.pendingStatus ?? s.finalStatus) === 'late').length,
    modified: students.filter(s => s.isModified).length,
  }), [students]);

  // ── Error state ──────────────────────────────────────────────────────────────
  if (error) {
    return (
      <LecturerLayout>
        <div className="text-center py-12 text-red-400">
          <p>{error}</p>
          <button onClick={() => navigate('/lecturer/courses')} className="mt-4 px-4 py-2 bg-[#16254F] rounded-lg text-white">
            Back to Courses
          </button>
        </div>
      </LecturerLayout>
    );
  }

  const selectedWeekData = weeks.find(w => w.week_id === selectedWeek);

  return (
    <LecturerLayout>
      {/* ── Header ── */}
      <div className="mb-6">
        <button
          onClick={() => navigate('/lecturer/courses')}
          className="flex items-center gap-2 text-slate-400 hover:text-white mb-4 transition text-sm"
        >
          <ArrowLeft size={15} /> Back to Courses
        </button>
        <div className="flex items-start justify-between flex-wrap gap-3">
          <div>
            <h1 className="text-2xl font-bold text-white">📋 Manual Attendance</h1>
            <p className="text-sm text-slate-400 mt-1">
              {courseName || 'Course'}
              {selectedWeekData && (
                <span className="ml-2 text-slate-500">
                  · Week {selectedWeekData.week_number}, {selectedWeekData.year}
                </span>
              )}
            </p>
          </div>

          {/* Save button */}
          {students.length > 0 && (
            <button
              onClick={saveAttendance}
              disabled={saving || !counts.modified}
              className={`flex items-center gap-2 px-5 py-2.5 rounded-xl font-semibold text-sm transition-all
                ${counts.modified
                  ? 'bg-[#16254F] hover:bg-[#1e3a6e] text-white shadow-lg'
                  : 'bg-slate-700/40 text-slate-500 cursor-not-allowed'}`}
            >
              <Save size={16} />
              {saving ? 'Saving…' : counts.modified ? `Save ${counts.modified} change${counts.modified !== 1 ? 's' : ''}` : 'No changes'}
            </button>
          )}
        </div>
      </div>

      {/* ── Controls row ── */}
      <div className="bg-slate-800/50 rounded-xl p-4 mb-5 flex flex-wrap gap-3 items-center">
        {/* Week selector */}
        <div className="flex items-center gap-2">
          <Calendar size={16} className="text-slate-400" />
          <select
            value={selectedWeek}
            onChange={e => setSelectedWeek(e.target.value)}
            className="px-3 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white text-sm"
          >
            {weeks.map(w => (
              <option key={w.week_id} value={w.week_id}>
                Week {w.week_number}, {w.year}
                {w.period_status === 'current' && ' (Current)'}
              </option>
            ))}
          </select>
        </div>

        {/* Search */}
        <div className="flex items-center gap-2 bg-slate-700/60 border border-slate-600 rounded-lg px-3 py-2 flex-1 min-w-[180px]">
          <Search size={15} className="text-slate-400 shrink-0" />
          <input
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Search student…"
            className="bg-transparent text-white text-sm outline-none w-full placeholder-slate-500"
          />
        </div>

        {/* Status filter */}
        <div className="flex gap-1.5">
          {['all', 'present', 'absent', 'late'].map(f => (
            <button
              key={f}
              onClick={() => setFilterStatus(f)}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all border
                ${filterStatus === f
                  ? f === 'all'
                    ? 'bg-slate-600 border-slate-500 text-white'
                    : `${STATUS[f]?.bg ?? ''} border-transparent text-white`
                  : 'border-slate-600 text-slate-400 hover:border-slate-500'}`}
            >
              {f === 'all' ? 'All' : STATUS[f].full}
              {f !== 'all' && (
                <span className="ml-1 opacity-75">({counts[f] ?? 0})</span>
              )}
            </button>
          ))}
        </div>
      </div>

      {/* ── Summary pills ── */}
      <div className="flex gap-3 mb-6 flex-wrap">
        {[
          { icon: <Users size={14} />, label: 'Total', value: students.length, color: 'text-slate-300' },
          { icon: <CheckCircle size={14} />, label: 'Present', value: counts.present, color: 'text-emerald-400' },
          { icon: <XCircle size={14} />, label: 'Absent', value: counts.absent, color: 'text-red-400' },
          { icon: <Clock size={14} />, label: 'Late', value: counts.late, color: 'text-amber-400' },
        ].map(pill => (
          <div key={pill.label} className="flex items-center gap-1.5 bg-slate-800/50 border border-slate-700/50 rounded-full px-3 py-1">
            <span className={pill.color}>{pill.icon}</span>
            <span className="text-xs text-slate-400">{pill.label}:</span>
            <span className={`text-sm font-bold ${pill.color}`}>{pill.value}</span>
          </div>
        ))}
      </div>

      {/* ── Content ── */}
      {loading ? (
        <div className="flex flex-col items-center justify-center py-20 gap-3 text-slate-500">
          <div className="w-8 h-8 border-2 border-slate-600 border-t-blue-500 rounded-full animate-spin" />
          <span className="text-sm">Loading students…</span>
        </div>
      ) : students.length === 0 ? (
        <div className="text-center py-16 text-slate-500">
          <Users size={40} className="mx-auto mb-3 opacity-30" />
          <p>No students found for this course</p>
        </div>
      ) : (
        <div>
          <div className="grid grid-cols-6 gap-4">
            {filtered.map(student => (
              <StudentCard
                key={student.studentId}
                student={student}
                onStatusChange={handleStatusChange}
              />
            ))}
          </div>

          {/* Floating save (bottom) */}
          {counts.modified > 0 && (
            <div className="sticky bottom-6 flex justify-center pointer-events-none">
              <button
                onClick={saveAttendance}
                disabled={saving}
                className="pointer-events-auto flex items-center gap-2 px-6 py-3 bg-[#16254F] hover:bg-[#1e3a6e] disabled:opacity-60
                  text-white rounded-2xl font-semibold shadow-2xl shadow-black/40 transition-all text-sm backdrop-blur-sm border border-blue-900/50"
              >
                <Save size={16} />
                {saving ? 'Saving…' : `Save ${counts.modified} change${counts.modified !== 1 ? 's' : ''}`}
              </button>
            </div>
          )}
        </div>
      )}
    </LecturerLayout>
  );
}