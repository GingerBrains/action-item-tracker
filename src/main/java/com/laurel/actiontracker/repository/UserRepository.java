package com.laurel.actiontracker.repository;

import com.laurel.actiontracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByEmail(String email);
    boolean existsByRole(User.Role role);
}
