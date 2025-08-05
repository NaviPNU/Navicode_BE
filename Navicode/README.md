# Navicode Backend

## 개발 환경 설정

### 1. 로컬 환경 (H2 데이터베이스) - 권장
가장 간단한 방법으로, MySQL 설치 없이 바로 사용할 수 있습니다.

```bash
./gradlew bootRun
```

환경변수 설정 (선택사항):
```bash
export H2_USERNAME=your_h2_username
export H2_PASSWORD=your_h2_password
export JWT_SECRET=your_jwt_secret_key
export JWT_EXPIRATION=3600000
```

### 2. MySQL 환경 설정

#### MySQL 설치
```bash
# macOS
brew install mysql
brew services start mysql

# Windows
# MySQL 공식 사이트에서 다운로드 후 설치

# Linux
sudo apt install mysql-server
```

#### 데이터베이스 생성
```bash
mysql -u root -p
CREATE DATABASE navicode CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```

#### 환경변수로 MySQL 계정 설정 (권장)
```bash
export DB_USERNAME=your_mysql_username
export DB_PASSWORD=your_mysql_password
./gradlew bootRun --args='--spring.profiles.active=dev'
```

#### 또는 properties 파일에 직접 설정
`src/main/resources/application-dev.properties` 파일을 수정:
```properties
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
```
그 후 실행:
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```
## 환경변수 목록
- `H2_USERNAME`: H2 데이터베이스 사용자명 (기본값: sa)
- `H2_PASSWORD`: H2 데이터베이스 비밀번호 (기본값: password)
- `DB_USERNAME`: MySQL 사용자명 (기본값: root)
- `DB_PASSWORD`: MySQL 비밀번호 (기본값: 빈 값)
- `JWT_SECRET`: JWT 비밀키 (기본값: 제공됨)
- `JWT_EXPIRATION`: JWT 만료시간 (기본값: 3600000ms)

## 프로파일 설명
- **기본 (local)**: H2 인메모리 데이터베이스 사용, 개발 시작 시 권장
- **dev**: MySQL 데이터베이스 사용, 실제 DB 환경 테스트 시 사용
