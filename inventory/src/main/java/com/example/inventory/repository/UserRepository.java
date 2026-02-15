package com.example.inventory.repository;

import com.example.inventory.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    @Query("SELECT u.id FROM User u WHERE u.username = :username")
    Long getIdByUsername(@Param("username") String username);
    @Query("SELECT u.id FROM User u WHERE u.password = :password")
    Long getIdByPassword(@Param("password") String password);
    Optional<User> findByUsername(String username);


}