package net.engineerAnsh.journalApp.Dto;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data // this single notation takes care for @Getter , @Setter , @EqualsAndHashCode, @RequiredArgsConstructor...
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    @Id
    private int id;
    private String userName;
    private String password;
    private String email;
    private List<String> roles;
    private String city;
    private boolean sentimentAnalysis;
    private List<JournalEntriesDto> journalEntriesDto;


}
