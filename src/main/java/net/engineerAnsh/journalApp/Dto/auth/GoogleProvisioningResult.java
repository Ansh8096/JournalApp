package net.engineerAnsh.journalApp.Dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import net.engineerAnsh.journalApp.Entity.User;
import net.engineerAnsh.journalApp.enums.GoogleProvisioningStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleProvisioningResult {

    private GoogleProvisioningStatus status;

    private User user;
}