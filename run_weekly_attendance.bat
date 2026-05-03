:: C:\Users\john\Desktop\eduvision\run_weekly_attendance.bat

@echo off
echo ========================================
echo EduVision Weekly Attendance Calculator
echo %date% %time%
echo ========================================

cd C:\Users\john\Desktop\eduvision\vision-engine
.venv\Scripts\activate
python scripts\trigger_weekly_attendance.py

echo Completed at %date% %time%
echo ----------------------------------------