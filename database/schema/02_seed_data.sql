-- Seed data 
USE eduvision;

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
SELECT r.id, p.id
FROM roles r
JOIN permissions p
WHERE r.name = 'admin'
UNION ALL
SELECT r.id, p.id
FROM roles r
JOIN permissions p
WHERE r.name = 'lecturer'
  AND p.name IN ('session:start','session:stop','session:view','emotion:view','emotion:export','report:generate','report:view','alert:manage','alert:view')
UNION ALL
SELECT r.id, p.id
FROM roles r
JOIN permissions p
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