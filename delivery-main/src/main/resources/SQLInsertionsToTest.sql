-- All required insertions to db:

--adtg.course:
INSERT INTO adtg.course (c_subject, c_number, title, gitlab_repo, gitlab_token)
VALUES ('CS', 101, 'Intro to Computer Science', 'testing/Repo_13428', 'glpat-TQH4Ssiw_cNaxk5zbNwW'),
('CS', 202, 'CSE Programing', '/home/xl435/data/Assessments', 'NOTOKEN'); 


--adtg.users:
INSERT INTO adtg.users (netid, email, name)
VALUES ('yz853', 'yz853@duke.edu', 'Yz User'),
       ('xl435', 'xl435@duke.edu', 'Louise'),
       ('someone', 'someone@duke.edu', 'someone'),
       ('xl435test', 'Louisexyli@hotmail.com', 'TestAcc'),
       ('inst1', 'inst1@duke.edu', 'Inst1');

--adtg.section:
INSERT INTO adtg.section (c_subject, c_number, sec_id, instructor_netid, startdate, gitlab_group, gitlab_token)
VALUES ('CS', 101, 1, 'inst1', DATE '2024-07-10', 'jpastorino/adtg-test', 'glpat-ciy4u-diLpK12yiKnDDk'),
    ('CS', 202, 1, 'inst1', DATE '2024-07-10', 'jpastorino/adtg-test', 'glpat-ciy4u-diLpK12yiKnDDk');

--adtg.enrollment:
INSERT INTO adtg.enrollment (c_subject, c_number, sec_id, student_netid)
VALUES 
    ('CS', 101, 1, 'xl435test'),
    ('CS', 101, 1, 'yz853'),
    ('CS', 101, 1, 'xl435'),
    ('CS', 202, 1, 'xl435test'),
    ('CS', 202, 1, 'yz853'),
    ('CS', 202, 1, 'xl435');

--adtg.assn_category:
INSERT INTO adtg.assn_category (category) VALUES 
    ('Formative'),
    ('Evaluative');


