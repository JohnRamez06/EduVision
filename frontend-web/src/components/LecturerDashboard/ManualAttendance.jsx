import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Save, ArrowLeft, Calendar } from 'lucide-react';
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
      let weeksArray = Array.isArray(data) ? data : data?.weeks || [];
      setWeeks(weeksArray);
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
      let studentsArray = Array.isArray(data) ? data : data?.students || [];
      
      // Map backend data to frontend format
      const mappedStudents = studentsArray.map(student => ({
        ...student,
        autoStatus: student.autoStatus === 'regular' ? 'present' : (student.autoStatus || 'absent'),
        manualStatus: student.manualStatus === 'regular' ? 'present' : (student.manualStatus || null),
        finalStatus: student.finalStatus === 'regular' ? 'present' : (student.finalStatus || 'absent'),
        isManuallyModified: false
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

  // 🔥 FIXED: This function now properly marks the student as modified
  const updateStudentStatus = (studentId, newStatus) => {
    console.log(`Updating student ${studentId} to status: ${newStatus}`);
    
    setStudents(prevStudents => 
      prevStudents.map(student => {
        if (student.studentId === studentId) {
          console.log(`Found student: ${student.studentName}, changing from ${student.autoStatus} to ${newStatus}`);
          return {
            ...student,
            manualStatus: newStatus,
            finalStatus: newStatus,
            isManuallyModified: true  // 🔥 This is the key flag
          };
        }
        return student;
      })
    );
  };

  const saveAttendance = async () => {
    // 🔥 Get all students that were manually modified
    const modifiedStudents = students.filter(s => s.isManuallyModified === true);
    
    console.log('=== MODIFIED STUDENTS ===');
    console.log(`Total modified: ${modifiedStudents.length}`);
    modifiedStudents.forEach(s => {
      console.log(`- ${s.studentName} (${s.studentNumber}): ${s.autoStatus} -> ${s.manualStatus}`);
    });
    
    if (modifiedStudents.length === 0) {
      alert('No changes to save. Please change a student status first.');
      return;
    }

    setSaving(true);
    
    const payload = {
      courseId: courseId,
      weekId: selectedWeek,
      students: modifiedStudents.map(s => ({
        studentId: s.studentId,
        status: s.manualStatus === 'present' ? 'regular' : s.manualStatus,
        notes: s.notes || ''
      }))
    };

    console.log('Sending payload:', payload);

    try {
      await attendanceService.saveManualAttendance(payload);
      alert(`Successfully saved ${modifiedStudents.length} attendance record(s)!`);
      fetchStudents(); // Refresh to show saved status
    } catch (error) {
      console.error('Error saving:', error);
      alert('Failed to save attendance. Check console for details.');
    }
    setSaving(false);
  };

  const getStatusBadge = (status, isManual = false) => {
    if (status === 'present') {
      return <span className={`px-2 py-1 rounded-full text-xs ${isManual ? 'bg-[#16254F] text-white' : 'bg-green-600 text-white'}`}>
        {isManual ? '✓ Manual Present' : '✓ Present'}
      </span>;
    } else if (status === 'excused') {
      return <span className="px-2 py-1 rounded-full text-xs bg-[#16254F] text-white">📝 Excused</span>;
    } else {
      return <span className="px-2 py-1 rounded-full text-xs bg-red-600 text-white">✗ Absent</span>;
    }
  };

  const selectedWeekData = weeks.find(w => w.week_id === selectedWeek);

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

  return (
    <LecturerLayout>
      <div className="mb-6">
        <button onClick={() => navigate('/lecturer/courses')} className="flex items-center gap-2 text-slate-400 hover:text-white mb-4 transition">
          <ArrowLeft size={16} /> Back to Courses
        </button>
        <h1 className="text-2xl font-bold text-white">📋 Manual Attendance</h1>
        <p className="text-sm text-slate-500 mt-1">{courseName || 'Course'} - Override attendance for students</p>
      </div>

      {/* Week Selector */}
      <div className="bg-slate-800/50 rounded-xl p-4 mb-6">
        <div className="flex items-center gap-3 flex-wrap">
          <Calendar size={18} className="text-slate-400" />
          <span className="text-sm text-slate-300">Select Week:</span>
          <select
            value={selectedWeek}
            onChange={(e) => setSelectedWeek(e.target.value)}
            className="px-3 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white text-sm"
          >
            {weeks.map(week => (
              <option key={week.week_id} value={week.week_id}>
                Week {week.week_number}, {week.year} ({week.start_date} to {week.end_date})
                {week.period_status === 'current' && ' (Current)'}
              </option>
            ))}
          </select>
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
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-400">Current Status</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-400">Change To</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-400">Attendance Rate</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-700">
                {students.length === 0 ? (
                  <tr><td colSpan="4" className="px-4 py-8 text-center text-slate-500">No students found</td></tr>
                ) : (
                  students.map(student => (
                    <tr key={student.studentId} className="hover:bg-slate-800/30">
                      <td className="px-4 py-3">
                        <div className="font-medium text-white">{student.studentName}</div>
                        <div className="text-xs text-slate-500">{student.studentNumber}</div>
                        <div className="text-xs text-slate-400 mt-1">{student.sessionsAttended}/{student.totalSessions} sessions</div>
                      </td>
                      <td className="px-4 py-3">
                        {getStatusBadge(student.finalStatus, student.isManuallyModified)}
                      </td>
                      <td className="px-4 py-3">
                        <select
                          value={student.manualStatus || student.autoStatus || 'absent'}
                          onChange={(e) => updateStudentStatus(student.studentId, e.target.value)}
                          className="px-3 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white text-sm"
                        >
                          <option value="present">✓ Mark Present</option>
                          <option value="absent">✗ Mark Absent</option>
                          <option value="excused">📝 Mark Excused</option>
                        </select>
                        {student.isManuallyModified && (
                          <div className="text-xs text-[#667D9D] mt-1">⚠️ Will override current status</div>
                        )}
                      </td>
                      <td className="px-4 py-3 text-white">
                        {student.attendanceRate ? `${student.attendanceRate}%` : 'N/A'}
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
                className="flex items-center gap-2 px-6 py-3 bg-[#16254F] hover:bg-[#16254F] disabled:opacity-50 text-white rounded-lg font-semibold transition"
              >
                <Save size={18} />
                {saving ? 'Saving...' : 'Save Manual Attendance'}
              </button>
            </div>
          )}
        </>
      )}
    </LecturerLayout>
  );
}