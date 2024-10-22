package streamingsettlement.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import streamingsettlement.user.domain.User;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
}
