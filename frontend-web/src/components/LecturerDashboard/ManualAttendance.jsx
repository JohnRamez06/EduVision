import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Save, ArrowLeft, Calendar, Users } from 'lucide-react';
import LecturerLayout from '../../layouts/LecturerLayout';
import attendanceService from '../../services/attendanceService';

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

  // Log courseId for debugging
  console.log('Course ID from params:', courseId);

  useEffect(() => {
    if (courseId) {
      fetchWeeks();
    } else {
      setError('Course ID not found');
      setLoading(false);
    }
  }, [courseId]);

  useEffect(() => {
    if (selectedWeek && courseId) {
      fetchStudents();
    }
  }, [selectedWeek, courseId]);

  const fetchWeeks = async () => {
    try {
      setLoading(true);
      const data = await attendanceService.getAvailableWeeks(courseId);
      console.log('Weeks data:', data);
      
      // Handle both array and object responses
      let weeksArray = Array.isArray(data) ? data : data?.weeks || [];
      setWeeks(weeksArray);
      
      // Auto-select current or most recent week
      const targetWeek = weeksArray.find(w => w.period_status === 'current') || weeksArray[0];
      if (targetWeek) setSelectedWeek(targetWeek.week_id);
    } catch (error) {
      console.error('Error fetching weeks:', error);
      setError('Failed to load weeks');
    } finally {
      setLoading(false);
    }
  };

  const fetchStudents = async () => {
    setLoading(true);
    try {
      const data = await attendanceService.getStudentsForManualAttendance(courseId, selectedWeek);
      console.log('Students data:', data);
      
      // Handle both array and object responses
      let studentsArray = Array.isArray(data) ? data : data?.students || [];
      
      // 🔥 FIX 1: Map backend status 'regular' to frontend 'present'
      const mappedStudents = studentsArray.map(student => ({
        ...student,
        autoStatus: student.autoStatus === 'regular' ? 'present' : (student.autoStatus || 'absent'),
        manualStatus: student.manualStatus === 'regular' ? 'present' : (student.manualStatus || null),
        finalStatus: student.finalStatus === 'regular' ? 'present' : (student.finalStatus || 'absent')
      }));
      
      setStudents(mappedStudents);
      
      if (mappedStudents.length > 0 && mappedStudents[0].courseName) {
        setCourseName(mappedStudents[0].courseName);
      }
    } catch (error) {
      console.error('Error fetching students:', error);
      setError('Failed to load students');
    }
    setLoading(false);
  };

  const updateStudentStatus = (studentId, status, notes) => {
    setStudents(prev => prev.map(s =>
      s.studentId === studentId
        ? { ...s, manualStatus: status, notes, finalStatus: status, isManuallyModified: true }
        : s
    ));
  };

  const saveAttendance = async () => {
    const modifiedStudents = students.filter(s => s.isManuallyModified);
    
    if (modifiedStudents.length === 0) {
      alert('No changes to save');
      return;
    }

    setSaving(true);
    
    // 🔥 FIX 2: Convert 'present' back to 'regular' for backend
    const payload = {
      courseId,
      weekId: selectedWeek,
      students: modifiedStudents.map(s => ({
        studentId: s.studentId,
        status: s.manualStatus === 'present' ? 'regular' : s.manualStatus,
        notes: s.notes || ''
      }))
    };

    try {
      await attendanceService.saveManualAttendance(payload);
      alert('Attendance saved successfully!');
      fetchStudents(); // Refresh
    } catch (error) {
      console.error('Error saving:', error);
      alert('Failed to save attendance');
    }
    setSaving(false);
  };

  // 🔥 FIX 3: Updated getStatusBadge to handle 'regular' and 'present'
  const getStatusBadge = (student) => {
    const status = student.finalStatus;
    const isManual = student.isManuallyModified;
    
    // Handle both 'regular' (backend) and 'present' (frontend)
    if (status === 'present' || status === 'regular') {
      return <span className={`px-2 py-1 rounded-full text-xs ${isManual ? 'bg-purple-100 text-purple-800' : 'bg-green-100 text-green-800'}`}>
        {isManual ? '✓ Manual Present' : '✓ Present'}
      </span>;
    } else if (status === 'excused') {
      return <span className="px-2 py-1 rounded-full text-xs bg-blue-100 text-blue-800">📝 Excused</span>;
    } else {
      return <span className="px-2 py-1 rounded-full text-xs bg-red-100 text-red-800">✗ Absent</span>;
    }
  };

  const selectedWeekData = weeks.find(w => w.week_id === selectedWeek);

  if (error) {
    return (
      <LecturerLayout>
        <div className="text-center py-12 text-red-400">
          <p>{error}</p>
          <button
            onClick={() => navigate('/lecturer/courses')}
            className="mt-4 px-4 py-2 bg-purple-600 rounded-lg text-white"
          >
            Back to Courses
          </button>
        </div>
      </LecturerLayout>
    );
  }

  return (
    <LecturerLayout>
      <div className="mb-6">
        <button
          onClick={() => navigate('/lecturer/courses')}
          className="flex items-center gap-2 text-slate-400 hover:text-white mb-4 transition"
        >
          <ArrowLeft size={16} /> Back to Courses
        </button>
        <h1 className="text-2xl font-bold text-white">📋 Manual Attendance</h1>
        <p className="text-sm text-slate-500 mt-1">
          {courseName || 'Course'} - Override attendance for students
        </p>
      </div>

      {/* Week Selector */}
      <div className="bg-slate-800/50 rounded-xl p-4 mb-6">
        <div className="flex items-center gap-3 flex-wrap">
          <div className="flex items-center gap-2">
            <Calendar size={18} className="text-slate-400" />
            <span className="text-sm text-slate-300">Select Week:</span>
          </div>
          <select
            value={selectedWeek}
            onChange={(e) => setSelectedWeek(e.target.value)}
            className="px-3 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white text-sm"
            disabled={weeks.length === 0}
          >
            {weeks.map(week => (
              <option key={week.week_id} value={week.week_id}>
                Week {week.week_number}, {week.year} ({week.start_date} to {week.end_date})
                {week.period_status === 'current' && ' (Current)'}
              </option>
            ))}
          </select>
          {selectedWeekData && (
            <span className="text-xs text-slate-500">
              Week {selectedWeekData.week_number}: {selectedWeekData.start_date} to {selectedWeekData.end_date}
            </span>
          )}
        </div>
      </div>

      {/* Students Table */}
      {loading ? (
        <div className="text-center py-12 text-slate-500">Loading students...</div>
      ) : (
        <>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-slate-800/50 rounded-xl">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-400">Student</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-400">Auto Status</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-400">Manual Status</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-400">Attendance Rate</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-400">Notes</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-700">
                {students.length === 0 ? (
                  <tr>
                    <td colSpan="5" className="px-4 py-8 text-center text-slate-500">
                      No students found for this course
                    </td>
                  </tr>
                ) : (
                  students.map(student => (
                    <tr key={student.studentId} className="hover:bg-slate-800/30">
                      <td className="px-4 py-3">
                        <div className="font-medium text-white">{student.studentName}</div>
                        <div className="text-xs text-slate-500">{student.studentNumber}</div>
                      </td>
                      <td className="px-4 py-3">
                        {getStatusBadge({ ...student, isManuallyModified: false, finalStatus: student.autoStatus })}
                        <div className="text-xs text-slate-500 mt-1">
                          {student.sessionsAttended}/{student.totalSessions} sessions
                        </div>
                       </td>
                      <td className="px-4 py-3">
                        <select
                          value={student.manualStatus || student.autoStatus || 'absent'}
                          onChange={(e) => updateStudentStatus(student.studentId, e.target.value, student.notes)}
                          className="px-2 py-1 bg-slate-700 border border-slate-600 rounded text-white text-sm"
                        >
                          <option value="present">✓ Present</option>
                          <option value="absent">✗ Absent</option>
                          <option value="excused">📝 Excused</option>
                        </select>
                        {student.isManuallyModified && (
                          <span className="ml-2 text-xs text-purple-400">(Override)</span>
                        )}
                       </td>
                      <td className="px-4 py-3 text-white">
                        {student.attendanceRate ? `${student.attendanceRate}%` : 'N/A'}
                       </td>
                      <td className="px-4 py-3">
                        <input
                          type="text"
                          value={student.notes || ''}
                          onChange={(e) => updateStudentStatus(student.studentId, student.manualStatus || student.autoStatus || 'absent', e.target.value)}
                          placeholder="Add note..."
                          className="w-full px-2 py-1 bg-slate-700 border border-slate-600 rounded text-white text-sm placeholder-slate-500"
                        />
                       </td>
                     </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          {/* Save Button */}
          {students.length > 0 && (
            <div className="mt-6 flex justify-end">
              <button
                onClick={saveAttendance}
                disabled={saving}
                className="flex items-center gap-2 px-4 py-2 bg-purple-600 hover:bg-purple-700 disabled:opacity-50 text-white rounded-lg transition"
              >
                <Save size={16} />
                {saving ? 'Saving...' : 'Save Manual Attendance'}
              </button>
            </div>
          )}

          {/* Info Box */}
          <div className="mt-4 p-3 bg-blue-900/30 border border-blue-500/30 rounded-lg text-sm text-blue-300">
            <strong>💡 Note:</strong> Manual attendance overrides automatic detection.
            Purple badges indicate manually modified records. Excused absences don't count as absent.
          </div>
        </>
      )}
    </LecturerLayout>
  );
}