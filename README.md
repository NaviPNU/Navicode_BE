# MySQL 설정 가이드

## H2 사용 (MySQL 없이)

```bash
./gradlew bootRun
```

## MySQL 사용

### 1. MySQL 설치
```bash
# macOS
brew install mysql
brew services start mysql

# Windows
MySQL 공식 사이트에서 다운로드 후 설치

# Linux
sudo apt install mysql-server
```

### 2. 데이터베이스 생성
```bash
mysql -u root -p
CREATE DATABASE navicode;
EXIT;
```

### 3. 실행
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 4. 테스트
```bash
# 회원가입
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "testpass123"}'

# 로그인
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "testpass123"}'
```
