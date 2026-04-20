source("../config.R")
fetch_session_snapshots <- function(session_id) {
  query_df(sprintf("SELECT * FROM emotion_snapshots WHERE session_id='s' ORDER BY seq_index ASC", session_id))
}
fetch_student_summaries <- function(student_id) {
  query_df(sprintf("SELECT sls.*, ls.scheduled_start FROM student_lecture_summaries sls JOIN lecture_sessions ls ON ls.id=sls.session_id WHERE sls.student_id='s'", student_id))
}
