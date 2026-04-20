-- =============================================================================
-- EduVision: Classroom Emotion & Concentration Detection System
-- MySQL 8.0+ Schema (XAMPP compatible)
-- =============================================================================

CREATE DATABASE IF NOT EXISTS eduvision CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE eduvision;

SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================================
-- SECTION 1 — RBAC FOUNDATION
-- =============================================================================

CREATE TABLE permissions (
    id            CHAR(36)      PRIMARY KEY DEFAULT (UUID()),
    name          VARCHAR(100)  NOT NULL UNIQUE,
    resource      VARCHAR(60)   NOT NULL,
    action        VARCHAR(40)   NOT NULL,
    description   TEXT,
    is_active     TINYINT(1)    NOT NULL DEFAULT 1,
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_permission_resource_action UNIQUE (resource, action)
) ENGINE=InnoDB;

CREATE TABLE roles (
    id            CHAR(36)      PRIMARY KEY DEFAULT (UUID()),
    name          VARCHAR(60)   NOT NULL UNIQUE,
    description   TEXT,
    is_system     TINYINT(1)    NOT NULL DEFAULT 0,
    is_active     TINYINT(1)    NOT NULL DEFAULT 1,
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE role_permissions (
    role_id       CHAR(36)      NOT NULL,
    permission_id CHAR(36)      NOT NULL,
    granted_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    granted_by    CHAR(36),
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id)       REFERENCES roles(id)       ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =============================================================================
-- SECTION 2 — USERS
-- =============================================================================

CREATE TABLE users (
    id                  CHAR(36)      PRIMARY KEY DEFAULT (UUID()),
    email               VARCHAR(255)  NOT NULL UNIQUE,
    password_hash       TEXT          NOT NULL,
    first_name          VARCHAR(100)  NOT NULL,
    last_name           VARCHAR(100)  NOT NULL,
    display_name        VARCHAR(150),
    gender              ENUM('male','female','other','prefer_not_to_say'),
    date_of_birth       DATE,
    phone_number        VARCHAR(30),
    profile_picture_url TEXT,
    locale              VARCHAR(10)   NOT NULL DEFAULT 'en',
    timezone            VARCHAR(60)   NOT NULL DEFAULT 'UTC',
    status              ENUM('active','inactive','suspended','pending_verification') NOT NULL DEFAULT 'pending_verification',
    last_login_at       DATETIME,
    failed_login_count  SMALLINT      NOT NULL DEFAULT 0,
    locked_until        DATETIME,
    created_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at          DATETIME
) ENGINE=InnoDB;

-- Back-fill FK on role_permissions.granted_by
ALTER TABLE role_permissions
    ADD CONSTRAINT fk_rp_granted_by FOREIGN KEY (granted_by) REFERENCES users(id);

CREATE TABLE user_roles (
    user_id     CHAR(36)  NOT NULL,
    role_id     CHAR(36)  NOT NULL,
    assigned_at DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by CHAR(36),
    expires_at  DATETIME,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id)     REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id)     REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(id)
) ENGINE=InnoDB;

-- =============================================================================
-- SECTION 3 — ROLE-SPECIFIC PROFILES (LSP sub-types)
-- =============================================================================

CREATE TABLE admins (
    user_id          CHAR(36)     PRIMARY KEY,
    department       VARCHAR(150),
    access_level     TINYINT      NOT NULL DEFAULT 1,
    can_manage_roles TINYINT(1)   NOT NULL DEFAULT 0,
    notes            TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_admin_access_level CHECK (access_level BETWEEN 1 AND 5)
) ENGINE=InnoDB;

