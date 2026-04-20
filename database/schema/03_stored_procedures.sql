-- Stored procedures 
USE eduvision;

DELIMITER $$

CREATE PROCEDURE sp_activate_session(
    IN p_course_id   CHAR(36),
    IN p_session_id  CHAR(36)
)
BEGIN
    DECLARE v_count INT DEFAULT 0;

    SELECT COUNT(*) INTO v_count
    FROM lecture_sessions
    WHERE course_id = p_course_id
      AND status    = 'active'
      AND id       <> p_session_id;

    IF v_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Cannot activate session: course already has an active session running';
    END IF;

    UPDATE lecture_sessions
       SET status = 'active', actual_start = NOW()
     WHERE id = p_session_id;

    INSERT INTO lecture_session_registry (course_id, active_session_id, last_activated_at)
        VALUES (p_course_id, p_session_id, NOW())
    ON DUPLICATE KEY UPDATE
        active_session_id   = p_session_id,
        last_activated_at   = NOW();
END$$

DELIMITER ;