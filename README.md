# 게시글 커뮤니티 백엔드 서버

> **Spring Boot + JPA 기반의 커뮤니티 API 서버입니다.** <br>
> 게시글, 댓글, 좋아요, 회원가입 및 인증, 회원 탈퇴 등 전체 흐름을 도메인 주도적으로 설계하고, **JPA 성능 최적화, 트랜잭션 처리, 예외 처리, 인증 흐름 설계** 등을 고민하며 개발했습니다.

## 기술 스택

| 구분 | 기술                                    |
| --- |---------------------------------------|
| Language | Java 21                               |
| Framework | Spring Boot 3.4.3                     |
| DB | MySQL 8.0.41                          |
| ORM | Spring Data JPA, Hibernate            |
| Security | Spring Security, JWT                  |
| Infra | Redis (Token Blacklist, Bloom Filter) |
| Build Tool | Gradle                                |
| Cloud | (현재는 로컬 저장 기반, 향후 AWS S3 고려)          |

## 기능 요약
### 회원
- 회원가입 (유효성 검사, 커스텀 Exception 적용)
- 로그인 (JWT 기반 인증)
- 회원 정보 수정 (닉네임, 프로필 이미지)
- 비밀번호 변경 (복잡도 검사 포함)
- 회원 탈퇴 (Soft Delete + 30일 후 데이터 익명화)

### 게시글
- 게시글 작성 (다중 이미지 업로드, 트랜잭션 처리)
- 게시글 상세 조회 (작성자, 이미지, 댓글, 좋아요 포함 / N+1 문제 해결)
- 게시글 수정 (Soft Delete + 순서 변경, 변경 감지 활용)
- 게시글 삭제 (연관 데이터 일괄 Soft Delete + Batch Update)
- 게시글 전체 조회 (커서 기반 페이지네이션)

### 댓글 & 좋아요
- 댓글/대댓글 작성, 수정, 삭제 (계층 구조 유지 + Soft Delete)
- 좋아요 추가/취소 (토글 방식, Soft Delete 적용)
- 좋아요 개수 조회 API 분리 → 성능 최적화

### 인증 및 로그아웃
- Spring Security + JWT
- Refresh Token 관리
- Access Token 블랙리스트 (Redis + Bloom Filter + TTL)

## 성능 최적화 핵심 포인트
### 1. JPA N+1 문제 해결
- @EntityGraph + @BatchSize + @Formula를 조합하여, 한 쿼리로 작성자, 댓글, 좋아요 수, 이미지를 함께 조회
- Hibernate SQL 로그 기반으로 실제 쿼리 실행 수를 분석하고 튜닝

```java
@EntityGraph(attributePaths = {"author"})
@Query("SELECT p FROM Post p WHERE p.id = :id AND p.deletedAt IS NULL")
Optional<Post> findByIdAndDeletedAtIsNull(@Param("id") Long id);
```

### 2. 커서 기반 페이지네이션
- createdAt 기준 커서 방식 도입으로 무한 스크롤 UX 대응
- OFFSET 기반 성능 저하 문제 해결

### 3. 이미지/댓글 Soft Delete 및 최소 UPDATE 처리
- isDeleted 플래그와 deletedAt 타임스탬프를 적용하여 데이터 복구 가능성 확보
- 불필요한 DELETE & INSERT를 줄이고 필요한 필드만 업데이트

### 4. 로그아웃 보안 강화
- Bloom Filter + Redis TTL 조합으로 Access Token 블랙리스트 구현
- Redis 키 조회 없이도 빠르게 유효성 판단 가능

```java
bloomClient.add("accessTokenBlacklist", accessToken);
redisTemplate.opsForValue().set("blacklist:" + accessToken, "true", ttl, TimeUnit.MILLISECONDS);
```

## 주요 설계/개선 고민
| 문제 | 해결 |
| --- | --- |
| JPA에서 연관 데이터 조회 시 다수의 쿼리 발생 (N+1) | `@EntityGraph`, `@BatchSize`로 즉시 로딩 처리 |
| 댓글/이미지의 계층 구조 or 중복 조회 | Stream API + HashMap 계층 매핑 + Soft Delete 처리 |
| 게시글 수정 시 모든 이미지를 삭제/재등록 | 기존 이미지 유지 + 필요한 항목만 Soft Delete/Insert |
| Access Token 무효화가 어려움 | Access Token 무효화가 어려움 |

## 예외 및 테스트 케이스 설계
- 커스텀 예외 코드 (ErrorCode) 기반 글로벌 예외 처리
- JWT 토큰 오류, 유효하지 않은 입력값, DB 조회 실패 등 주요 에러 명확하게 응답
- Hibernate SQL 로그 분석 기반으로 기능별 쿼리 흐름 검증

## 향후 개선 사항
- 이미지 업로드 → AWS S3 또는 CloudFront CDN 적용
- 캐싱 전략 → Redis 활용한 조회수/좋아요 수/인기글 캐싱
- Kafka, Event 기반 비동기 처리 고려 (ex. 조회수, 좋아요 기록)

<details markdown="1">
  <summary>마무리 및 느낀 점</summary>
  <div>
    <ul>
      <li>이 프로젝트는 단순한 CRUD 구현을 넘어, 성능과 구조, 보안, 사용자 경험을 모두 고려하며 실무적인 관점에서 고민한 작업이었습니다.</li>
      <li>JPA 최적화, 계층 구조 설계, 트랜잭션 처리, Redis 블랙리스트 구축 등, 단기간의 실습이 아닌 실제 서비스 개발을 목표로 설계·개발·테스트를 반복했습니다.</li>
      <li>앞으로도 "왜 이렇게 설계해야 하는가?"를 고민하는 개발자로서 성장을 이어가겠습니다.</li>
    </ul>
  </div>
</details>