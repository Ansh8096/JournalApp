package net.engineerAnsh.journalApp.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpDto {

    @NotEmpty
    @Schema(description = "The user's username")
    private String userName;

    @NotEmpty
    @Schema(description = "The user's password")
    private String password;

    @Schema(description = "The user's email")
    private String email;

    @Schema(description = "The user's city")
    private String city;

    @Schema(description = "The user's consent for sentimentAnalysis")
    private boolean sentimentAnalysis;


}
