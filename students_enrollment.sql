-- EduVision - Student Enrollment SQL
-- Generated automatically

-- Student 1: محمد علاء لطفى (231006367)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0001-0000-0000-0000-000000000001', '231006367@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'محمد', 'علاء لطفى', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0001-0000-0000-0000-000000000001', '231006367', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0001-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0001-0000-0000-0000-000000000001');

-- Student 2: بيشوى مرقس حبيب (231015291)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0002-0000-0000-0000-000000000001', '231015291@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'بيشوى', 'مرقس حبيب', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0002-0000-0000-0000-000000000001', '231015291', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0002-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0002-0000-0000-0000-000000000001');

-- Student 3: مرام تامر عبدالحى (231014184)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0003-0000-0000-0000-000000000001', '231014184@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'مرام', 'تامر عبدالحى', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0003-0000-0000-0000-000000000001', '231014184', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0003-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0003-0000-0000-0000-000000000001');

-- Student 4: رضوى شريف حماد (231014670)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0004-0000-0000-0000-000000000001', '231014670@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'رضوى', 'شريف حماد', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0004-0000-0000-0000-000000000001', '231014670', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0004-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0004-0000-0000-0000-000000000001');

-- Student 5: ندى شريف ابراهيم (231006507)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0005-0000-0000-0000-000000000001', '231006507@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'ندى', 'شريف ابراهيم', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0005-0000-0000-0000-000000000001', '231006507', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0005-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0005-0000-0000-0000-000000000001');

-- Student 6: مريم وائل البورصلى (231005837)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0006-0000-0000-0000-000000000001', '231005837@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'مريم', 'وائل البورصلى', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0006-0000-0000-0000-000000000001', '231005837', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0006-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0006-0000-0000-0000-000000000001');

-- Student 7: حسين هشام فريد (231006798)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0007-0000-0000-0000-000000000001', '231006798@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'حسين', 'هشام فريد', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0007-0000-0000-0000-000000000001', '231006798', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0007-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0007-0000-0000-0000-000000000001');

-- Student 8: فرح ياسر ابراهيم (231004345)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0008-0000-0000-0000-000000000001', '231004345@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'فرح', 'ياسر ابراهيم', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0008-0000-0000-0000-000000000001', '231004345', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0008-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0008-0000-0000-0000-000000000001');

-- Student 9: زينه محمد ابراهيم (231014067)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0009-0000-0000-0000-000000000001', '231014067@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'زينه', 'محمد ابراهيم', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0009-0000-0000-0000-000000000001', '231014067', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0009-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0009-0000-0000-0000-000000000001');

-- Student 10: مريم محمد سالم (231005936)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0010-0000-0000-0000-000000000001', '231005936@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'مريم', 'محمد سالم', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0010-0000-0000-0000-000000000001', '231005936', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0010-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0010-0000-0000-0000-000000000001');

-- Student 11: ماريو رافت عياد (231004779)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0011-0000-0000-0000-000000000001', '231004779@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'ماريو', 'رافت عياد', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0011-0000-0000-0000-000000000001', '231004779', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0011-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0011-0000-0000-0000-000000000001');

-- Student 12: براء ايمن عبدالعظيم (231014972)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0012-0000-0000-0000-000000000001', '231014972@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'براء', 'ايمن عبدالعظيم', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0012-0000-0000-0000-000000000001', '231014972', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0012-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0012-0000-0000-0000-000000000001');

-- Student 13: ندى محمد ابراهيم (231006982)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0013-0000-0000-0000-000000000001', '231006982@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'ندى', 'محمد ابراهيم', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0013-0000-0000-0000-000000000001', '231006982', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0013-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0013-0000-0000-0000-000000000001');

-- Student 14: نور رضا ابوالخير (231006760)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0014-0000-0000-0000-000000000001', '231006760@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'نور', 'رضا ابوالخير', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0014-0000-0000-0000-000000000001', '231006760', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0014-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0014-0000-0000-0000-000000000001');

-- Student 15: معاذ وائل سلام (231005898)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0015-0000-0000-0000-000000000001', '231005898@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'معاذ', 'وائل سلام', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0015-0000-0000-0000-000000000001', '231005898', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0015-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0015-0000-0000-0000-000000000001');

