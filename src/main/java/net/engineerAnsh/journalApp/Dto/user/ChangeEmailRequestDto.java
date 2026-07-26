package net.engineerAnsh.journalApp.Dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeEmailRequestDto {

    @Email
    private String newEmail;

    @NotBlank
    private String password;
}