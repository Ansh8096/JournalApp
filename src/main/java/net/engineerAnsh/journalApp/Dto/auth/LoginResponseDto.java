package net.engineerAnsh.journalApp.Dto.auth;

import lombok.Builder;
import lombok.Getter;
import net.engineerAnsh.journalApp.Dto.user.UserSummaryDto;

@Getter
@Builder
public class LoginResponseDto {

    private String accessToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private UserSummaryDto user;
}