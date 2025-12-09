package net.engineerAnsh.journalApp.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {

    @NotEmpty
    @Schema(description = "The user's username")
    private String userName;

    @NotEmpty
    @Schema(description = "The user's password")
    private String password;
}
