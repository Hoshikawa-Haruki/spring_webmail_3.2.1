# spring_webmail_3.2.1
객체지향설계 웹메일프로젝트

## 🛠️ James 서버 실행 방법

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