-- Student 16: شهد اسامه سعود (231005756)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0016-0000-0000-0000-000000000001', '231005756@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'شهد', 'اسامه سعود', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0016-0000-0000-0000-000000000001', '231005756', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0016-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0016-0000-0000-0000-000000000001');

-- Student 17: عبدالله خالد عمار (231006916)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0017-0000-0000-0000-000000000001', '231006916@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'عبدالله', 'خالد عمار', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0017-0000-0000-0000-000000000001', '231006916', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0017-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0017-0000-0000-0000-000000000001');

-- Student 18: بلال اشرف حسن (231006688)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0018-0000-0000-0000-000000000001', '231006688@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'بلال', 'اشرف حسن', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0018-0000-0000-0000-000000000001', '231006688', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0018-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0018-0000-0000-0000-000000000001');

-- Student 19: انس مصطفى مكاوى (231006359)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0019-0000-0000-0000-000000000001', '231006359@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'انس', 'مصطفى مكاوى', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0019-0000-0000-0000-000000000001', '231006359', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0019-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0019-0000-0000-0000-000000000001');

-- Student 20: جون ماجد لبيب (231004095)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0020-0000-0000-0000-000000000001', '231004095@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'جون', 'ماجد لبيب', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0020-0000-0000-0000-000000000001', '231004095', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0020-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0020-0000-0000-0000-000000000001');

-- Student 21: عمر خالد يوسف (231005820)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0021-0000-0000-0000-000000000001', '231005820@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'عمر', 'خالد يوسف', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0021-0000-0000-0000-000000000001', '231005820', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0021-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0021-0000-0000-0000-000000000001');

-- Student 22: اروى يحيى سالمه (231006309)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0022-0000-0000-0000-000000000001', '231006309@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'اروى', 'يحيى سالمه', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0022-0000-0000-0000-000000000001', '231006309', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0022-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0022-0000-0000-0000-000000000001');

-- Student 23: محمود عمرو احمد (231006563)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0023-0000-0000-0000-000000000001', '231006563@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'محمود', 'عمرو احمد', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0023-0000-0000-0000-000000000001', '231006563', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0023-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0023-0000-0000-0000-000000000001');

-- Student 24: شيرين احمد حسنين (231002467)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0024-0000-0000-0000-000000000001', '231002467@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'شيرين', 'احمد حسنين', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0024-0000-0000-0000-000000000001', '231002467', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0024-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0024-0000-0000-0000-000000000001');

-- Student 25: ناريمان عادل الازهرى (231007895)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0025-0000-0000-0000-000000000001', '231007895@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'ناريمان', 'عادل الازهرى', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0025-0000-0000-0000-000000000001', '231007895', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0025-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0025-0000-0000-0000-000000000001');

-- Student 26: فريده احمد سليم (231014770)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0026-0000-0000-0000-000000000001', '231014770@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'فريده', 'احمد سليم', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0026-0000-0000-0000-000000000001', '231014770', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0026-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0026-0000-0000-0000-000000000001');

-- Student 27: عمر شريف الادهم (231015308)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0027-0000-0000-0000-000000000001', '231015308@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'عمر', 'شريف الادهم', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0027-0000-0000-0000-000000000001', '231015308', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0027-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0027-0000-0000-0000-000000000001');

-- Student 28: لؤى وليد ابوالمعاطى (231004836)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0028-0000-0000-0000-000000000001', '231004836@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'لؤى', 'وليد ابوالمعاطى', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0028-0000-0000-0000-000000000001', '231004836', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0028-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0028-0000-0000-0000-000000000001');

-- Student 29: ميرا عاطف صالح (231005027)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0029-0000-0000-0000-000000000001', '231005027@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'ميرا', 'عاطف صالح', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0029-0000-0000-0000-000000000001', '231005027', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0029-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0029-0000-0000-0000-000000000001');

-- Student 30: عبدالله محمد شتات (231004160)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0030-0000-0000-0000-000000000001', '231004160@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'عبدالله', 'محمد شتات', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0030-0000-0000-0000-000000000001', '231004160', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0030-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0030-0000-0000-0000-000000000001');

