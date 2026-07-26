package net.engineerAnsh.journalApp.Dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteAccountRequestDto {

    @NotBlank(message = "Password is required...")
    private String password;

}