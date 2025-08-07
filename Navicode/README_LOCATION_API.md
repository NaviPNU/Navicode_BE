# Navicode 위치 API

Flask 애플리케이션을 Spring Boot로 변환한 위치 기반 API입니다.

## 주요 기능

1. **좌표 타입 확인** (`/coord_type`)
2. **동적 좌표 조회** (`/get_coord_dynamic`)
3. **정적 좌표 조회** (`/get_coord_static`)
4. **위치 추가** (`/add_coord_location`) - **사용자별 만료 시스템 추가**

## 사용자별 만료 시스템

### 기능
- **사용자 정보**: 모든 코드 등록 시 username 기록
- **동적 코드 (type=1)**: 무조건 admin으로 저장, 1년 후 만료
- **정적 코드 (type=2)**: 사용자 이름으로 저장
  - username에 "admin" 포함: 1년 후 만료
  - 일반 사용자: 3일 후 만료
- **자동 삭제**: 매일 자정에 만료된 코드 자동 삭제
- **스케줄러**: `@Scheduled(cron = "0 0 0 * * ?")`로 매일 자정 실행

### 동작 방식
1. 코드 등록 시 사용자 타입에 따라 만료 날짜 자동 설정
2. 매일 자정에 만료된 코드를 자동으로 삭제
3. 만료된 코드는 더 이상 조회되지 않음

## API 엔드포인트

### 1. 좌표 타입 확인
```
GET /api/location/coord_type?navicode={navicode}
```

**응답:**
```json
{
  "type": "1"  // 1: dynamic, 2: static
}
```

### 2. 동적 좌표 조회
```
GET /api/location/get_coord_dynamic?navicode={navicode}&latitude={latitude}&longitude={longitude}
```

**응답:**
```json
[
  {
    "name": "스타벅스 강남점",
    "latitude": 37.123456,
    "longitude": 127.123456
  }
]
```

### 3. 정적 좌표 조회
```
GET /api/location/get_coord_static?navicode={navicode}
```

**응답:**
```json
{
  "name": "스타벅스 강남점",
  "longitude": 127.123456,
  "latitude": 37.123456
}
```

### 4. 위치 추가 (업데이트)
```
POST /api/location/add_coord_location
Content-Type: application/json

// 동적 코드 등록 (type=1, username 무시됨, 자동으로 admin으로 설정)
{
  "name": "스타벅스 신규점",
  "navicode": "12345",
  "latitude": 37.123456,
  "longitude": 127.123456,
  "type": 1
}

// 정적 코드 등록 (type=2, username 필수)
{
  "name": "테스트정적코드",
  "navicode": "5555",
  "latitude": 37.123456,
  "longitude": 127.123456,
  "type": 2,
  "username": "testuser"
}

// admin이 포함된 사용자 정적 코드 등록 (1년 후 만료)
{
  "name": "테스트어드민코드",
  "navicode": "6666",
  "latitude": 37.123456,
  "longitude": 127.123456,
  "type": 2,
  "username": "adminuser"
}
```

**응답:**
```json
{
  "message": "location added success"
}
```

## 데이터베이스 설정

애플리케이션은 H2 인메모리 데이터베이스를 사용합니다. 
`starbucks.csv` 파일이 프로젝트 루트에 있으면 애플리케이션 시작 시 자동으로 데이터를 로드합니다.

## 실행 방법

1. 프로젝트 루트에서 다음 명령어 실행:
```bash
./gradlew bootRun
```

2. 애플리케이션이 `http://localhost:8080`에서 실행됩니다.

## CSV 파일 형식

`starbucks.csv` 파일은 다음 형식이어야 합니다:
```csv
name,navicode,latitude,longitude,type,username,expire
스타벅스 강남점,12345,37.123456,127.123456,1,admin,2026-08-06
스타벅스 홍대점,12346,37.123457,127.123457,2,testuser,2025-08-09
```

- `name`: 매장명
- `navicode`: 네비코드
- `latitude`: 위도
- `longitude`: 경도
- `type`: 타입 (1: dynamic, 2: static)
- `username`: 사용자 이름 (type=1인 경우 무조건 admin, type=2인 경우 사용자 이름)
- `expire`: 만료 날짜 (yyyy-MM-dd 형식)

## 사용 예시

### 코드 등록 예시

1. **동적 코드 등록 (1년 후 만료):**
```bash
curl -X POST "http://localhost:8080/api/location/add_coord_location" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "테스트동적코드",
    "navicode": "4444",
    "latitude": 37.123456,
    "longitude": 127.123456,
    "type": 1
  }'
```

2. **일반 사용자 정적 코드 등록 (3일 후 만료):**
```bash
curl -X POST "http://localhost:8080/api/location/add_coord_location" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "테스트정적코드",
    "navicode": "5555",
    "latitude": 37.123456,
    "longitude": 127.123456,
    "type": 2,
    "username": "testuser"
  }'
```

3. **admin이 포함된 사용자 정적 코드 등록 (1년 후 만료):**
```bash
curl -X POST "http://localhost:8080/api/location/add_coord_location" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "테스트어드민코드",
    "navicode": "6666",
    "latitude": 37.123456,
    "longitude": 127.123456,
    "type": 2,
    "username": "adminuser"
  }'
```

### 자동 만료 시스템

- **동적 코드**: 무조건 admin으로 저장, 1년 후 만료
- **정적 코드**: 
  - username에 "admin" 포함: 1년 후 만료
  - 일반 사용자: 3일 후 만료
- **자동 삭제**: 매일 자정에 만료된 코드 자동 삭제
- **로그**: 만료된 코드 삭제 시 콘솔에 로그 출력 