package com.anju.dto;

import com.anju.entity.User;
import com.anju.entity.User.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
// import lombok.Data;
// import lombok.Builder;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
public class UserCreateRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", 
             message = "Password must contain both letters and numbers")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    public UserCreateRequest() {
    }

    public UserCreateRequest(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public static User toEntity(UserCreateRequest request, String encodedPassword) {
        return User.builder()
                .username(request.getUsername())
                .passwordHash(encodedPassword)
                .role(request.getRole())
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String username;
        private String password;
        private Role role;

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public UserCreateRequest build() {
            return new UserCreateRequest(username, password, role);
        }
    }
}
