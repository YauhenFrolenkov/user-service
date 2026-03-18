package com.innowise.user.repository;

import com.innowise.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = "cards")
    Page<User> findAll(Specification<User> spec, Pageable pageable);

    @EntityGraph(attributePaths = "cards")
    Optional<User> findWithCardsById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(Long id);

    @Query(value = "SELECT * FROM users u WHERE u.id = :id", nativeQuery = true)
    Optional<User> findByIdIncludingInactive(@Param("id") Long id);

}
