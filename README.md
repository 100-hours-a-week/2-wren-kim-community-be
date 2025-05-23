# 게시글 커뮤니티 백엔드 서버

> Spring Boot + JWT 기반의 실전형 커뮤니티 API 서버
>
>
> 회원가입부터 게시글·댓글 CRUD, 이미지 업로드, 인증/보안, 트랜잭션 처리까지 실제 서비스 수준의 흐름을 도메인 중심으로 구현하고, JPA 성능 최적화와 예외 처리, 테스트 커버리지 확보까지 고려한 프로젝트입니다.
>

---

## 1. 프로젝트 개요 (What & Why)

**한 줄 요약**

JWT 기반 인증·보안·테스트 구조를 반영한 실전형 커뮤니티 백엔드 프로젝트입니다.

**배경과 목적**

단순 CRUD를 넘어서 사용자 인증, 이미지 처리, 댓글 계층 구조, 소프트 삭제, 토큰 블랙리스트 등 실제 서비스에서 발생하는 요구사항을 반영한 게시판 시스템을 직접 설계했습니다.

**기술적 차별성**

- `Soft Delete + 복구 + 자동 익명화` 흐름을 포함한 회원 탈퇴 구조
- `Redis + Bloom Filter` 기반 JWT 블랙리스트 처리
- `@EntityGraph + BatchSize` 조합으로 N+1 병목 해결
- 커서 기반 무한스크롤 조회 + 이미지 순서 조정 기능

---

## 2. 기술 스택 (Tech Stack)

- **Language**: Java 21
- **Framework**: Spring Boot 3.4.3
- **ORM**: Hibernate, Spring Data JPA
- **Security**: Spring Security, JWT
- **Infra**: Redis (Token Blacklist, Bloom Filter)
- **Database**: MySQL 8.0.41
- **Build**: Gradle
- **Test**: JUnit5, Mockito, JaCoCo
- **Cloud (예정)**: AWS S3, CloudFront (이미지 업로드 및 정적 리소스)

---

## 3. 시스템 아키텍처 (Architecture)

- **레이어드 아키텍처**: Controller → Service → Repository
- **토큰 인증 흐름**: JWT + Refresh 토큰 + Redis 블랙리스트
- **소프트 삭제**: deletedAt 기반 + 복구 로직
- **댓글/이미지 계층 구조**: depth 1 제한 구조, 순서 조정 지원

---

## 4. 주요 기능 (Core Features)

### 회원 기능

- 소셜/이메일 기반 회원가입 및 로그인
- JWT 인증 / 로그아웃 / 토큰 재발급
- 프로필 이미지 등록 및 수정
- Soft Delete 기반 탈퇴 → 30일 후 자동 익명화 및 복구 지원

### 게시글 기능

- 게시글 작성/조회/수정/삭제 (이미지 포함)
- 커서 기반 페이지네이션
- 좋아요 개수 조회 최적화 (분리 API, 캐시 구조 예정)

### 댓글 및 좋아요

- 댓글/대댓글 계층 구조 (depth 1)
- Soft Delete 및 작성자 정보 마스킹
- 좋아요 토글 방식 구현 및 Soft Delete 적용

---

## 5. 성능 최적화 및 트러블슈팅

### N+1 병목 제거

- `@EntityGraph`, `@BatchSize`, `@Formula`를 활용해 조회 최적화
- SQL 로그 기반 실행 쿼리 수 추적 및 병목 튜닝

### 커서 기반 페이지네이션

- OFFSET → 커서 방식으로 전환 → 무한스크롤 대응 + 성능 개선

### 토큰 보안 구조

- Redis + Bloom Filter 조합으로 AccessToken 블랙리스트 처리
- TTL 설정으로 메모리 효율 확보 및 빠른 조회

---

## 6. API 문서 (Docs)

- [🔗 Swagger 문서 바로가기 (추후 적용)](https://github.com/seplease/community/docs)
- 주요 예외 응답 형식:

```json
{
  "status": 400,
  "message": "닉네임은 2~20자여야 합니다.",
  "data": null
}
```

- API 설계 방식: RESTful + HTTP 상태 코드 준수 + 명확한 URI 규칙 (`/api/posts`, `/api/comments`, `/api/auth`)

---

## 7. 테스트 및 보안 구조

- 테스트 방식: 단위 테스트 + 통합 테스트 + 보안 흐름 검증
- `@WithMockUser` 기반 인증 시나리오 검증
- JWT 예외, 복구, 중복 검증까지 커버
- 테스트 커버리지: `전체 80% 이상` 확보 (JaCoCo 기준)

---

## 8. 배포 및 운영

- 현재는 로컬 기반 개발
- 향후 AWS 기반 인프라 이전 예정:
    - 이미지 → S3
    - CI/CD → GitHub Actions → ECR → ECS
    - 환경 분리: `dev`, `prod`

---

## 9. 실행 방법 / 로컬 실행 가이드

```bash
git clone https://github.com/seplease/community.git
cd community
./gradlew bootRun
```

- 필요 환경:
    - Java 21
    - MySQL 8.x (로컬)
    - Redis 서버 실행 필요 (`localhost:6379`)
- `.env.example` 제공 예정

---

## 10. 프로젝트 회고 / 느낀 점

> 이 프로젝트는 단순한 CRUD 구현을 넘어 인증, 보안, 트랜잭션, 성능 최적화 등 서비스 전반을 고려하여 설계·개발한 경험이었습니다.
>
>
> 특히, JWT 기반 인증 흐름, Soft Delete 및 복구 처리, Redis 기반 토큰 무효화 설계 등을 통해 실무에서 빈번하게 마주하는 이슈를 직접 해결하며 백엔드 설계 역량을 높일 수 있었습니다.
>
> 앞으로도 설계 의도를 설명할 수 있는 개발자, 사용자와 비즈니스 요구를 동시에 고려하는 백엔드 엔지니어로 성장하고자 합니다.
>