package net.engineerAnsh.journalApp.mapper;

import net.engineerAnsh.journalApp.Dto.user.UserSummaryDto;
import net.engineerAnsh.journalApp.Entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserSummaryDto toSummaryDto(User user) {

        return UserSummaryDto.builder()
                .id(user.getId().toHexString())
                .username(user.getUsername())
                .email(user.getEmail())
                .city(user.getCity())
                .sentimentAnalysisEnabled(user.isSentimentAnalysis())
                .roles(user.getRoles())
                .build();
    }

}
