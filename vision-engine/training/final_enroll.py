# final_enroll.py - Complete solution for face enrollment
import sys
sys.path.insert(0, r'C:\Users\john\Desktop\eduvision\vision-engine')

import cv2
import numpy as np
import mysql.connector
from pathlib import Path
from deepface import DeepFace
import time
import re

DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': '',
    'database': 'eduvision'
}

FACE_ENROLLMENT_DIR = Path("C:/Users/john/Desktop/eduvision/face_enrollment")

def get_all_students():
    """Get all students from database"""
    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor(dictionary=True)
    cursor.execute("""
        SELECT s.user_id, u.first_name, u.last_name, s.student_number, 
               s.face_encoding, s.program
        FROM students s
        JOIN users u ON u.id = s.user_id
        ORDER BY u.last_name
    """)
    students = cursor.fetchall()
    cursor.close()
    conn.close()
    return students

def extract_embedding_from_photo(photo_path):
    """Extract Facenet embedding (128-d) from photo"""
    try:
        img = cv2.imread(str(photo_path))
        if img is None:
            return None
        
        # Try multiple times with different settings for difficult photos
        for attempt in range(2):
            try:
                result = DeepFace.represent(
                    img_path=img,
                    model_name="Facenet",
                    enforce_detection=(attempt == 0),  # First attempt: require face
                    detector_backend="opencv",
                    align=True
                )
                
                if result and len(result) > 0:
                    embedding = np.array(result[0]["embedding"], dtype=np.float32)
                    # L2 normalize
                    embedding = embedding / (np.linalg.norm(embedding) + 1e-9)
                    return embedding
            except:
                continue
                
    except Exception as e:
        print(f"      Error: {str(e)[:80]}")
    
    return None

def update_embedding_in_db(user_id, embedding):
    """Store embedding in database"""
    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor()
    embedding_bytes = embedding.tobytes()
    cursor.execute("UPDATE students SET face_encoding = %s WHERE user_id = %s", 
                   (embedding_bytes, user_id))
    conn.commit()
    cursor.close()
    conn.close()

def match_student_by_name(folder_name, students):
    """
    Try to match folder numeric ID to student by checking if the number
    appears in any student record or by approximate name matching
    """
    folder_num = str(folder_name)
    
    # First, check if any student_number or user_id contains this number
    for student in students:
        # Check if folder number is in student_number
        if student.get('student_number') and folder_num in student['student_number']:
            return student
        # Check if folder number matches user_id (if user_id is numeric)
        if str(student['user_id']).isdigit() and str(student['user_id']) == folder_num:
            return student
    
    return None

def enroll_from_folders():
    """Main enrollment function"""
    print("=" * 80)
    print("🎯 FACE ENROLLMENT SYSTEM - Processing all folders")
    print("=" * 80)
    
    # Get all students
    students = get_all_students()
    print(f"\n📊 Database: {len(students)} students found")
    
    # Get all folders
    if not FACE_ENROLLMENT_DIR.exists():
        print(f"❌ Folder not found: {FACE_ENROLLMENT_DIR}")
        return
    
    folders = [d for d in FACE_ENROLLMENT_DIR.iterdir() if d.is_dir()]
    print(f"📁 Found {len(folders)} folders in face_enrollment/")
    
    # Separate into numeric and non-numeric
    numeric_folders = [f for f in folders if f.name.isdigit()]
    print(f"📁 Numeric folders: {len(numeric_folders)}")
    
    success_list = []
    failed_list = []
    no_match_list = []
    
    for idx, folder in enumerate(numeric_folders, 1):
        folder_name = folder.name
        print(f"\n[{idx}/{len(numeric_folders)}] Processing: {folder_name}")
        
        # Find matching student
        student = match_student_by_name(folder_name, students)
        
        if not student:
            print(f"   ⚠️ No direct match found for {folder_name}")
            no_match_list.append(folder_name)
            continue
        
        # Find photo file
        photo_files = list(folder.glob("*.jpg")) + list(folder.glob("*.png")) + list(folder.glob("*.jpeg"))
        if not photo_files:
            print(f"   ❌ No photo found in folder")
            failed_list.append((folder_name, "No photo file"))
            continue
        
        photo_path = photo_files[0]
        print(f"   📷 Photo: {photo_path.name}")
        print(f"   👤 Student: {student['first_name']} {student['last_name']}")
        print(f"   🆔 Student Number: {student['student_number']}")
        
        # Extract embedding
        print(f"   🔄 Extracting embedding...")
        embedding = extract_embedding_from_photo(photo_path)
        
        if embedding is None:
            print(f"   ❌ Failed to extract embedding (no face detected)")
            failed_list.append((folder_name, student['first_name'] + " " + student['last_name'], "No face detected"))
            continue
        
        if len(embedding) != 128:
            print(f"   ❌ Wrong embedding dimension: {len(embedding)}")
            failed_list.append((folder_name, student['first_name'] + " " + student['last_name'], "Wrong dimension"))
            continue
        
        # Update database
        update_embedding_in_db(student['user_id'], embedding)
        print(f"   ✅ SUCCESS! 128-d embedding stored (norm: {np.linalg.norm(embedding):.3f})")
        success_list.append((student['user_id'], student['first_name'] + " " + student['last_name'], folder_name))
        
        # Small delay
        time.sleep(0.1)
    
    # Print summary
    print("\n" + "=" * 80)
    print("📈 ENROLLMENT SUMMARY")
    print("=" * 80)
    print(f"✅ Successfully enrolled: {len(success_list)} students")
    print(f"❌ Failed (no face): {len(failed_list)} students")
    print(f"⚠️ No database match: {len(no_match_list)} folders")
    
    if success_list:
        print("\n✅ ENROLLED STUDENTS:")
        for user_id, name, folder in success_list:
            print(f"   • {name} (Folder: {folder})")
    
    if failed_list:
        print("\n❌ FAILED (no face detected in photo):")
        for folder, name, reason in failed_list:
            print(f"   • Folder {folder} - {name}")
    
    if no_match_list:
        print("\n⚠️ FOLDERS WITH NO DATABASE MATCH:")
        for folder in no_match_list[:20]:
            print(f"   • {folder}")
        if len(no_match_list) > 20:
            print(f"   ... and {len(no_match_list) - 20} more")
    
    print("\n" + "=" * 80)
    
    # Return success list for manual mapping
    return success_list, failed_list, no_match_list

def create_mapping_for_unmatched(folders):
    """Create SQL to manually map unmatched folders"""
    print("\n📝 To manually map the unmatched folders, run this SQL query:")
    print("-- First, find which students these numeric IDs belong to")
    print("-- You need to check your student records to map them")
    
    print("\n-- Example SQL to update a student with correct embedding:")
    print("-- UPDATE students SET face_encoding = (SELECT embedding FROM temp_table WHERE folder_id = '231002467') WHERE student_number = 'STU-XXXXXX';")
    
    # Create a lookup file
    with open("unmatched_folders.txt", "w") as f:
        f.write("Numeric folders without database matches:\n")
        for folder in folders:
            f.write(f"{folder}\n")
    
    print(f"\n💾 List saved to: unmatched_folders.txt")

if __name__ == "__main__":
    success, failed, no_match = enroll_from_folders()
    
    if no_match:
        create_mapping_for_unmatched(no_match)
        print("\n🔍 Next steps for unmatched folders:")
        print("1. Check which students correspond to these numeric IDs")
        print("2. We'll create a manual mapping script")
        print("3. Or extract embeddings and map manually")