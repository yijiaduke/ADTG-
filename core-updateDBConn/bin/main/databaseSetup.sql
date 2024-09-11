-- DATABASE SCHEMA

CREATE SCHEMA adtg;


-- Table: users
CREATE TABLE adtg.users (
netid   VARCHAR(50)     PRIMARY KEY,
email   VARCHAR(100)    NOT NULL,
name    VARCHAR(100)    NOT NULL
);

-- Table: user_auth
CREATE TABLE adtg.user_auth (
netid   VARCHAR(50)     NOT NULL PRIMARY KEY,
hash    VARCHAR(255)    NOT NULL,
FOREIGN KEY (netid) REFERENCES adtg.users(netid)
);

-- Table: user_role
CREATE TABLE adtg.user_role (
netid       VARCHAR(50)         NOT NULL,
user_role   VARCHAR(50)         NOT NULL, --this is the role of the user. multiple roles possible. 
PRIMARY KEY (netid, user_role),
FOREIGN KEY (netid) REFERENCES adtg.users(netid) 
);

-- Table: course
CREATE TABLE adtg.course (
c_subject       CHAR(10)        NOT NULL,
c_number        NUMERIC(4)      NOT NULL,
title           VARCHAR(255)    NOT NULL,
gitlab_repo     VARCHAR(1000)   NOT NULL, --the gitlab repo containing the assessments https://gitlab.oit.duke.edu/kits/ece551_0000/
gitlab_token    VARCHAR(1000)   NOT NULL, --the gitlab repo token
PRIMARY KEY (c_subject, c_number)
);


-- Table: Course TAs 
CREATE TABLE adtg.course_ta (
c_subject   CHAR(10)    NOT NULL,
c_number    NUMERIC(4)  NOT NULL,
ta_netid    VARCHAR(50) NOT NULL,
PRIMARY KEY (c_subject, c_number, ta_netid),
FOREIGN KEY (c_subject,c_number) REFERENCES adtg.course(c_subject, c_number),
FOREIGN KEY (ta_netid) REFERENCES adtg.users(netid)
);

-- Table: section
CREATE TABLE adtg.section (
c_subject           CHAR(10)        NOT NULL,
c_number            NUMERIC(4)      NOT NULL,
sec_id              NUMERIC(3)      NOT NULL,
instructor_netid    VARCHAR(50)     NOT NULL,
startdate           DATE            NOT NULL,
gitlab_group        VARCHAR(1000)   NOT NULL, --the gitlab group where student repositories will be stored 
gitlab_token        VARCHAR(1000)   NOT NULL, --the gitlab group
PRIMARY KEY (c_subject, c_number, sec_id),
FOREIGN KEY (c_subject,c_number) REFERENCES adtg.course(c_subject, c_number),
FOREIGN KEY (instructor_netid) REFERENCES adtg.users(netid)
);

-- Table: enrollment
CREATE TABLE adtg.enrollment (
c_subject       CHAR(10)    NOT NULL,
c_number        NUMERIC(4)  NOT NULL,
sec_id          NUMERIC(3)  NOT NULL,
student_netid   VARCHAR(50) NOT NULL,
tokens_avail    NUMERIC(4)  NOT NULL DEFAULT 0, -- current student token balance
PRIMARY KEY (c_subject, c_number, sec_id, student_netid),
FOREIGN KEY (c_subject, c_number, sec_id) REFERENCES adtg.section(c_subject, c_number, sec_id),
FOREIGN KEY (student_netid) REFERENCES adtg.users(netid)
);










-- ------------------------------------
-- -------- ASSESSMENTS
-- ------------------------------------
CREATE TABLE adtg.assn_category(
category    varCHAR(100)    NOT NULL PRIMARY KEY
);




-- Table: assessment
CREATE TABLE adtg.assessment (
c_subject           CHAR(10)        NOT NULL,
c_number            NUMERIC(4)      NOT NULL,
assn                VARCHAR(50)     NOT NULL,
title               VARCHAR(255)    NOT NULL,
start_date          TIMESTAMP       NOT NULL,
due                 TIMESTAMP       NOT NULL,
token_req           NUMERIC(4)      NOT NULL DEFAULT 0,     -- tokens needed to grade this assessment
category            VARCHAR(100)    NOT NULL,     --course category, used for late policies.
max_score           NUMERIC(6, 2)   NOT NULL,
passing_score       NUMERIC(6, 2)   NOT NULL DEFAULT 0,
is_extra_credit     BOOLEAN         NOT NULL DEFAULT false,
test_cmd            VARCHAR(2048)   NOT NULL,
PRIMARY KEY (c_subject, c_number, assn),
FOREIGN KEY (c_subject, c_number) REFERENCES adtg.course(c_subject, c_number),
FOREIGN KEY (category) REFERENCES adtg.assn_category
);

-- Table: prerequisite
CREATE TABLE adtg.prerequisite (
c_subject       CHAR(10)    NOT NULL,
c_number        NUMERIC(4)  NOT NULL,
assn            VARCHAR(50) NOT NULL,
pre_c_subject   CHAR(10)    NOT NULL,
pre_c_number    NUMERIC(4)  NOT NULL,
pre_assn        VARCHAR(50) NOT NULL,
PRIMARY KEY (c_subject, c_number, assn, pre_c_subject, pre_c_number, pre_assn ),
FOREIGN KEY (c_subject, c_number, assn) REFERENCES adtg.assessment(c_subject, c_number, assn),
FOREIGN KEY (pre_c_subject, pre_c_number, pre_assn) REFERENCES adtg.assessment(c_subject, c_number, assn)
);




