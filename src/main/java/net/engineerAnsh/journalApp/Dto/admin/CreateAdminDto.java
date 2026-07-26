package net.engineerAnsh.journalApp.Dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAdminDto {

    @NotBlank(message = "Username is required")
    @Schema(description = "The user's username")
    private String username;

    @NotBlank(message = "Email is required")
    @Schema(description = "The user's email")
    private String email;

}
