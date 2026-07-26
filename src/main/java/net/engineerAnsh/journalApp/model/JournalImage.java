package net.engineerAnsh.journalApp.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalImage {

    private String imageUrl;

    private String publicId;
}