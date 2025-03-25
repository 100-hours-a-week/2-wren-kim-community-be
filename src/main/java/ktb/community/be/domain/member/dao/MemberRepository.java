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
     * 회원 탈퇴
     */
    @Query("SELECT m FROM Member m WHERE m.email = :email")
    Optional<Member> findByEmailIncludingDeleted(@Param("email") String email);

    /**
     * 30일이 지난 탈퇴 회원 조회
     */
    List<Member> findAllByIsDeletedTrueAndDeletedAtBefore(LocalDateTime dateTime);
}
