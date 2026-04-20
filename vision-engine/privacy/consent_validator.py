from database.mysql_connector import get_connection
def has_consent(student_id: str) -> bool:
    con = get_connection()
    cursor = con.cursor()
    cursor.execute("SELECT status FROM consent_log WHERE student_id=s ORDER BY created_at DESC LIMIT 1", (student_id,))
    row = cursor.fetchone()
    return row is not None and row[0] == 'granted'