-- Student 31: هنا ايهاب على (231014083)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0031-0000-0000-0000-000000000001', '231014083@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'هنا', 'ايهاب على', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0031-0000-0000-0000-000000000001', '231014083', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0031-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0031-0000-0000-0000-000000000001');

-- Student 32: محمود احمد شلبى (231014373)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0032-0000-0000-0000-000000000001', '231014373@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'محمود', 'احمد شلبى', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0032-0000-0000-0000-000000000001', '231014373', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0032-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0032-0000-0000-0000-000000000001');

-- Student 33: عبدالله عماد حسن (231006822)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0033-0000-0000-0000-000000000001', '231006822@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'عبدالله', 'عماد حسن', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0033-0000-0000-0000-000000000001', '231006822', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0033-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0033-0000-0000-0000-000000000001');

-- Student 34: زياد السيد حسن (231006766)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0034-0000-0000-0000-000000000001', '231006766@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'زياد', 'السيد حسن', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0034-0000-0000-0000-000000000001', '231006766', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0034-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0034-0000-0000-0000-000000000001');

-- Student 35: روان طارق ابوالدهب (231014466)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0035-0000-0000-0000-000000000001', '231014466@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'روان', 'طارق ابوالدهب', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0035-0000-0000-0000-000000000001', '231014466', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0035-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0035-0000-0000-0000-000000000001');

-- Student 36: ادهم هانى اسماعيل (231006844)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0036-0000-0000-0000-000000000001', '231006844@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'ادهم', 'هانى اسماعيل', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0036-0000-0000-0000-000000000001', '231006844', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0036-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0036-0000-0000-0000-000000000001');

-- Student 37: ريم حسين حسن (231004206)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0037-0000-0000-0000-000000000001', '231004206@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'ريم', 'حسين حسن', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0037-0000-0000-0000-000000000001', '231004206', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0037-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0037-0000-0000-0000-000000000001');

-- Student 38: زياد خالد احمد (231006901)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0038-0000-0000-0000-000000000001', '231006901@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'زياد', 'خالد احمد', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0038-0000-0000-0000-000000000001', '231006901', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0038-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0038-0000-0000-0000-000000000001');

-- Student 39: مارك هانى ابادير (231006804)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0039-0000-0000-0000-000000000001', '231006804@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'مارك', 'هانى ابادير', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0039-0000-0000-0000-000000000001', '231006804', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0039-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0039-0000-0000-0000-000000000001');

-- Student 40: عمر علاء الصناديدى (241004978)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0040-0000-0000-0000-000000000001', '241004978@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'عمر', 'علاء الصناديدى', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0040-0000-0000-0000-000000000001', '241004978', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0040-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0040-0000-0000-0000-000000000001');

-- Student 41: يوستينا ممدوح مينا (231014763)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0041-0000-0000-0000-000000000001', '231014763@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'يوستينا', 'ممدوح مينا', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0041-0000-0000-0000-000000000001', '231014763', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0041-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0041-0000-0000-0000-000000000001');

-- Student 42: امال يوسف صالح (231005601)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0042-0000-0000-0000-000000000001', '231005601@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'امال', 'يوسف صالح', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0042-0000-0000-0000-000000000001', '231005601', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0042-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0042-0000-0000-0000-000000000001');

-- Student 43: اسماء عادل بيومى (232004221)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0043-0000-0000-0000-000000000001', '232004221@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'اسماء', 'عادل بيومى', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0043-0000-0000-0000-000000000001', '232004221', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0043-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0043-0000-0000-0000-000000000001');

-- Student 44: ليندا احمد مصيلحى (231006154)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0044-0000-0000-0000-000000000001', '231006154@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'ليندا', 'احمد مصيلحى', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0044-0000-0000-0000-000000000001', '231006154', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0044-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0044-0000-0000-0000-000000000001');

-- Student 45: مروان عبدالمنعم عبدالمنعم (231008132)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0045-0000-0000-0000-000000000001', '231008132@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'مروان', 'عبدالمنعم عبدالمنعم', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0045-0000-0000-0000-000000000001', '231008132', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0045-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0045-0000-0000-0000-000000000001');