CREATE TABLE lecturers (
    user_id         CHAR(36)      PRIMARY KEY,
    employee_id     VARCHAR(60)   UNIQUE,
    department      VARCHAR(150),
    specialization  VARCHAR(200),
    office_location VARCHAR(200),
    bio             TEXT,
    hired_at        DATE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE students (
    user_id         CHAR(36)      PRIMARY KEY,
    student_number  VARCHAR(60)   UNIQUE NOT NULL,
    program         VARCHAR(200),
    year_of_study   TINYINT,
    gpa             DECIMAL(3,2),
    enrolled_at     DATE,
    expected_grad   DATE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_year_of_study CHECK (year_of_study BETWEEN 1 AND 10)
) ENGINE=InnoDB;

-- =============================================================================
-- SECTION 4 — COURSES & SESSIONS
-- =============================================================================

CREATE TABLE courses (
    id            CHAR(36)      PRIMARY KEY DEFAULT (UUID()),
    code          VARCHAR(30)   NOT NULL UNIQUE,
    title         VARCHAR(255)  NOT NULL,
    description   TEXT,
    credit_hours  TINYINT       NOT NULL DEFAULT 3,
    department    VARCHAR(150),
    semester      VARCHAR(30),
    academic_year VARCHAR(10),
    is_active     TINYINT(1)    NOT NULL DEFAULT 1,
    created_by    CHAR(36)      NOT NULL,
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE course_lecturers (
    course_id   CHAR(36)  NOT NULL,
    lecturer_id CHAR(36)  NOT NULL,
    is_primary  TINYINT(1) NOT NULL DEFAULT 0,
    assigned_at DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (course_id, lecturer_id),
    FOREIGN KEY (course_id)   REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (lecturer_id) REFERENCES users(id)   ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE course_students (
    course_id   CHAR(36)  NOT NULL,
    student_id  CHAR(36)  NOT NULL,
    enrolled_at DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dropped_at  DATETIME,
    PRIMARY KEY (course_id, student_id),
    FOREIGN KEY (course_id)  REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id)   ON DELETE CASCADE
) ENGINE=InnoDB;

-- SINGLETON PATTERN: lecture_session_registry
-- One active session per course at any time (DB-level enforcement)
CREATE TABLE lecture_sessions (
    id               CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
    course_id        CHAR(36)     NOT NULL,
    lecturer_id      CHAR(36)     NOT NULL,
    title            VARCHAR(255),
    description      TEXT,
    scheduled_start  DATETIME     NOT NULL,
    scheduled_end    DATETIME     NOT NULL,
    actual_start     DATETIME,
    actual_end       DATETIME,
    status           ENUM('scheduled','active','paused','completed','cancelled') NOT NULL DEFAULT 'scheduled',
    room_location    VARCHAR(200),
    session_metadata JSON,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id)   REFERENCES courses(id),
    FOREIGN KEY (lecturer_id) REFERENCES users(id),
    CONSTRAINT chk_session_times CHECK (scheduled_end > scheduled_start)
) ENGINE=InnoDB;

CREATE TABLE lecture_session_registry (
    id                  CHAR(36)  PRIMARY KEY DEFAULT (UUID()),
    course_id           CHAR(36)  NOT NULL UNIQUE,
    active_session_id   CHAR(36),
    last_activated_at   DATETIME,
    last_deactivated_at DATETIME,
    registry_metadata   JSON,
    FOREIGN KEY (course_id)         REFERENCES courses(id),
    FOREIGN KEY (active_session_id) REFERENCES lecture_sessions(id)
) ENGINE=InnoDB;

-- =============================================================================
-- SECTION 5 — ATTENDANCE
-- =============================================================================

CREATE TABLE session_attendance (
    id            CHAR(36)  PRIMARY KEY DEFAULT (UUID()),
    session_id    CHAR(36)  NOT NULL,
    student_id    CHAR(36)  NOT NULL,
    status        ENUM('present','late','absent','excused') NOT NULL DEFAULT 'absent',
    joined_at     DATETIME,
    left_at       DATETIME,
    notes         TEXT,
    recorded_at   DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_attendance_session_student (session_id, student_id),
    FOREIGN KEY (session_id) REFERENCES lecture_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- =============================================================================
-- SECTION 6 — CAMERA CONFIGURATIONS (Abstract Factory Pattern)
-- factory_type is the discriminator CameraConfigurationFactory reads
-- =============================================================================

CREATE TABLE camera_configurations (
    id               CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
    name             VARCHAR(150) NOT NULL,
    factory_type     ENUM('usb','ip','mobile','virtual') NOT NULL,
    device_index     SMALLINT,
    stream_url       TEXT,
    rtsp_username    VARCHAR(100),
    rtsp_password_enc TEXT,
    device_token     TEXT,
    resolution_w     SMALLINT     NOT NULL DEFAULT 640,
    resolution_h     SMALLINT     NOT NULL DEFAULT 480,
    fps              TINYINT      NOT NULL DEFAULT 15,
    extra_config     JSON,
    is_active        TINYINT(1)   NOT NULL DEFAULT 1,
    created_by       CHAR(36)     NOT NULL,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE session_cameras (
    session_id  CHAR(36)  NOT NULL,
    camera_id   CHAR(36)  NOT NULL,
    assigned_at DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (session_id, camera_id),
    FOREIGN KEY (session_id) REFERENCES lecture_sessions(id)      ON DELETE CASCADE,
    FOREIGN KEY (camera_id)  REFERENCES camera_configurations(id)
) ENGINE=InnoDB;

-- =============================================================================
-- SECTION 7 — EMOTION SNAPSHOTS (Iterator Pattern)
-- seq_index gives SentimentHistoryBuffer its ordered cursor position
-- =============================================================================

CREATE TABLE emotion_snapshots (
    id                CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
    session_id        CHAR(36)     NOT NULL,
    camera_id         CHAR(36),
    seq_index         BIGINT       NOT NULL,
    captured_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    frame_url         TEXT,
    student_count     SMALLINT     NOT NULL DEFAULT 0,
    avg_concentration DECIMAL(4,3),
    dominant_emotion  ENUM('happy','sad','angry','surprised','fearful','disgusted','neutral','confused','engaged'),
    engagement_score  DECIMAL(4,3),
    raw_payload       JSON,
    processing_ms     INT,
    UNIQUE KEY uq_snapshot_session_seq (session_id, seq_index),
    FOREIGN KEY (session_id) REFERENCES lecture_sessions(id)      ON DELETE CASCADE,
    FOREIGN KEY (camera_id)  REFERENCES camera_configurations(id),
    CONSTRAINT chk_engagement_score CHECK (engagement_score BETWEEN 0 AND 1)
) ENGINE=InnoDB;

CREATE INDEX idx_emotion_snapshots_session_seq ON emotion_snapshots (session_id, seq_index ASC);

CREATE TABLE student_emotion_snapshots (
    id               CHAR(36)    PRIMARY KEY DEFAULT (UUID()),
    snapshot_id      CHAR(36)    NOT NULL,
    student_id       CHAR(36)    NOT NULL,
    session_id       CHAR(36)    NOT NULL,
    face_embedding   BLOB,
    emotion          ENUM('happy','sad','angry','surprised','fearful','disgusted','neutral','confused','engaged') NOT NULL,
    concentration    ENUM('high','medium','low','distracted') NOT NULL,
    confidence_score DECIMAL(4,3) NOT NULL,
    bounding_box     JSON,
    gaze_direction   JSON,
    captured_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_anonymised    TINYINT(1)  NOT NULL DEFAULT 0,
    FOREIGN KEY (snapshot_id) REFERENCES emotion_snapshots(id)   ON DELETE CASCADE,
    FOREIGN KEY (student_id)  REFERENCES users(id),
    FOREIGN KEY (session_id)  REFERENCES lecture_sessions(id),
    CONSTRAINT chk_confidence CHECK (confidence_score BETWEEN 0 AND 1)
) ENGINE=InnoDB;

CREATE INDEX idx_student_emotion_session ON student_emotion_snapshots (session_id, student_id, captured_at DESC);

-- =============================================================================
-- SECTION 8 — SUMMARIES
-- =============================================================================

CREATE TABLE student_lecture_summaries (
    id                  CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
    student_id          CHAR(36)     NOT NULL,
    session_id          CHAR(36)     NOT NULL,
    course_id           CHAR(36)     NOT NULL,
    pct_happy           DECIMAL(4,3),
    pct_sad             DECIMAL(4,3),
    pct_angry           DECIMAL(4,3),
    pct_confused        DECIMAL(4,3),
    pct_neutral         DECIMAL(4,3),
    pct_engaged         DECIMAL(4,3),
    pct_high_conc       DECIMAL(4,3),
    pct_med_conc        DECIMAL(4,3),
    pct_low_conc        DECIMAL(4,3),
    pct_distracted      DECIMAL(4,3),
    overall_engagement  DECIMAL(4,3),
    attention_score     DECIMAL(4,3),
    participation_score DECIMAL(4,3),
    r_analysis_json     JSON,
    snapshot_count      INT          NOT NULL DEFAULT 0,
    generated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_summary_student_session (student_id, session_id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (session_id) REFERENCES lecture_sessions(id),
    FOREIGN KEY (course_id)  REFERENCES courses(id)
) ENGINE=InnoDB;

-- =============================================================================
-- SECTION 9 — STRATEGY PATTERN
-- New strategies = INSERT rows, not schema changes (OCP)
-- =============================================================================

CREATE TABLE strategies (
    id            CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
    name          VARCHAR(100) NOT NULL UNIQUE,
    type          ENUM('alert','authorization','privacy') NOT NULL,
    handler_class VARCHAR(255) NOT NULL,
    config        JSON,
    is_default    TINYINT(1)   NOT NULL DEFAULT 0,
    is_active     TINYINT(1)   NOT NULL DEFAULT 1,
    description   TEXT,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE UNIQUE INDEX uq_default_strategy_per_type ON strategies (type, is_default, is_active);

-- =============================================================================
-- SECTION 10 — ALERTS & NOTIFICATIONS (Observer Pattern)
-- =============================================================================

CREATE TABLE alerts (
    id              CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
    session_id      CHAR(36)     NOT NULL,
    course_id       CHAR(36)     NOT NULL,
    triggered_by    CHAR(36),
    strategy_id     CHAR(36),
    severity        ENUM('info','warning','critical') NOT NULL DEFAULT 'info',
    status          ENUM('open','acknowledged','resolved','dismissed') NOT NULL DEFAULT 'open',
    title           VARCHAR(255) NOT NULL,
    message         TEXT         NOT NULL,
    snapshot_id     CHAR(36),
    threshold_value DECIMAL(5,3),
    actual_value    DECIMAL(5,3),
    alert_metadata  JSON,
    triggered_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at DATETIME,
    acknowledged_by CHAR(36),
    resolved_at     DATETIME,
    resolved_by     CHAR(36),
    FOREIGN KEY (session_id)      REFERENCES lecture_sessions(id),
    FOREIGN KEY (course_id)       REFERENCES courses(id),
    FOREIGN KEY (triggered_by)    REFERENCES users(id),
    FOREIGN KEY (strategy_id)     REFERENCES strategies(id),
    FOREIGN KEY (snapshot_id)     REFERENCES emotion_snapshots(id),
    FOREIGN KEY (acknowledged_by) REFERENCES users(id),
    FOREIGN KEY (resolved_by)     REFERENCES users(id)
) ENGINE=InnoDB;

CREATE INDEX idx_alerts_session_status ON alerts (session_id, status, triggered_at DESC);

CREATE TABLE notifications (
    id                CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
    alert_id          CHAR(36),
    recipient_id      CHAR(36)     NOT NULL,
    channel           ENUM('in_app','email','push','sms') NOT NULL DEFAULT 'in_app',
    status            ENUM('pending','sent','delivered','failed','read') NOT NULL DEFAULT 'pending',
    subject           VARCHAR(255),
    body              TEXT         NOT NULL,
    observer_class    VARCHAR(255),
    delivery_attempts SMALLINT     NOT NULL DEFAULT 0,
    last_attempt_at   DATETIME,
    delivered_at      DATETIME,
    read_at           DATETIME,
    metadata          JSON,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (alert_id)     REFERENCES alerts(id) ON DELETE SET NULL,
    FOREIGN KEY (recipient_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE INDEX idx_notifications_recipient_status ON notifications (recipient_id, status, created_at DESC);

-- =============================================================================
-- SECTION 11 — PRIVACY (Privacy Strategy Pattern)
-- =============================================================================

CREATE TABLE privacy_policies (
    id                    CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
    strategy_id           CHAR(36),
    name                  VARCHAR(150) NOT NULL,
    version               VARCHAR(20)  NOT NULL,
    store_face_data       TINYINT(1)   NOT NULL DEFAULT 0,
    store_frame_urls      TINYINT(1)   NOT NULL DEFAULT 0,
    anonymise_after_days  INT          NOT NULL DEFAULT 30,
    retention_days        INT          NOT NULL DEFAULT 90,
    policy_text           TEXT,
    is_active             TINYINT(1)   NOT NULL DEFAULT 1,
    effective_from        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (strategy_id) REFERENCES strategies(id)
) ENGINE=InnoDB;

CREATE TABLE consent_log (
    id           CHAR(36)  PRIMARY KEY DEFAULT (UUID()),
    student_id   CHAR(36)  NOT NULL,
    policy_id    CHAR(36)  NOT NULL,
    status       ENUM('granted','revoked','pending') NOT NULL DEFAULT 'pending',
    consented_at DATETIME,
    revoked_at   DATETIME,
    ip_address   VARCHAR(45),
    user_agent   TEXT,
    created_at   DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_consent_student_policy (student_id, policy_id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (policy_id)  REFERENCES privacy_policies(id)
) ENGINE=InnoDB;

-- =============================================================================
-- SECTION 12 — REPORTS
-- =============================================================================

CREATE TABLE reports (
    id               CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
    type             ENUM('session_summary','student_progress','course_analytics','engagement_trend','custom') NOT NULL,
    status           ENUM('pending','generating','ready','failed') NOT NULL DEFAULT 'pending',
    title            VARCHAR(255) NOT NULL,
    description      TEXT,
    course_id        CHAR(36),
    session_id       CHAR(36),
    student_id       CHAR(36),
    date_from        DATETIME,
    date_to          DATETIME,
    file_url         TEXT,
    file_size_bytes  BIGINT,
    r_script_used    TEXT,
    result_metadata  JSON,
    requested_by     CHAR(36)     NOT NULL,
    requested_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at     DATETIME,
    FOREIGN KEY (course_id)    REFERENCES courses(id),
    FOREIGN KEY (session_id)   REFERENCES lecture_sessions(id),
    FOREIGN KEY (student_id)   REFERENCES users(id),
    FOREIGN KEY (requested_by) REFERENCES users(id)
) ENGINE=InnoDB;

-- =============================================================================
-- SECTION 13 — AUDIT LOGS (Immutable — no updates/deletes)
-- =============================================================================

CREATE TABLE audit_logs (
    id            BIGINT       PRIMARY KEY AUTO_INCREMENT,
    user_id       CHAR(36),
    session_id    CHAR(36),
    action        ENUM('create','read','update','delete','login','logout','export','alert_triggered') NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id   CHAR(36),
    old_value     JSON,
    new_value     JSON,
    ip_address    VARCHAR(45),
    user_agent    TEXT,
    request_id    CHAR(36),
    duration_ms   INT,
    success       TINYINT(1)   NOT NULL DEFAULT 1,
    error_message TEXT,
    occurred_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)    REFERENCES users(id)             ON DELETE SET NULL,
    FOREIGN KEY (session_id) REFERENCES lecture_sessions(id)  ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_audit_logs_user_time   ON audit_logs (user_id, occurred_at DESC);
CREATE INDEX idx_audit_logs_resource    ON audit_logs (resource_type, resource_id, occurred_at DESC);
CREATE INDEX idx_audit_logs_action_time ON audit_logs (action, occurred_at DESC);

-- =============================================================================
-- SECTION 14 — FACADE VIEW (DashboardFacadeController reads this)
-- =============================================================================

CREATE OR REPLACE VIEW dashboard_session_view AS
SELECT
    ls.id                                           AS session_id,
    ls.course_id,
    c.code                                          AS course_code,
    c.title                                         AS course_title,
    ls.lecturer_id,
    CONCAT(u.first_name, ' ', u.last_name)         AS lecturer_name,
    ls.status                                       AS session_status,
    ls.scheduled_start,
    ls.actual_start,
    ls.actual_end,
    COUNT(DISTINCT sa.student_id)                   AS students_present,
    es.avg_concentration,
    es.dominant_emotion,
    es.engagement_score,
    es.captured_at                                  AS last_snapshot_at,
    COUNT(DISTINCT CASE WHEN a.status = 'open' THEN a.id END) AS open_alerts,
    NOW()                                           AS refreshed_at
FROM lecture_sessions ls
JOIN courses c                 ON c.id = ls.course_id
JOIN users u                   ON u.id = ls.lecturer_id
LEFT JOIN session_attendance sa ON sa.session_id = ls.id AND sa.status = 'present'
LEFT JOIN emotion_snapshots es
    ON es.session_id = ls.id
    AND es.seq_index = (
        SELECT MAX(seq_index) FROM emotion_snapshots WHERE session_id = ls.id
    )
LEFT JOIN alerts a             ON a.session_id = ls.id
GROUP BY
    ls.id, c.code, c.title, ls.lecturer_id,
    u.first_name, u.last_name,
    es.avg_concentration, es.dominant_emotion,
    es.engagement_score, es.captured_at;

-- =============================================================================
-- SECTION 15 — SINGLETON GUARD STORED PROCEDURE
-- =============================================================================

DELIMITER $$

CREATE PROCEDURE sp_activate_session(
    IN p_course_id  CHAR(36),
    IN p_session_id CHAR(36)
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
        SET MESSAGE_TEXT = 'Singleton violation: course already has an active session';
    END IF;

    UPDATE lecture_sessions
       SET status = 'active', actual_start = NOW()
     WHERE id = p_session_id;

    INSERT INTO lecture_session_registry (course_id, active_session_id, last_activated_at)
        VALUES (p_course_id, p_session_id, NOW())
    ON DUPLICATE KEY UPDATE
        active_session_id = VALUES(active_session_id),
        last_activated_at = VALUES(last_activated_at);
END$$

DELIMITER ;

-- =============================================================================
-- SECTION 16 — SEED DATA
-- =============================================================================

INSERT INTO roles (id, name, description, is_system) VALUES
    (UUID(), 'admin',    'Full system access',          1),
    (UUID(), 'lecturer', 'Manage own courses/sessions', 1),
    (UUID(), 'student',  'View own data only',          1);

INSERT INTO permissions (id, name, resource, action, description) VALUES
    (UUID(), 'session:start',   'session', 'start',    'Start a lecture session'),
    (UUID(), 'session:stop',    'session', 'stop',     'Stop a lecture session'),
    (UUID(), 'session:view',    'session', 'view',     'View session details'),
    (UUID(), 'emotion:view',    'emotion', 'view',     'View emotion analytics'),
    (UUID(), 'emotion:export',  'emotion', 'export',   'Export emotion data'),
    (UUID(), 'report:generate', 'report',  'generate', 'Generate reports'),
    (UUID(), 'report:view',     'report',  'view',     'View reports'),
    (UUID(), 'user:manage',     'user',    'manage',   'Create/edit/delete users'),
    (UUID(), 'role:assign',     'role',    'assign',   'Assign roles to users'),
    (UUID(), 'alert:manage',    'alert',   'manage',   'Acknowledge/resolve alerts'),
    (UUID(), 'alert:view',      'alert',   'view',     'View alerts'),
    (UUID(), 'consent:manage',  'consent', 'manage',   'Manage student consent');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'admin'
UNION ALL
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'lecturer'
  AND p.name IN ('session:start','session:stop','session:view','emotion:view','emotion:export','report:generate','report:view','alert:manage','alert:view')
UNION ALL
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'student'
  AND p.name IN ('session:view','report:view','alert:view');

INSERT INTO strategies (id, name, type, handler_class, is_default, config, description) VALUES
    (UUID(), 'ThresholdAlertStrategy',   'alert',
     'com.eduvision.alert.strategy.ThresholdAlertStrategy',   1,
     '{"low_engagement_threshold":0.4,"distraction_threshold":0.6,"window_seconds":60}',
     'Fires alert when engagement drops below threshold'),
    (UUID(), 'RBACAuthorizationStrategy','authorization',
     'com.eduvision.auth.strategy.RBACAuthorizationStrategy', 1,
     '{"cache_ttl_seconds":300}',
     'Role-based access control'),
    (UUID(), 'GDPRPrivacyStrategy',      'privacy',
     'com.eduvision.privacy.strategy.GDPRPrivacyStrategy',    1,
     '{"anonymise_on_revoke":true,"purge_raw_frames":true}',
     'GDPR-compliant privacy defaults');

INSERT INTO privacy_policies (id, name, version, store_face_data, store_frame_urls, anonymise_after_days, retention_days)
VALUES (UUID(), 'Default GDPR Policy', '1.0', 0, 0, 30, 90);

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- END OF SCHEMA
-- =============================================================================
