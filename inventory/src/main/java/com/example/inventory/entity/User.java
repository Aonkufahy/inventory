package com.example.inventory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Entity
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String username;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role {
        ADMIN("/api/admin/dashboard"),
        USER("/api/user/home");

        private final String redirectPath;

        Role(String redirectPath) {
            this.redirectPath = redirectPath;
        }

        public String getRedirectPath() {
            return redirectPath;
        }
    }
}