-- Student 46: محمد اسلام على (231004918)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0046-0000-0000-0000-000000000001', '231004918@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'محمد', 'اسلام على', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0046-0000-0000-0000-000000000001', '231004918', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0046-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0046-0000-0000-0000-000000000001');

-- Student 47: ضحى ايمن حسن (231005865)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0047-0000-0000-0000-000000000001', '231005865@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'ضحى', 'ايمن حسن', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0047-0000-0000-0000-000000000001', '231005865', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0047-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0047-0000-0000-0000-000000000001');

-- Student 48: محمد احمد التهامى (231015004)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0048-0000-0000-0000-000000000001', '231015004@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'محمد', 'احمد التهامى', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0048-0000-0000-0000-000000000001', '231015004', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0048-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0048-0000-0000-0000-000000000001');

-- Student 49: تسنيم احمد الوزير (231014462)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0049-0000-0000-0000-000000000001', '231014462@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'تسنيم', 'احمد الوزير', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0049-0000-0000-0000-000000000001', '231014462', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0049-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0049-0000-0000-0000-000000000001');

-- Student 50: همسه اشرف السخي (231014761)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0050-0000-0000-0000-000000000001', '231014761@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'همسه', 'اشرف السخي', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0050-0000-0000-0000-000000000001', '231014761', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0050-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0050-0000-0000-0000-000000000001');

-- Student 51: بسمله محمد محمد (231006502)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0051-0000-0000-0000-000000000001', '231006502@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'بسمله', 'محمد محمد', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0051-0000-0000-0000-000000000001', '231006502', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0051-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0051-0000-0000-0000-000000000001');

-- Student 52: احمد خورشيد ميهوب (231006272)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0052-0000-0000-0000-000000000001', '231006272@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'احمد', 'خورشيد ميهوب', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0052-0000-0000-0000-000000000001', '231006272', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0052-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0052-0000-0000-0000-000000000001');

-- Student 53: ياسين شريف الجوهري (231004567)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0053-0000-0000-0000-000000000001', '231004567@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'ياسين', 'شريف الجوهري', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0053-0000-0000-0000-000000000001', '231004567', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0053-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0053-0000-0000-0000-000000000001');

-- Student 54: باسل اسامه سليمان (231005711)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0054-0000-0000-0000-000000000001', '231005711@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'باسل', 'اسامه سليمان', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0054-0000-0000-0000-000000000001', '231005711', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0054-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0054-0000-0000-0000-000000000001');

-- Student 55: مروان محمد خلف (211014850)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0055-0000-0000-0000-000000000001', '211014850@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'مروان', 'محمد خلف', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0055-0000-0000-0000-000000000001', '211014850', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0055-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0055-0000-0000-0000-000000000001');

-- Student 56: للوار صادق حسين (231006900)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0056-0000-0000-0000-000000000001', '231006900@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'للوار', 'صادق حسين', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0056-0000-0000-0000-000000000001', '231006900', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0056-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0056-0000-0000-0000-000000000001');

-- Student 57: منه الله عطيه (231014783)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0057-0000-0000-0000-000000000001', '231014783@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'منه', 'الله عطيه', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0057-0000-0000-0000-000000000001', '231014783', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0057-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0057-0000-0000-0000-000000000001');

-- Student 58: احمد فوزى الياسرجى (231005915)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0058-0000-0000-0000-000000000001', '231005915@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'احمد', 'فوزى الياسرجى', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0058-0000-0000-0000-000000000001', '231005915', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0058-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0058-0000-0000-0000-000000000001');

-- Student 59: نور احمد محمد (231014666)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0059-0000-0000-0000-000000000001', '231014666@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'نور', 'احمد محمد', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0059-0000-0000-0000-000000000001', '231014666', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0059-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0059-0000-0000-0000-000000000001');

-- Student 60: جنى محمد رياض (231006613)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0060-0000-0000-0000-000000000001', '231006613@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'جنى', 'محمد رياض', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0060-0000-0000-0000-000000000001', '231006613', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0060-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0060-0000-0000-0000-000000000001');

-- Student 61: انجى على طه (231017969)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0061-0000-0000-0000-000000000001', '231017969@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'انجى', 'على طه', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0061-0000-0000-0000-000000000001', '231017969', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0061-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0061-0000-0000-0000-000000000001');

