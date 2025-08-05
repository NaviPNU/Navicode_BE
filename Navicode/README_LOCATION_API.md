# Navicode 위치 API

Flask 애플리케이션을 Spring Boot로 변환한 위치 기반 API입니다.

## 주요 기능

1. **좌표 타입 확인** (`/coord_type`)
2. **동적 좌표 조회** (`/get_coord_dynamic`)
3. **정적 좌표 조회** (`/get_coord_static`)
4. **위치 추가** (`/add_coord_location`)

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

### 4. 위치 추가
```
POST /api/location/add_coord_location
Content-Type: application/json

{
  "name": "스타벅스 신규점",
  "navicode": "12345",
  "latitude": 37.123456,
  "longitude": 127.123456,
  "type": 1
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
name,navicode,latitude,longitude,type
스타벅스 강남점,12345,37.123456,127.123456,1
스타벅스 홍대점,12346,37.123457,127.123457,2
```

- `name`: 매장명
- `navicode`: 네비코드
- `latitude`: 위도
- `longitude`: 경도
- `type`: 타입 (1: dynamic, 2: static) 