--adtg.assessment:
INSERT INTO adtg.assessment (c_subject, c_number, assn, title, start_date, due, category, max_score, passing_score, is_extra_credit, test_cmd, token_req)
VALUES 
    ('CS', 101, 'cs101_FIRST_STEP', 'FIRST_STEP', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-06-19 23:59:59', 'Formative', 1.00, 1.00, false, 'python test.py', 0),
    ('CS', 101, 'cs101_hw2', 'Homework 2', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-06-19 23:59:59', 'Formative', 100.00, 80.00, false, 'python test_hw2.py', 1),
    ('CS', 101, 'cs101_hw5', 'Homework 5', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-06-28 23:59:59', 'Formative', 100.00, 80.00, false, 'python test_hw5.py', 1),
    ('CS', 101, 'cs101_hw4', 'Homework 4', TIMESTAMP '2024-06-20 00:00:00', TIMESTAMP '2024-07-07 23:59:59', 'Formative', 100.00, 80.00, false, 'python test_hw4.py', 1),
    ('CS', 101, 'cs101_hw1', 'Homework 1', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-06-27 23:59:59', 'Formative', 100.00, 80.00, false, 'python test_hw1.py', 1),
    ('CS', 101, 'cs101_eval1', 'Eval1', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-06-28 23:59:59', 'Evaluative', 100.00, 80.00, false, 'python test_eval1', 0),
    ('CS', 101, 'cs101_eval2', 'Eval2', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-07-01 23:59:59', 'Evaluative', 100.00, 80.00, false, 'python test_eval2.py', 0),
    ('CS', 101, 'cs101_eval3', 'Eval3', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-07-07 23:59:59', 'Evaluative', 100.00, 80.00, false, 'python test_eval3.py', 0),
    ('CS', 202, 'FIRST_STEP', 'FIRST_STEP', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-06-19 23:59:59', 'Formative', 1.00, 1.00, false, 'python test.py', 0),
    ('CS', 202, 'cs202_hw2', 'Homework 2', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-06-19 23:59:59', 'Formative', 100.00, 80.00, false, 'python test_hw2.py', 1),
    ('CS', 202, 'cs202_hw5', 'Homework 5', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-06-28 23:59:59', 'Formative', 100.00, 80.00, false, 'python test_hw5.py', 1),
    ('CS', 202, 'cs202_hw4', 'Homework 4', TIMESTAMP '2024-06-20 00:00:00', TIMESTAMP '2024-07-07 23:59:59', 'Formative', 100.00, 80.00, false, 'python test_hw4.py', 1),
    ('CS', 202, 'cs202_hw1', 'Homework 1', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-06-27 23:59:59', 'Formative', 100.00, 80.00, false, 'python test_hw1.py', 1),
    ('CS', 202, 'cs202_eval1', 'Eval1', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-06-28 23:59:59', 'Evaluative', 100.00, 80.00, false, 'python test_eval1', 0),
    ('CS', 202, 'cs202_eval2', 'Eval2', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-07-01 23:59:59', 'Evaluative', 100.00, 80.00, false, 'python test_eval2.py', 0),
    ('CS', 202, 'cs202_eval3', 'Eval3', TIMESTAMP '2024-06-18 00:00:00', TIMESTAMP '2024-07-07 23:59:59', 'Evaluative', 100.00, 80.00, false, 'python test_eval3.py', 0);

--adtg.prerequisite:
INSERT INTO adtg.prerequisite (c_subject, c_number, assn, pre_c_subject, pre_c_number, pre_assn)
VALUES 
    ('CS', 101, 'cs101_hw2', 'CS', 101, 'cs101_hw1'),
    ('CS', 101, 'cs101_hw4', 'CS', 101, 'cs101_hw2'),
    ('CS', 101, 'cs101_eval1', 'CS', 101, 'cs101_hw2'),
    ('CS', 101, 'cs101_eval2', 'CS', 101, 'cs101_eval1'),
    ('CS', 101, 'cs101_eval3', 'CS', 101, 'cs101_eval2'),
    ('CS', 101, 'cs101_hw5', 'CS', 101, 'cs101_hw4');


INSERT INTO adtg.delivery (delivery_time, c_subject, c_number, assn, netid, status) 
VALUES
    -- (TIMESTAMP '2024-06-18 00:00:00', 'CS', 101, 'FIRST_STEP', 'yz853', 'init'),
    -- (TIMESTAMP '2024-06-18 00:00:00', 'CS', 101, 'FIRST_STEP', 'xl435', 'init'),
    -- (TIMESTAMP '2024-06-18 00:00:00', 'CS', 101, 'FIRST_STEP', 'someone', 'init'),
    -- (TIMESTAMP '2024-06-18 00:00:00', 'CS', 101, 'cs101_hw1', 'xl435', 'DELIVER'),
    -- (TIMESTAMP '2024-06-18 00:00:00', 'CS', 101, 'cs101_hw1', 'yz853', 'DELIVER'),
    -- (TIMESTAMP '2024-06-18 00:00:00', 'CS', 101, 'cs101_hw1', 'someone', 'DELIVER'),
    -- (TIMESTAMP '2024-06-18 00:00:00', 'CS', 101, 'cs101_hw1', 'xl435test', 'deliver'),
    -- (TIMESTAMP '2024-06-18 00:00:00', 'CS', 101, 'cs101_hw2', 'xl435', 'deliver'),
    -- (TIMESTAMP '2024-06-18 00:00:00', 'CS', 101, 'cs101_eval1', 'xl435', 'deliver'),
    (TIMESTAMP '2024-06-18 00:00:00', 'CS', 202, 'FIRST_STEP', 'yz853', 'init'),
    (TIMESTAMP '2024-06-18 00:00:00', 'CS', 202, 'FIRST_STEP', 'xl435', 'init'),
    (TIMESTAMP '2024-06-18 00:00:00', 'CS', 202, 'FIRST_STEP', 'someone', 'init'),
    (TIMESTAMP '2024-06-18 00:00:00', 'CS', 202, 'cs202_hw1', 'xl435', 'DELIVER'),
    (TIMESTAMP '2024-06-18 00:00:00', 'CS', 202, 'cs202_hw1', 'yz853', 'DELIVER'),
    (TIMESTAMP '2024-06-18 00:00:00', 'CS', 202, 'cs202_hw1', 'someone', 'DELIVER'),
    (TIMESTAMP '2024-06-18 00:00:00', 'CS', 202, 'cs202_hw1', 'xl435test', 'deliver'),
    (TIMESTAMP '2024-06-18 00:00:00', 'CS', 202, 'cs202_hw2', 'xl435', 'deliver'),
    (TIMESTAMP '2024-06-18 00:00:00', 'CS', 202, 'cs202_eval1', 'xl435', 'deliver');

--adtg.grades:
--later!!!

--  c_subject  | c_number |   assn    | netid |         grade_time         | assn_grade | penalty | final_grade | log_text 
-- ------------+----------+-----------+-------+----------------------------+------------+---------+-------------+----------
--  CS         |      101 | cs101_hw1 | xl435 | 2024-06-26 21:25:02.534658 |      70.00 |    0.00 |       70.00 | test
--  CS         |      101 | cs101_hw1 | xl435 | 2024-06-26 21:39:23.949405 |      85.00 |   10.00 |       75.00 | test
--  CS         |      101 | cs101_hw2 | xl435 | 2024-06-26 21:59:37.881839 |      80.00 |    5.00 |       75.00 | test
--  CS         |      101 | cs101_hw1 | xl435 | 2024-07-08 02:35:34.960421 |      75.00 |    0.00 |       75.00 | test

INSERT INTO adtg.grades (c_subject, c_number, assn, netid, grade_time, assn_grade, penalty, final_grade, log_text)
VALUES 
    ('CS', 101, 'cs101_hw1', 'xl435', TIMESTAMP '2024-06-26 21:25:02.534658', 70.00, 0.00, 70.00, 'test'),
    ('CS', 101, 'cs101_hw1', 'xl435', TIMESTAMP '2024-06-26 21:39:23.949405', 85.00, 10.00, 75.00, 'test'),
    ('CS', 101, 'cs101_hw2', 'xl435', TIMESTAMP '2024-06-26 21:59:37.881839', 80.00, 5.00, 75.00, 'test');

INSERT INTO adtg.assn_deadline (c_subject, c_number, assn, netid, due, assn_to_grade)
VALUES 
('CS', 101, 'cs101_hw1', 'yz853', TIMESTAMP '2024-06-27 23:59:59', 'Y'),
('CS', 101, 'cs101_hw1', 'xl435', TIMESTAMP '2024-06-27 23:59:59', 'Y'),
('CS', 101, 'cs101_hw2', 'xl435', TIMESTAMP '2024-06-19 23:59:59', 'Y');


-- Clean before initial test:
DELETE from adtg.grades where c_subject = 'CS';
DELETE from adtg.assn_deadline where c_subject = 'CS';
DELETE from adtg.delivery where c_subject = 'CS';


DELETE FROM adtg.prerequisite;
DELETE FROM adtg.assessment;
DELETE FROM adtg.assn_category;
DELETE FROM adtg.enrollment;
DELETE FROM adtg.section;
DELETE FROM adtg.users;
DELETE FROM adtg.course;

-- or just update ERROR: 
update adtg.delivery set status = 'DELIVER' where status = 'ERROR';