-- ------------------------------------
-- -------- ASSESSMENT MANAGEMENT
-- ------------------------------------
CREATE TABLE adtg.assn_deadline(
c_subject           CHAR(10)        NOT NULL,
c_number            NUMERIC(4)      NOT NULL,
assn                VARCHAR(50)     NOT NULL,
netid               VARCHAR(50)     NOT NULL,
due                 TIMESTAMP       NOT NULL, -- THIS IS THE DUE for the student assn. It can be modified by the instructor.
--assn_to_grade       CHAR(1)         NOT NULL CHECK (assn_to_grade in ('Y','N')), -- this tells the grader which assessment to grade
PRIMARY KEY (c_subject, c_number, assn, netid),
FOREIGN KEY (netid) REFERENCES adtg.users(netid),
FOREIGN KEY (c_subject, c_number, assn) REFERENCES adtg.assessment(c_subject, c_number, assn)
);


-- ------------------------------------
-- -------- DELIVERY
-- ------------------------------------

-- Table: delivery
CREATE TABLE adtg.delivery (
delivery_time   TIMESTAMP       NOT NULL,
c_subject       CHAR(10)        NOT NULL,
c_number        NUMERIC(4)      NOT NULL,
assn            VARCHAR(50)     NOT NULL,
netid           VARCHAR(50)     NOT NULL,
status          VARCHAR(50)     NOT NULL,  
    -- COMPLETED 
    -- INIT  -> read c_subject, c_number, netId 
    --            --> if no repo for student X, in that course then initialize the repo. 
    --            Retrieve gitlab group from section table.
    --              Repository name format: 
    -- DELIVER  -> read c_subject, c_number, netid, assn - Deliver the assessment to the student's repo. 
    --              Add tuple to assn_deadline
log_text        TEXT,

PRIMARY KEY (delivery_time, c_subject, c_number, assn, netid),
FOREIGN KEY (netid) REFERENCES adtg.users(netid),
FOREIGN KEY (c_subject, c_number, assn) REFERENCES adtg.assessment(c_subject, c_number, assn)

-- deliveryId      VARCHAR(50)     NOT NULL PRIMARY KEY,   -- NEEDED? ?? 
);




-- ------------------------------------
-- -------- GRADER
-- ------------------------------------


-- Table: final_grade
CREATE TABLE adtg.final_grade (               ---- NOT NEEDED  CAN BE QUERY FROM GRADES
c_subject      CHAR(10)        NOT NULL,
c_number        NUMERIC(4)      NOT NULL,
assn            VARCHAR(50)     NOT NULL,
netid           VARCHAR(50)     NOT NULL,
grade           NUMERIC(6, 2)   NOT NULL,   --includes any penalty applied.

PRIMARY KEY (c_subject, c_number, assn, netid), 
FOREIGN KEY (netid) REFERENCES adtg.users(netid),
FOREIGN KEY (c_subject, c_number, assn) REFERENCES adtg.assessment(c_subject, c_number, assn)

--deliveryId VARCHAR(50) PRIMARY KEY,             -- --- WHY????? NOT NEEDED
--FOREIGN KEY (deliveryId) REFERENCES delivery(eliveryId),
);

-- Table: grades
CREATE TABLE adtg.grades (
c_subject       CHAR(10)        NOT NULL,
c_number        NUMERIC(4)      NOT NULL,
assn            VARCHAR(50)     NOT NULL,
netid           VARCHAR(50)     NOT NULL,
grade_time      TIMESTAMP       NOT NULL,
assn_grade      NUMERIC(6, 2)   NOT NULL,   -- w/o penalty
penalty         NUMERIC(6, 2)   NOT NULL,      -- as determined by assn policy
final_grade     NUMERIC(6, 2)   NOT NULL, -- assn_grade - penalty --> to copy to final_Gade
log_text        TEXT,
PRIMARY KEY (c_subject, c_number, assn, netid, grade_time), 
FOREIGN KEY (netid) REFERENCES adtg.users(netid),
FOREIGN KEY (c_subject, c_number, assn) REFERENCES adtg.assessment(c_subject, c_number, assn)
);

--------------------
CREATE TABLE adtg.grade_request(
    --records grading requests from students using portal to the grader.
id              BIGSERIAL       NOT NULL PRIMARY KEY,
c_subject       CHAR(10)        NOT NULL,
c_number        NUMERIC(4)      NOT NULL,
assn            VARCHAR(50)     NOT NULL,
netid           VARCHAR(50)     NOT NULL,
tmstmp          TIMESTAMP       NOT NULL,
status          VARCHAR(50)     NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'IN PROGRESS', 'DONE')),
log_text        TEXT,
owner           VARCHAR(200),
commitID        VARCHAR(200),
expires_on      TIMESTAMP,
apply_penalty   CHAR(1) NOT NULL DEFAULT 'Y',
FOREIGN KEY (netid) REFERENCES adtg.users(netid),
FOREIGN KEY (c_subject, c_number, assn) REFERENCES adtg.assessment(c_subject, c_number, assn)
);

