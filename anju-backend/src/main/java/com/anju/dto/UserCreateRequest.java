package com.anju.dto;

import com.anju.entity.User;
import com.anju.entity.User.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    public static User toEntity(UserCreateRequest request, String encodedPassword) {
        return User.builder()
                .username(request.getUsername())
                .passwordHash(encodedPassword)
                .role(request.getRole())
                .build();
    }
}
