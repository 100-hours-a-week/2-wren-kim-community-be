package ktb.community.be.domain.member.dao;

import ktb.community.be.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    /**
     * 현재 활성 회원 여부와 관계없이 동일 이메일 또는 deleted_이메일_UUID 형식 포함 회원 모두 조회
     * - 복수 탈퇴 계정 존재 시 복구 우선순위 판단용
     * - 사용 위치: 로그인 시 탈퇴 회원 복구 로직 (AuthService, MemberService)
     */
    @Query("SELECT m FROM Member m WHERE m.email = :email OR m.email LIKE CONCAT('deleted_', :email, '\\_%') escape '\\'")
    List<Member> findAllByEmailIncludingDeleted(@Param("email") String email);

    /**
     * 탈퇴 후 30일이 경과했지만 아직 익명화되지 않은 회원 조회
     * - 익명화 기준: email NOT LIKE 'deleted_%_%'
     * - 사용 위치: 스케줄러 기반 자동 익명화 로직 (MemberService.processExpiredDeletedAccounts())
     */
    @Query("SELECT m FROM Member m " +
            "WHERE m.isDeleted = true " +
            "AND m.deletedAt < :threshold " +
            "AND m.email NOT LIKE 'deleted\\_%\\_%' escape '\\'")
    List<Member> findExpiredAndNotAlreadyMarked(@Param("threshold") LocalDateTime threshold);
}
