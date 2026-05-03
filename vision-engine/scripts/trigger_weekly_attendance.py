# C:\Users\john\Desktop\eduvision\vision-engine\scripts\trigger_weekly_attendance.py

import requests
import datetime

def trigger_weekly_calculation():
    """Call Spring Boot API to calculate weekly attendance"""
    
    url = "http://localhost:8080/api/v1/attendance/weekly/calculate"
    
    try:
        response = requests.post(url, timeout=30)
        
        if response.status_code == 200:
            print(f"✅ Weekly attendance calculated at {datetime.datetime.now()}")
            print(f"   Response: {response.json()}")
        else:
            print(f"❌ Failed: HTTP {response.status_code}")
            print(f"   {response.text}")
            
    except Exception as e:
        print(f"❌ Error calling API: {e}")

if __name__ == "__main__":
    trigger_weekly_calculation()