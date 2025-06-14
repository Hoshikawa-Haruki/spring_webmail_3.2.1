# spring_webmail_3.2.1
4-1 객체지향설계 교과목 팀프로젝트 – 웹메일 시스템 개발
<br>
본 프로젝트는 동의대학교 컴퓨터소프트웨어공학과 4학년 1학기 '객체지향설계' 과목의 팀프로젝트로 진행되었습니다.


## 🛠️ James 서버 및 프로젝트 실행 방법

프로젝트를 실행하기 전에 James local 서버가 반드시 구동되어 있어야 합니다.  
다음과 같은 순서로 James 서버를 실행하세요:

### 1. 설치 (최초 1회만 수행)
```bash
james.bat install
```
* James 서버를 Windows 서비스로 등록합니다.
* (설치는 처음 한 번만 필요합니다.)

### 2. 서버 실행
```bash
james.bat start
```

* James 서버를 실행합니다.
* Daemon(백그라운드) 모드로 동작합니다.

### 3. 서버 상태 확인
```bash
james.bat status
```
* 현재 James 서버의 실행 상태를 확인할 수 있습니다.

## 🛠️ MySQL 설정 정보

웹메일 프로젝트는 Docker 기반 MySQL 데이터베이스를 사용합니다.
다음과 같은 계정 정보를 사용해야 정상적으로 연동됩니다:

* **사용자 이름**: `jdbctester`
* **비밀번호**: `12345`
---

### 💾 MySQL 8 Docker 컨테이너 실행 방법

Spring Boot 프로젝트를 실행하기 전에 아래 명령어로 **MySQL 8 컨테이너**를 먼저 실행해야 합니다.

```bash
docker run -dt -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=12345 \
  -e MYSQL_USER=jdbctester \
  -e MYSQL_PASSWORD=12345 \
  --name jsp_mysql8 \
  mysql:8
```

### 📌 참고

* MySQL 컨테이너가 실행 중이어야 Spring Boot 애플리케이션이 DB에 연결됩니다.
* 비밀번호 등은 프로젝트 설정 파일(`application.properties`)에 맞춰 주세요.

---
