# C:\Users\john\Desktop\eduvision\vision-engine\training\check_mapping.py
import mysql.connector
from pathlib import Path

DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': '',
    'database': 'eduvision'
}

def get_all_students():
    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor(dictionary=True)
    cursor.execute("""
        SELECT s.user_id, u.first_name, u.last_name, s.student_code
        FROM students s
        JOIN users u ON u.id = s.user_id
        ORDER BY s.user_id
    """)
    students = cursor.fetchall()
    cursor.close()
    conn.close()
    return students

def main():
    print("=" * 70)
    print("📁 Checking face_enrollment folders vs Database")
    print("=" * 70)
    
    # Get all numeric folders
    enrollment_dir = Path("C:/Users/john/Desktop/eduvision/face_enrollment")
    numeric_folders = [d.name for d in enrollment_dir.iterdir() if d.is_dir() and d.name.isdigit()]
    
    print(f"\n📸 Found {len(numeric_folders)} numeric folders: {sorted(numeric_folders)[:10]}...")
    
    # Get all students
    students = get_all_students()
    
    print(f"\n👨‍🎓 Found {len(students)} students in database")
    
    # Check if any student_code or user_id matches the numeric folders
    print("\n🔍 Looking for matches...")
    matches = []
    
    for student in students:
        user_id = student['user_id']
        student_code = student.get('student_code', '')
        
        # Check if user_id or student_code is purely numeric and matches a folder
        for folder in numeric_folders:
            if str(user_id) == folder or str(student_code) == folder:
                matches.append({
                    'folder_id': folder,
                    'user_id': user_id,
                    'name': f"{student['first_name']} {student['last_name']}",
                    'student_code': student_code
                })
                break
    
    print(f"\n✅ Found {len(matches)} matches!")
    
    for m in matches:
        print(f"   Folder {m['folder_id']} → {m['name']} (ID: {m['user_id']})")
    
    # Also show numeric folders without matches
    matched_folders = [m['folder_id'] for m in matches]
    unmatched = [f for f in numeric_folders if f not in matched_folders]
    
    if unmatched:
        print(f"\n⚠️ {len(unmatched)} numeric folders with no database match:")
        for f in unmatched[:10]:
            print(f"   - {f}")
    
    print("\n" + "=" * 70)

if __name__ == "__main__":
    main()