-- Student 62: نورالهدى اشرف محمود (231006601)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0062-0000-0000-0000-000000000001', '231006601@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'نورالهدى', 'اشرف محمود', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0062-0000-0000-0000-000000000001', '231006601', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0062-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0062-0000-0000-0000-000000000001');

-- Student 63: عمر حسام جاد (231006131)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0063-0000-0000-0000-000000000001', '231006131@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'عمر', 'حسام جاد', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0063-0000-0000-0000-000000000001', '231006131', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0063-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0063-0000-0000-0000-000000000001');

-- Student 64: اسراء عمرو سلامه (231015037)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0064-0000-0000-0000-000000000001', '231015037@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'اسراء', 'عمرو سلامه', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0064-0000-0000-0000-000000000001', '231015037', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0064-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0064-0000-0000-0000-000000000001');

-- Student 65: رنا ياسر عفيفي (231014860)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0065-0000-0000-0000-000000000001', '231014860@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'رنا', 'ياسر عفيفي', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0065-0000-0000-0000-000000000001', '231014860', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0065-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0065-0000-0000-0000-000000000001');

-- Student 66: على سيد حسانين (231004649)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0066-0000-0000-0000-000000000001', '231004649@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'على', 'سيد حسانين', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0066-0000-0000-0000-000000000001', '231004649', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0066-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0066-0000-0000-0000-000000000001');

-- Student 67: ياسين وفيق طولان (231004431)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0067-0000-0000-0000-000000000001', '231004431@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'ياسين', 'وفيق طولان', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0067-0000-0000-0000-000000000001', '231004431', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0067-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0067-0000-0000-0000-000000000001');

-- Student 68: مهاب امين حجازى (231014259)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0068-0000-0000-0000-000000000001', '231014259@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'مهاب', 'امين حجازى', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0068-0000-0000-0000-000000000001', '231014259', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0068-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0068-0000-0000-0000-000000000001');

-- Student 69: يوسف احمد الصواف (231014599)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0069-0000-0000-0000-000000000001', '231014599@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'يوسف', 'احمد الصواف', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0069-0000-0000-0000-000000000001', '231014599', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0069-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0069-0000-0000-0000-000000000001');

-- Student 70: كريم محمد فتيح (231006928)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0070-0000-0000-0000-000000000001', '231006928@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'كريم', 'محمد فتيح', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0070-0000-0000-0000-000000000001', '231006928', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0070-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0070-0000-0000-0000-000000000001');

-- Student 71: مروان محمد الشامى (231006417)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0071-0000-0000-0000-000000000001', '231006417@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'مروان', 'محمد الشامى', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0071-0000-0000-0000-000000000001', '231006417', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0071-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0071-0000-0000-0000-000000000001');

-- Student 72: زياد ايهاب احمد (231014691)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0072-0000-0000-0000-000000000001', '231014691@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'زياد', 'ايهاب احمد', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0072-0000-0000-0000-000000000001', '231014691', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0072-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0072-0000-0000-0000-000000000001');

-- Student 73: نديم كامل كامل (231014324)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0073-0000-0000-0000-000000000001', '231014324@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'نديم', 'كامل كامل', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0073-0000-0000-0000-000000000001', '231014324', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0073-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0073-0000-0000-0000-000000000001');

-- Student 74: ياسين تامر عبدالحميد (231006879)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0074-0000-0000-0000-000000000001', '231006879@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'ياسين', 'تامر عبدالحميد', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0074-0000-0000-0000-000000000001', '231006879', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0074-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0074-0000-0000-0000-000000000001');

-- Student 75: ريتاج على على (231005689)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0075-0000-0000-0000-000000000001', '231005689@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'ريتاج', 'على على', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0075-0000-0000-0000-000000000001', '231005689', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0075-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0075-0000-0000-0000-000000000001');

-- Student 76: مهند وليد نصار (231005430)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0076-0000-0000-0000-000000000001', '231005430@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'مهند', 'وليد نصار', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0076-0000-0000-0000-000000000001', '231005430', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0076-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0076-0000-0000-0000-000000000001');

