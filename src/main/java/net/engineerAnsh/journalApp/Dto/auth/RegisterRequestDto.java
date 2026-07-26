package net.engineerAnsh.journalApp.Dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequestDto {

    @Schema(
            description = "Unique username used for login",
            example = "ansh8096"
    )
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30)
    @Pattern(
            regexp = "^[a-zA-Z0-9._]+$",
            message = "Username can contain only letters, numbers, dots and underscores."
    )
    private String username;

    @Schema(
            description = "Account password",
            example = "StrongPassword123"
    )
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must contain at least 8 characters")
    private String password;

    @Schema(
            description = "User's email address",
            example = "ansh@gmail.com"
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    @Schema(
            description = "User's city",
            example = "Chandigarh"
    )
    @Size(max = 100)
    @NotBlank(message = "City is required")
    private String city;

    @Schema(
            description = "Enable weekly mood analysis emails",
            example = "true"
    )
    private boolean sentimentAnalysisEnabled;

}
