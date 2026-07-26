package net.engineerAnsh.journalApp.Dto.user;

import lombok.*;
import net.engineerAnsh.journalApp.enums.Role;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryDto {

    private String id;

    private String username;

    private String email;

    private String city;

    private boolean sentimentAnalysisEnabled;

    private List<Role> roles;
}