-- Student 77: يوسف حسين نور (231004387)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0077-0000-0000-0000-000000000001', '231004387@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'يوسف', 'حسين نور', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0077-0000-0000-0000-000000000001', '231004387', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0077-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0077-0000-0000-0000-000000000001');

-- Student 78: جيداء مجدى الخشاب (231004747)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0078-0000-0000-0000-000000000001', '231004747@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'جيداء', 'مجدى الخشاب', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0078-0000-0000-0000-000000000001', '231004747', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0078-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0078-0000-0000-0000-000000000001');

-- Student 79: زياد محمد همام (231006572)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0079-0000-0000-0000-000000000001', '231006572@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'زياد', 'محمد همام', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0079-0000-0000-0000-000000000001', '231006572', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0079-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0079-0000-0000-0000-000000000001');

-- Student 80: مروان وليد حجاج (231004727)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0080-0000-0000-0000-000000000001', '231004727@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'مروان', 'وليد حجاج', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0080-0000-0000-0000-000000000001', '231004727', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0080-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0080-0000-0000-0000-000000000001');

-- Student 81: روان عاطف حسن (231005789)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0081-0000-0000-0000-000000000001', '231005789@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'روان', 'عاطف حسن', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0081-0000-0000-0000-000000000001', '231005789', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0081-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0081-0000-0000-0000-000000000001');

-- Student 82: مصطفى وليد الخولي (231014241)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0082-0000-0000-0000-000000000001', '231014241@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'مصطفى', 'وليد الخولي', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0082-0000-0000-0000-000000000001', '231014241', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0082-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0082-0000-0000-0000-000000000001');

-- Student 83: عمر نشأت على (231004224)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0083-0000-0000-0000-000000000001', '231004224@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'عمر', 'نشأت على', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0083-0000-0000-0000-000000000001', '231004224', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0083-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0083-0000-0000-0000-000000000001');

-- Student 84: سما سيد سليمان (231014002)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0084-0000-0000-0000-000000000001', '231014002@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'سما', 'سيد سليمان', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0084-0000-0000-0000-000000000001', '231014002', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0084-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0084-0000-0000-0000-000000000001');

-- Student 85: ديفيد عاطف مكاريوس (231014849)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0085-0000-0000-0000-000000000001', '231014849@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'ديفيد', 'عاطف مكاريوس', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0085-0000-0000-0000-000000000001', '231014849', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0085-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0085-0000-0000-0000-000000000001');

-- Student 86: ندى عبدالعزيز امنه (231014025)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0086-0000-0000-0000-000000000001', '231014025@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'ندى', 'عبدالعزيز امنه', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0086-0000-0000-0000-000000000001', '231014025', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0086-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0086-0000-0000-0000-000000000001');

-- Student 87: يوسف كريم محمد (231014449)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0087-0000-0000-0000-000000000001', '231014449@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'يوسف', 'كريم محمد', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0087-0000-0000-0000-000000000001', '231014449', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0087-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0087-0000-0000-0000-000000000001');

-- Student 88: زياد محمد مخلوف (231014457)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0088-0000-0000-0000-000000000001', '231014457@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'زياد', 'محمد مخلوف', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0088-0000-0000-0000-000000000001', '231014457', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0088-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0088-0000-0000-0000-000000000001');

-- Student 89: ميرنا عبدالعظيم مصطفى (231006127)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0089-0000-0000-0000-000000000001', '231006127@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'ميرنا', 'عبدالعظيم مصطفى', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0089-0000-0000-0000-000000000001', '231006127', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0089-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0089-0000-0000-0000-000000000001');

-- Student 90: عبدالله اشرف عبدالعزيز (231004285)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0090-0000-0000-0000-000000000001', '231004285@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'عبدالله', 'اشرف عبدالعزيز', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0090-0000-0000-0000-000000000001', '231004285', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0090-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0090-0000-0000-0000-000000000001');

-- Student 91: مصطفى محمد جبر (231005940)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0091-0000-0000-0000-000000000001', '231005940@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'مصطفى', 'محمد جبر', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0091-0000-0000-0000-000000000001', '231005940', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0091-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0091-0000-0000-0000-000000000001');

