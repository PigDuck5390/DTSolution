-- =============================================
-- DT Solution Auth System - Oracle DDL
-- =============================================

-- 기존 테이블 삭제 (재실행 시 충돌 방지)
DROP TABLE user_project  CASCADE CONSTRAINTS PURGE;
DROP TABLE user_profile  CASCADE CONSTRAINTS PURGE;
DROP TABLE login_log     CASCADE CONSTRAINTS PURGE;
DROP TABLE users         CASCADE CONSTRAINTS PURGE;

-- =============================================
-- 1. USERS (인증 전용)
-- =============================================
CREATE TABLE users (
    id            NUMBER         GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    emp_no        VARCHAR2(20)   NOT NULL,
    password      VARCHAR2(255)  NOT NULL,
    name          VARCHAR2(50)   NOT NULL,
    email         VARCHAR2(100)  NOT NULL,
    role          VARCHAR2(20)   DEFAULT 'ROLE_USER' NOT NULL,
    enabled       NUMBER(1)      DEFAULT 1 NOT NULL,
    fail_count    NUMBER         DEFAULT 0 NOT NULL,
    locked_until  TIMESTAMP      NULL,
    created_at    TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_login_at TIMESTAMP      NULL,
    CONSTRAINT pk_users    PRIMARY KEY (id),
    CONSTRAINT uq_emp_no   UNIQUE (emp_no),
    CONSTRAINT uq_email    UNIQUE (email)
);

CREATE INDEX idx_users_emp_no ON users(emp_no);

COMMENT ON TABLE  users              IS '인증 전용 - 로그인에 필요한 최소 정보만 보유';
COMMENT ON COLUMN users.emp_no       IS '사번 - 로그인 ID';
COMMENT ON COLUMN users.role         IS 'ROLE_ADMIN / ROLE_APPROVER / ROLE_USER / ROLE_VIEWER';
COMMENT ON COLUMN users.fail_count   IS '5회 초과 시 30분 계정 잠금';

-- =============================================
-- 2. USER_PROFILE (임직원 상세 - HR 연동 대비)
-- =============================================
CREATE TABLE user_profile (
    emp_no      VARCHAR2(20)   NOT NULL,
    dept        VARCHAR2(100)  NULL,
    position    VARCHAR2(20)   NULL,
    phone       VARCHAR2(20)   NULL,
    updated_at  TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_user_profile PRIMARY KEY (emp_no),
    CONSTRAINT fk_profile_user FOREIGN KEY (emp_no) REFERENCES users(emp_no)
);

COMMENT ON TABLE  user_profile          IS 'HR 연동 또는 관리자 설정으로 채워지는 임직원 상세 정보';
COMMENT ON COLUMN user_profile.position IS '사원/대리/과장/차장/부장/임원';

-- =============================================
-- 3. LOGIN_LOG (보안 감사)
-- =============================================
CREATE TABLE login_log (
    id          NUMBER         GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    emp_no      VARCHAR2(20)   NOT NULL,
    login_at    TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL,
    logout_at   TIMESTAMP      NULL,
    ip_address  VARCHAR2(45)   NULL,
    success     NUMBER(1)      DEFAULT 1 NOT NULL,
    reason      VARCHAR2(100)  NULL
);

CREATE INDEX idx_login_log_emp ON login_log(emp_no, login_at);

COMMENT ON TABLE login_log IS '로그인/로그아웃 이력 - 보안 감사 및 비정상 접근 탐지용';