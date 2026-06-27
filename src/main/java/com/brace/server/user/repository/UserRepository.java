package com.brace.server.user.repository;

import com.brace.server.user.entity.User;
import com.brace.server.user.entity.SocialProvider;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    Optional<User> findBySocialProviderAndSocialIdAndDeletedAtIsNull(SocialProvider socialProvider, String socialId);

    List<User> findAllByDeletedAtBefore(LocalDateTime deletedBefore);

    boolean existsByEmail(String email);
}
