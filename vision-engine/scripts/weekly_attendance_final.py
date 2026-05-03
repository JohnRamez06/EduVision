# C:\Users\john\Desktop\eduvision\vision-engine\scripts\weekly_attendance_final.py
"""
Weekly Attendance Calculator - Matches your exact database schema
"""

import mysql.connector
from datetime import datetime, timedelta
from typing import Tuple, Dict, Optional
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
    
    def get_current_week_range(self) -> Tuple[datetime, datetime]:
        """Get Monday to Sunday of current week"""
        today = datetime.now()
        monday = today - timedelta(days=today.weekday())
        monday = monday.replace(hour=0, minute=0, second=0, microsecond=0)
        sunday = monday + timedelta(days=6, hours=23, minutes=59, seconds=59)
        return monday, sunday
    
    def get_week_id(self, week_start: datetime) -> str:
        """Generate week ID in format YYYY-WW"""
        return week_start.strftime('%Y-W%W')
    
    def get_or_create_week_period(self, week_start: datetime) -> str:
        """Get or create week_period record and return week_id"""
        conn = self.get_connection()
        cursor = conn.cursor(dictionary=True)
        
        week_end = week_start + timedelta(days=6)
        week_name = f"Week of {week_start.strftime('%Y-%m-%d')}"
        week_id = self.get_week_id(week_start)
        
        # Check if week_id exists in weekly_periods
        cursor.execute("""
            SELECT id, week_id FROM weekly_periods 
            WHERE week_id = %s
        """, (week_id,))
        
        result = cursor.fetchone()
        
        if not result:
            cursor.execute("""
                INSERT INTO weekly_periods (id, week_id, week_name, start_date, end_date, is_closed)
                VALUES (UUID(), %s, %s, %s, %s, %s)
            """, (week_id, week_name, week_start, week_end, False))
            conn.commit()
        
        cursor.close()
        return week_id
    
    def calculate_student_weekly_attendance(self, student_id: str, course_id: str, 
                                            week_start: datetime, week_end: datetime) -> Dict:
        """Calculate if student attended at least ONE session this week"""
        conn = self.get_connection()
        cursor = conn.cursor(dictionary=True)
        
        # Get all sessions for this course in the week
        cursor.execute("""
            SELECT ls.id as session_id, ls.scheduled_start as start_time, 
                   sa.id as attendance_id, sa.status as attendance_status
            FROM lecture_sessions ls
            LEFT JOIN session_attendance sa ON sa.session_id = ls.id AND sa.student_id = %s
            WHERE ls.course_id = %s 
              AND ls.scheduled_start BETWEEN %s AND %s
              AND ls.status = 'completed'
            ORDER BY ls.scheduled_start
        """, (student_id, course_id, week_start, week_end))
        
        sessions = cursor.fetchall()
        cursor.close()
        
        # Check if student attended any session (status not 'absent')
        attended_sessions = [s for s in sessions if s['attendance_status'] and s['attendance_status'] != 'absent']
        has_attended = len(attended_sessions) > 0
        has_sessions = len(sessions) > 0
        
        # Calculate attendance rate
        attendance_rate = 0.0
        if has_sessions:
            attendance_rate = (len(attended_sessions) / len(sessions)) * 100
        
        # Determine status
        if not has_sessions:
            week_status = 'excused'  # We'll map this
        elif has_attended:
            week_status = 'regular'
        else:
            week_status = 'absent'
        
        return {
            'has_attended': has_attended,
            'has_sessions': has_sessions,
            'sessions_attended': len(attended_sessions),
            'total_sessions': len(sessions),
            'attendance_rate': attendance_rate,
            'week_status': week_status,
            'first_attendance': attended_sessions[0]['start_time'] if attended_sessions else None
        }
    
    def update_weekly_attendance(self, week_id: str, student_id: str, course_id: str,
                                  sessions_held: int, sessions_attended: int, 
                                  attendance_rate: float, week_status: str):
        """Update or insert weekly course attendance record"""
        conn = self.get_connection()
        cursor = conn.cursor()
        
        # Map week_status to status enum
        status_map = {
            'regular': 'regular',
            'absent': 'absent',
            'excused': 'regular'  # excused counts as regular attendance
        }
        status = status_map.get(week_status, 'regular')
        
        sessions_missed = sessions_held - sessions_attended
        
        # Check if record exists
        cursor.execute("""
            SELECT id FROM weekly_course_attendance 
            WHERE week_id = %s AND student_id = %s AND course_id = %s
        """, (week_id, student_id, course_id))
        
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
                (id, week_id, student_id, course_id, sessions_held, sessions_attended, 
                 sessions_missed, attendance_rate, status, last_updated)
                VALUES (UUID(), %s, %s, %s, %s, %s, %s, %s, %s, NOW())
            """, (week_id, student_id, course_id, sessions_held, sessions_attended, 
                  sessions_missed, attendance_rate, status))
        
        conn.commit()
        cursor.close()
    
    def create_absence_alert(self, student_id: str, course_id: str, 
                              lecturer_id: str, consecutive_weeks: int):
        """Create alert for student with consecutive absences"""
        conn = self.get_connection()
        cursor = conn.cursor()
        
        # Check if alert already exists
        cursor.execute("""
            SELECT id FROM alerts 
            WHERE student_id = %s AND course_id = %s 
              AND alert_type = 'consecutive_absence' 
              AND status IN ('pending', 'acknowledged')
        """, (student_id, course_id))
        
        if not cursor.fetchone():
            cursor.execute("""
                INSERT INTO alerts 
                (id, student_id, course_id, lecturer_id, alert_type, severity, 
                 message, status, created_at)
                VALUES (UUID(), %s, %s, %s, %s, %s, %s, %s, NOW())
            """, (student_id, course_id, lecturer_id, 'consecutive_absence', 'high',
                  f"Student has been absent for {consecutive_weeks} consecutive weeks", 
                  'pending'))
            conn.commit()
            logger.warning(f"⚠️ Alert created for student {student_id} in course {course_id}")
        
        cursor.close()
    
    def get_previous_weeks_status(self, student_id: str, course_id: str, current_week_id: str) -> list:
        """Get attendance status from previous weeks"""
        conn = self.get_connection()
        cursor = conn.cursor(dictionary=True)
        
        cursor.execute("""
            SELECT week_id, status 
            FROM weekly_course_attendance 
            WHERE student_id = %s AND course_id = %s AND week_id != %s
            ORDER BY last_updated DESC
            LIMIT 3
        """, (student_id, course_id, current_week_id))
        
        results = cursor.fetchall()
        cursor.close()
        return results
    
    def run_weekly_calculation(self, week_start: datetime = None):
        """Main function to calculate weekly attendance for all students"""
        
        if not week_start:
            week_start, week_end = self.get_current_week_range()
        else:
            week_end = week_start + timedelta(days=6, hours=23, minutes=59, seconds=59)
        
        logger.info(f"📊 Calculating weekly attendance for week: {week_start.date()} to {week_end.date()}")
        
        # Get or create week period
        week_id = self.get_or_create_week_period(week_start)
        logger.info(f"📅 Week ID: {week_id}")
        
        conn = self.get_connection()
        cursor = conn.cursor(dictionary=True)
        
        # Get all active student-course assignments with lecturer info
        cursor.execute("""
            SELECT cs.student_id, cs.course_id, 
                   cl.lecturer_id
            FROM course_students cs
            JOIN course_lecturers cl ON cl.course_id = cs.course_id AND cl.is_primary = 1
            WHERE cs.dropped_at IS NULL
        """)
        
        assignments = cursor.fetchall()
        cursor.close()
        
        if not assignments:
            logger.warning("No active course_students assignments found!")
            return
        
        logger.info(f"👥 Processing {len(assignments)} student-course assignments")
        
        stats = {
            'total': len(assignments),
            'present': 0,
            'absent': 0,
            'excused': 0,
            'alerts_created': 0
        }
        
        for idx, assignment in enumerate(assignments, 1):
            if idx % 50 == 0:
                logger.info(f"Progress: {idx}/{len(assignments)}")
            
            result = self.calculate_student_weekly_attendance(
                assignment['student_id'],
                assignment['course_id'],
                week_start,
                week_end
            )
            
            # Update weekly attendance record
            self.update_weekly_attendance(
                week_id,
                assignment['student_id'],
                assignment['course_id'],
                result['total_sessions'],
                result['sessions_attended'],
                result['attendance_rate'],
                result['week_status']
            )
            
            # Update stats
            if not result['has_sessions']:
                stats['excused'] += 1
            elif result['has_attended']:
                stats['present'] += 1
            else:
                stats['absent'] += 1
                
                # Check for consecutive absences and create alerts
                if result['week_status'] == 'absent':
                    prev_weeks = self.get_previous_weeks_status(
                        assignment['student_id'], 
                        assignment['course_id'],
                        week_id
                    )
                    
                    # Count consecutive absences
                    consecutive = 1  # current week
                    for week in prev_weeks:
                        if week['status'] == 'absent':
                            consecutive += 1
                        else:
                            break
                    
                    # Create alert if 2 or more consecutive weeks absent
                    if consecutive >= 2:
                        self.create_absence_alert(
                            assignment['student_id'],
                            assignment['course_id'],
                            assignment['lecturer_id'],
                            consecutive
                        )
                        stats['alerts_created'] += 1
        
        logger.info(f"✅ Weekly calculation complete!")
        logger.info(f"   Present: {stats['present']} students")
        logger.info(f"   Absent: {stats['absent']} students")
        logger.info(f"   Excused (no sessions): {stats['excused']} students")
        logger.info(f"   Alerts created: {stats['alerts_created']}")
        
        # Close the week period
        cursor = conn.cursor()
        cursor.execute("UPDATE weekly_periods SET is_closed = TRUE WHERE week_id = %s", (week_id,))
        conn.commit()
        cursor.close()


def run_weekly_report():
    """Generate weekly report after calculation"""
    calculator = WeeklyAttendanceCalculator()
    calculator.run_weekly_calculation()


if __name__ == "__main__":
    run_weekly_report()