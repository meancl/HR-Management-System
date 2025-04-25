# HR Management System

## 프로젝트 개요

HR Management System은 사내 인사 정보를 통합 관리하는 시스템으로, 출근 기록, 급여, 휴가 등 다양한 인사 데이터를 처리할 수 있도록 설계된 백엔드 중심의 프로젝트입니다.  
이 프로젝트는 실무 환경에서 활용 가능한 구조를 목표로 설계되었으며, 성능 개선과 안정성 확보를 주요 목표로 개발되었습니다.

---

## 주요 기능

- 사원 등록 및 정보 조회
- 부서/직책 관리
- 출결 기록 등록 및 통계 처리
- 급여 및 연봉 계산 관리
- 휴가 신청 및 사용 내역 관리
- 공지사항 등록 및 첨부파일 업로드
- 관리자용 통계 API 제공
- 대용량 데이터 처리 시 병렬 처리 및 Kafka 연동

---

## 기술 스택

- **백엔드**: Java 17, Spring Boot 3, Spring Data JPA
- **DB**: MySQL 8, Redis
- **메시징**: Apache Kafka
- **빌드 도구**: Gradle
- **기타**: JUnit 5, Lombok

---

## 성능 개선 사례

- 출결 요청 처리 시 단일 vs 배치 전략 비교 및 혼합 처리 설계
- Nginx + Redis + Kafka 기반 멀티서버 구조로 TPS 향상
- Cursor 기반 Pagination으로 통계 처리 속도 개선
- 분기별 통계 데이터 처리 시 멀티스레드 도입으로 처리 시간 단축

---
## 폴더 구조 예시

```
src/
└── main/
    ├── java/
    │   └── com/
    │       └── hr_mgr/
    │           ├── attendance/
    │           ├── employee/
    │           ├── salary/
    │           └── ...
    └── resources/
        ├── application.yml
        └── sql/
```
---
## 실행 방법

```bash
./gradlew build
java -jar build/libs/hr-management-system.jar
```
