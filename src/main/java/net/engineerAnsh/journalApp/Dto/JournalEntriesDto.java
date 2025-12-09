package net.engineerAnsh.journalApp.Dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JournalEntriesDto {
    private String entry;

    public JournalEntriesDto(String entry) {
        this.entry = entry;
    }
}