-- Student 92: محمد احمد مرسي (231014744)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0092-0000-0000-0000-000000000001', '231014744@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'محمد', 'احمد مرسي', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0092-0000-0000-0000-000000000001', '231014744', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0092-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0092-0000-0000-0000-000000000001');

-- Student 93: ليال احمد موسى (231006574)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0093-0000-0000-0000-000000000001', '231006574@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'ليال', 'احمد موسى', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0093-0000-0000-0000-000000000001', '231006574', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0093-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0093-0000-0000-0000-000000000001');

-- Student 94: يوسف محمد نحله (231006950)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0094-0000-0000-0000-000000000001', '231006950@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'يوسف', 'محمد نحله', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0094-0000-0000-0000-000000000001', '231006950', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0094-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0094-0000-0000-0000-000000000001');

-- Student 95: ياسين السيد الهادى (231014539)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0095-0000-0000-0000-000000000001', '231014539@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'ياسين', 'السيد الهادى', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0095-0000-0000-0000-000000000001', '231014539', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0095-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0095-0000-0000-0000-000000000001');

-- Student 96: ضحى محمود ضيف (231005333)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0096-0000-0000-0000-000000000001', '231005333@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'ضحى', 'محمود ضيف', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0096-0000-0000-0000-000000000001', '231005333', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0096-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0096-0000-0000-0000-000000000001');

-- Student 97: شهد محمد جبريل (231005400)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0097-0000-0000-0000-000000000001', '231005400@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'شهد', 'محمد جبريل', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0097-0000-0000-0000-000000000001', '231005400', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0097-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0097-0000-0000-0000-000000000001');

-- Student 98: نور احمد الجندى (231014166)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0098-0000-0000-0000-000000000001', '231014166@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'نور', 'احمد الجندى', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0098-0000-0000-0000-000000000001', '231014166', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0098-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0098-0000-0000-0000-000000000001');

-- Student 99: محمد ياسر فرج (231006335)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0099-0000-0000-0000-000000000001', '231006335@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'محمد', 'ياسر فرج', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0099-0000-0000-0000-000000000001', '231006335', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0099-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0099-0000-0000-0000-000000000001');

-- Student 100: انس محمد ستيت (231006825)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0100-0000-0000-0000-000000000001', '231006825@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'انس', 'محمد ستيت', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0100-0000-0000-0000-000000000001', '231006825', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0100-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0100-0000-0000-0000-000000000001');

-- Student 101: عبدالرحمن سيد توفيق (231014647)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0101-0000-0000-0000-000000000001', '231014647@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'عبدالرحمن', 'سيد توفيق', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0101-0000-0000-0000-000000000001', '231014647', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0101-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0101-0000-0000-0000-000000000001');

-- Student 102: عبدالرحمن طارق نور (231014333)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0102-0000-0000-0000-000000000001', '231014333@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'عبدالرحمن', 'طارق نور', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0102-0000-0000-0000-000000000001', '231014333', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0102-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0102-0000-0000-0000-000000000001');

-- Student 103: نور خالد خليف (231004419)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0103-0000-0000-0000-000000000001', '231004419@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'نور', 'خالد خليف', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0103-0000-0000-0000-000000000001', '231004419', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0103-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0103-0000-0000-0000-000000000001');

-- Student 104: ساره رائف عبدالسلام (231015069)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0104-0000-0000-0000-000000000001', '231015069@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'ساره', 'رائف عبدالسلام', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0104-0000-0000-0000-000000000001', '231015069', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0104-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0104-0000-0000-0000-000000000001');

-- Student 105: حازم اسامه حجاج (231006012)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0105-0000-0000-0000-000000000001', '231006012@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'حازم', 'اسامه حجاج', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0105-0000-0000-0000-000000000001', '231006012', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0105-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0105-0000-0000-0000-000000000001');

-- Student 106: يوسف عبدالمنعم سالمان (231014590)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0106-0000-0000-0000-000000000001', '231014590@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'يوسف', 'عبدالمنعم سالمان', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0106-0000-0000-0000-000000000001', '231014590', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0106-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0106-0000-0000-0000-000000000001');

-- Student 107: زيد محمد حامد (231006511)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0107-0000-0000-0000-000000000001', '231006511@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'زيد', 'محمد حامد', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0107-0000-0000-0000-000000000001', '231006511', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0107-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0107-0000-0000-0000-000000000001');

