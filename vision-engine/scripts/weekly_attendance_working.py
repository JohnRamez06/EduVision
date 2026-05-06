"""
Weekly Attendance Calculator - Working with your exact database schema
"""

import mysql.connector
from datetime import datetime, timedelta
from typing import Tuple, Dict
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': '',
    'database': 'eduvision'
}

class WeeklyAttendanceCalculator:
    
    def __init__(self):
        self.conn = None
        
    def get_connection(self):
        if not self.conn or not self.conn.is_connected():
            self.conn = mysql.connector.connect(**DB_CONFIG)
        return self.conn
    
    def get_current_week_range(self) -> Tuple[datetime, datetime, int, int]:
        """Get Monday to Sunday of current week"""
        today = datetime.now()
        monday = today - timedelta(days=today.weekday())
        monday = monday.replace(hour=0, minute=0, second=0, microsecond=0)
        sunday = monday + timedelta(days=6, hours=23, minutes=59, seconds=59)
        week_number = monday.isocalendar()[1]
        year = monday.year
        return monday, sunday, week_number, year
    
    def get_or_create_week_period(self, week_start: datetime, week_number: int, year: int) -> str:
        """Get or create weekly_period record, return period_id"""
        conn = self.get_connection()
        cursor = conn.cursor(dictionary=True)
        
        week_end = week_start + timedelta(days=6)
        
        cursor.execute("""
            SELECT id FROM weekly_periods 
            WHERE week_number = %s AND year = %s
        """, (week_number, year))
        
        result = cursor.fetchone()
        
        if result:
            period_id = result['id']
        else:
            cursor.execute("""
                INSERT INTO weekly_periods (id, week_number, year, start_date, end_date)
                VALUES (UUID(), %s, %s, %s, %s)
            """, (week_number, year, week_start.date(), week_end.date()))
            conn.commit()
            period_id = cursor.lastrowid
        
        cursor.close()
        return period_id
    
    def calculate_student_weekly_attendance(self, student_id: str, course_id: str, 
                                            lecturer_id: str, week_start: datetime, 
                                            week_end: datetime) -> Dict:
        """Calculate if student attended at least ONE session this week"""
        conn = self.get_connection()
        cursor = conn.cursor(dictionary=True)
        
        cursor.execute("""
            SELECT ls.id as session_id, ls.scheduled_start as start_time, 
                   sa.id as attendance_id, sa.status as attendance_status
            FROM lecture_sessions ls
            LEFT JOIN session_attendance sa ON sa.session_id = ls.id AND sa.student_id = %s
            WHERE ls.course_id = %s 
              AND ls.lecturer_id = %s
              AND ls.scheduled_start BETWEEN %s AND %s
              AND ls.status = 'completed'
            ORDER BY ls.scheduled_start
        """, (student_id, course_id, lecturer_id, week_start, week_end))
        
        sessions = cursor.fetchall()
        cursor.close()
        
        # Count attended sessions
        attended_sessions = [s for s in sessions if s['attendance_status'] and s['attendance_status'] != 'absent']
        has_attended = len(attended_sessions) > 0
        has_sessions = len(sessions) > 0
        
        # Calculate attendance rate
        attendance_rate = 0.0
        if has_sessions:
            attendance_rate = (len(attended_sessions) / len(sessions)) * 100
        
        return {
            'has_attended': has_attended,
            'has_sessions': has_sessions,
            'sessions_attended': len(attended_sessions),
            'total_sessions': len(sessions),
            'attendance_rate': attendance_rate,
            'first_attendance': attended_sessions[0]['start_time'] if attended_sessions else None
        }
    
    def update_weekly_attendance(self, period_id: str, student_id: str, course_id: str,
                                  sessions_held: int, sessions_attended: int, 
                                  attendance_rate: float, status: str):
        """Update or insert weekly attendance record"""
        conn = self.get_connection()
        cursor = conn.cursor()
        
        sessions_missed = sessions_held - sessions_attended
        
        # Check if record exists
        cursor.execute("""
            SELECT id FROM weekly_course_attendance 
            WHERE week_id = %s AND student_id = %s AND course_id = %s
        """, (period_id, student_id, course_id))
        
        existing = cursor.fetchone()
        
        if existing:
            cursor.execute("""
                UPDATE weekly_course_attendance 
                SET sessions_held = %s, sessions_attended = %s, sessions_missed = %s,
                    attendance_rate = %s, status = %s, last_updated = NOW()
                WHERE id = %s
            """, (sessions_held, sessions_attended, sessions_missed, 
                  attendance_rate, status, existing[0]))
        else:
            cursor.execute("""
                INSERT INTO weekly_course_attendance 
                (id, week_id, student_id, course_id, sessions_held, 
                 sessions_attended, sessions_missed, attendance_rate, status, last_updated)
                VALUES (UUID(), %s, %s, %s, %s, %s, %s, %s, %s, NOW())
            """, (period_id, student_id, course_id, sessions_held, 
                  sessions_attended, sessions_missed, attendance_rate, status))
        
        conn.commit()
        cursor.close()
    
    def run_weekly_calculation(self, week_start: datetime = None):
        """Main function to calculate weekly attendance"""
        
        if not week_start:
            week_start, week_end, week_number, year = self.get_current_week_range()
        else:
            week_end = week_start + timedelta(days=6, hours=23, minutes=59, seconds=59)
            week_number = week_start.isocalendar()[1]
            year = week_start.year
        
        logger.info(f"📊 Calculating weekly attendance for week: {week_start.date()} to {week_end.date()}")
        logger.info(f"📅 Week {week_number}, {year}")
        
        # Get or create week period
        period_id = self.get_or_create_week_period(week_start, week_number, year)
        logger.info(f"📅 Week period ID: {period_id}")
        
        conn = self.get_connection()
        cursor = conn.cursor(dictionary=True)
        
        # Get all student-course-lecturer assignments
        cursor.execute("""
            SELECT cs.student_id, cs.course_id, cs.lecturer_id
            FROM course_students cs
            WHERE cs.dropped_at IS NULL
        """)
        
        assignments = cursor.fetchall()
        cursor.close()
        
        if not assignments:
            logger.warning("No assignments found")
            return
        
        logger.info(f"👥 Processing {len(assignments)} student-course assignments")
        
        stats = {
            'total': len(assignments),
            'present': 0,
            'absent': 0,
            'excused': 0
        }
        
        for idx, assignment in enumerate(assignments, 1):
            if idx % 50 == 0:
                logger.info(f"Progress: {idx}/{len(assignments)}")
            
            result = self.calculate_student_weekly_attendance(
                assignment['student_id'],
                assignment['course_id'],
                assignment['lecturer_id'],
                week_start,
                week_end
            )
            
            # Determine status
            if not result['has_sessions']:
                db_status = 'regular'  # No sessions = excused
                stats['excused'] += 1
            elif result['has_attended']:
                db_status = 'regular'  # Present
                stats['present'] += 1
            else:
                db_status = 'absent'   # Absent
                stats['absent'] += 1
            
            self.update_weekly_attendance(
                period_id,
                assignment['student_id'],
                assignment['course_id'],
                result['total_sessions'],
                result['sessions_attended'],
                result['attendance_rate'],
                db_status
            )
        
        logger.info(f"✅ Weekly calculation complete!")
        logger.info(f"   Present/Excused: {stats['present'] + stats['excused']} students")
        logger.info(f"   Absent: {stats['absent']} students")


if __name__ == "__main__":
    calculator = WeeklyAttendanceCalculator()
    calculator.run_weekly_calculation()