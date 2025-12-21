package course.repository;

import course.model.User;
import course.model.enums.AccountStatus;
import course.model.enums.MembershipLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByMembershipLevel(MembershipLevel membershipLevel);

    List<User> findByStatus(AccountStatus status);
}
