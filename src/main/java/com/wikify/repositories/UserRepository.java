package com.wikify.repositories;

import com.wikify.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"memberships", "memberships.department"})
    Optional<User> findByLogin(String login);

    Optional<User> findByEmail(String email);
}
