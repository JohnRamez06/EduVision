# C:\Users\john\Desktop\eduvision\vision-engine\debug_embeddings.py
import sys
sys.path.insert(0, r'C:\Users\john\Desktop\eduvision\vision-engine')

import mysql.connector
import numpy as np

DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': '',
    'database': 'eduvision'
}

def check_all_embeddings():
    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor(dictionary=True)
    
    cursor.execute("""
        SELECT s.user_id, u.first_name, u.last_name, 
               LENGTH(s.face_encoding) as encoding_size
        FROM students s
        JOIN users u ON u.id = s.user_id
        WHERE s.face_encoding IS NOT NULL
    """)
    
    students = cursor.fetchall()
    cursor.close()
    conn.close()
    
    print("=" * 80)
    print("📊 DATABASE EMBEDDING CHECK")
    print("=" * 80)
    
    valid_128d = []
    invalid_size = []
    
    for student in students:
        size = student['encoding_size']
        name = f"{student['first_name']} {student['last_name']}"
        
        if size == 512:  # 128 floats * 4 bytes = 512 bytes
            valid_128d.append((name, size))
        else:
            invalid_size.append((name, size, student['user_id']))
    
    print(f"\n✅ Valid 128-d embeddings (512 bytes): {len(valid_128d)} students")
    print(f"❌ Invalid size embeddings: {len(invalid_size)} students")
    
    if invalid_size:
        print("\n❌ Students with wrong embedding size:")
        for name, size, user_id in invalid_size[:10]:
            print(f"   • {name}: {size} bytes (expected 512)")
        
        # Check one invalid embedding
        print("\n🔍 Checking first invalid embedding...")
        cursor = conn.cursor()
        cursor.execute("""
            SELECT face_encoding FROM students 
            WHERE user_id = %s
        """, (invalid_size[0][2],))
        blob = cursor.fetchone()[0]
        
        if blob:
            arr = np.frombuffer(blob, dtype=np.float32)
            print(f"   Array shape: {arr.shape}")
            print(f"   First 5 values: {arr[:5]}")
            print(f"   Min/Max: {arr.min():.3f}/{arr.max():.3f}")
        
        cursor.close()
    
    conn.close()
    return valid_128d, invalid_size

if __name__ == "__main__":
    valid, invalid = check_all_embeddings()