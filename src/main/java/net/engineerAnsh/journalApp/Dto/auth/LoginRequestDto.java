package net.engineerAnsh.journalApp.Dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

    @NotBlank(message = "Username is required")
    @Schema(description = "The user's username")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "The user's password")
    private String password;
}
