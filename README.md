# DTSolution 로그인/로그아웃 시스템

---

## 사용 기술

|    구분    |           기술          |                          선택 이유                                 |
|-----------|-------------------------|------------------------------------------------------------------|
| Language  | Java 17                 | Spring Boot 3.x 최소 요구사항, 현재 실무 표준                        |
| Framework | Spring Boot 3.5         | 빠른 설정, 내장 서버, Spring Security 통합                           |
| Security  | Spring Security 6       | 인증/인가/세션/CSRF 처리를 프레임워크 수준에서 관리                     |
| ORM       | MyBatis                 | Oracle 특화 쿼리(ROWNUM 등) 직접 작성 가능, 복잡한 건설 ERP 쿼리에 적합 |
| View      | Thymeleaf + Bootstrap 5 | 서버사이드 렌더링, Spring Security CSRF 토큰 자동 삽입                |
| DB        | Oracle 21c XE           | 디티솔루션 주력 솔루션(Primavera Unifier)의 기본 DB와 동일한 스택       |
| Password  | BCrypt                  | Salt 자동 적용, Rainbow Table 공격 방어, Cost Factor 조절 가능        |

---

## DB 설계

### 테이블 구조

```
USERS (인증 전용)
│   사번(emp_no), 비밀번호, 이름, 이메일만 보유
│   → 인증 역할만 가짐으로써 단일 책임 원칙 준수
│   → 기능 추가 시 USERS 테이블을 건드릴 필요 없음
│
├── USER_PROFILE (임직원 상세 - 1:1)
│       부서, 직급, 연락처
│       → HR 시스템 연동 또는 관리자 설정으로 채워짐
│       → 회원가입 시 빈 row 생성, 추후 업데이트
│
├── USER_PROJECT (프로젝트 매핑 - N:M)
│       emp_no ↔ project_code
│       → 직원 한 명이 여러 프로젝트 담당 가능
│       → 프로젝트별 데이터 접근 제한 기준
│
└── LOGIN_LOG (보안 감사)
        로그인/로그아웃 이력, IP, 성공여부
        → emp_no를 FK 대신 문자열로 저장
          (사용자 삭제 후에도 감사 로그 보존)
```

### 설계 원칙

- **USERS는 인증 전용** — 로그인에 필요한 최소 정보만 보유
- **논리적 삭제** — `enabled` 컬럼으로 비활성화, 물리 삭제 지양
- **감사 추적** — 모든 로그인/로그아웃 이력을 LOGIN_LOG에 기록
- **확장성** — USER_PROFILE, USER_PROJECT는 독립적으로 확장 가능

---

## 보안 요소

|       항목        |                    내용                      |
|------------------|----------------------------------------------|
| 비밀번호 암호화    | BCrypt (Salt 자동 적용)                        |
| CSRF 보호        | Spring Security 기본 활성화, Thymeleaf 자동 삽입 |
| Brute-force 방어 | 5회 실패 시 30분 계정 잠금                       |
| 세션 보안         | 로그인 시 세션 ID 변경 (세션 고정 공격 방어)       |
| 중복 로그인 방지   | 동일 계정 동시 세션 1개 제한                     |
| 계정 열거 방지     | 사번 없음 / 비밀번호 틀림 동일 메시지 처리         |
| 감사 로그         | IP, 시각, 성공여부 DB 기록                      |
| 세션 만료         | 30분 비활성 시 자동 만료                        |

---

## 프로젝트 구조

```
src/main/
├── java/com/dtsolution/auth/
│   ├── config/
│   │   └── SecurityConfig.java         # Security 전체 설정
│   ├── controller/
│   │   └── AuthController.java         # 화면 라우팅
│   ├── domain/                         # 순수 Java 객체 (POJO)
│   │   ├── User.java
│   │   ├── UserProfile.java
│   │   └── LoginLog.java
│   ├── dto/
│   │   └── AuthDto.java                # 요청/응답 데이터
│   ├── mapper/                         # MyBatis 인터페이스
│   │   ├── UserMapper.java
│   │   ├── UserProfileMapper.java
│   │   └── LoginLogMapper.java
│   ├── security/
│   │   └── CustomUserDetailsService.java
│   └── service/
│       ├── UserService.java            # 회원가입 로직
│       └── LoginLogService.java        # 로그인 이력 관리
│
└── resources/
    ├── application.properties
    ├── schema.sql
    ├── mapper/                         # MyBatis XML SQL
    │   ├── UserMapper.xml
    │   ├── UserProfileMapper.xml
    │   └── LoginLogMapper.xml
    ├── static/css/
    │   ├── common.css
    │   ├── auth.css
    │   └── home.css
    └── templates/
        ├── login.html
        ├── signup.html
        └── home.html
```

---

## 실행 방법

### 사전 요구사항

- Java 17 이상
- Oracle 21c XE
- Maven

### 1. Oracle DB 설정

```cmd
# sqlplus 접속
sqlplus sys/비밀번호@localhost:1521/XEPDB1 as sysdba

# 사용자 생성
ALTER SESSION SET CONTAINER = XEPDB1;
CREATE USER dtsolution IDENTIFIED BY 비밀번호;
GRANT CONNECT, RESOURCE, DBA TO dtsolution;
```

### 2. 테이블 생성

DBeaver 또는 sqlplus에서 아래 파일 실행

```
src/main/resources/schema.sql
```

### 3. application.properties 설정

```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521/XEPDB1
spring.datasource.username=dtsolution
spring.datasource.password=비밀번호
```

### 4. 실행

```cmd
./mvnw spring-boot:run
```

### 5. 접속

```
http://localhost:8080/login
```


## 로그인 흐름

```
1. 사용자가 사번/비밀번호 입력 후 POST /login
2. Spring Security가 요청 가로채기
3. CustomUserDetailsService.loadUserByUsername() 호출
4. DB에서 사번으로 사용자 조회
5. BCrypt로 비밀번호 검증
6. 성공 → 세션 생성, LoginLog 기록, /home 리다이렉트
   실패 → 실패 횟수 증가, 5회 초과 시 계정 잠금, /login?error 리다이렉트
```

## UI

**로그인**
<img width="1920" height="950" alt="1_login" src="https://github.com/user-attachments/assets/f6f33f9f-2cc0-447a-a4e0-2e608227aef4" />

**회원가입**
<img width="1903" height="948" alt="2_signup" src="https://github.com/user-attachments/assets/191bf155-7e65-4899-b073-6574c0af93d7" />


**홈화면**
<img width="1920" height="946" alt="3_home" src="https://github.com/user-attachments/assets/1e04597a-54f9-4cb6-8899-63fc8c01c534" />
