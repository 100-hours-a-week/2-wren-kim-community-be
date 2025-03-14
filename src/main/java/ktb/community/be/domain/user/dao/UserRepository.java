package ktb.community.be.domain.user.dao;

import ktb.community.be.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