-- Student 108: عمر عماد الحبشى (231006695)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0108-0000-0000-0000-000000000001', '231006695@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'عمر', 'عماد الحبشى', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0108-0000-0000-0000-000000000001', '231006695', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0108-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0108-0000-0000-0000-000000000001');

-- Student 109: ملك احمد ابراهيم (231016666)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0109-0000-0000-0000-000000000001', '231016666@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'ملك', 'احمد ابراهيم', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0109-0000-0000-0000-000000000001', '231016666', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0109-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0109-0000-0000-0000-000000000001');

-- Student 110: يوسف محمد ابراهيم (231006856)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0110-0000-0000-0000-000000000001', '231006856@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'يوسف', 'محمد ابراهيم', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0110-0000-0000-0000-000000000001', '231006856', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0110-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0110-0000-0000-0000-000000000001');

-- Student 111: فاطمه خليل خليل (231014342)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0111-0000-0000-0000-000000000001', '231014342@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'فاطمه', 'خليل خليل', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0111-0000-0000-0000-000000000001', '231014342', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0111-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0111-0000-0000-0000-000000000001');

-- Student 112: نانسى محمد عرفات (231005501)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0112-0000-0000-0000-000000000001', '231005501@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'نانسى', 'محمد عرفات', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0112-0000-0000-0000-000000000001', '231005501', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0112-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0112-0000-0000-0000-000000000001');

-- Student 113: عبدالرحمن محمد عبدالنبى (231015218)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0113-0000-0000-0000-000000000001', '231015218@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'عبدالرحمن', 'محمد عبدالنبى', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0113-0000-0000-0000-000000000001', '231015218', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0113-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0113-0000-0000-0000-000000000001');

-- Student 114: يحيى احمد الحاوى (231004713)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0114-0000-0000-0000-000000000001', '231004713@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'يحيى', 'احمد الحاوى', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0114-0000-0000-0000-000000000001', '231004713', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0114-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0114-0000-0000-0000-000000000001');

-- Student 115: احمد محمد محمد (231014786)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0115-0000-0000-0000-000000000001', '231014786@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'احمد', 'محمد محمد', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0115-0000-0000-0000-000000000001', '231014786', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0115-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0115-0000-0000-0000-000000000001');

-- Student 116: عبدالرحمن محمد الصاوى (231005073)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0116-0000-0000-0000-000000000001', '231005073@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'عبدالرحمن', 'محمد الصاوى', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0116-0000-0000-0000-000000000001', '231005073', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0116-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0116-0000-0000-0000-000000000001');

-- Student 117: عبدالرحمن احمد عليوه (231014755)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0117-0000-0000-0000-000000000001', '231014755@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'عبدالرحمن', 'احمد عليوه', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0117-0000-0000-0000-000000000001', '231014755', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0117-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0117-0000-0000-0000-000000000001');

-- Student 118: رقيه حمدى ربه (231006586)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0118-0000-0000-0000-000000000001', '231006586@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'رقيه', 'حمدى ربه', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0118-0000-0000-0000-000000000001', '231006586', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0118-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0118-0000-0000-0000-000000000001');

-- Student 119: احمد فاروق دنيا (231014395)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('st0119-0000-0000-0000-000000000001', '231014395@eduvision.com', '$2a$10$SV7gi60Lxy61GuHvNA6PbOGAx7BdtmnwKvoG6ma.WfFCjAC/6Z7.e', 'احمد', 'فاروق دنيا', 'active');
INSERT INTO students (user_id, student_number, program, year_of_study, consent_given, consent_date, face_encoding) VALUES
('st0119-0000-0000-0000-000000000001', '231014395', 'Computer Science', 2, 1, NOW(), REPEAT(X'00000000', 64));
INSERT INTO user_roles (user_id, role_id) VALUES ('st0119-0000-0000-0000-000000000001', '564cabe8-3fdc-11f1-a3e3-fda12bf44785');
INSERT INTO course_students (course_id, student_id) VALUES ('c06c2fe0-417f-11f1-a276-543615c7a7ec', 'st0119-0000-0000-0000-000000000001');
