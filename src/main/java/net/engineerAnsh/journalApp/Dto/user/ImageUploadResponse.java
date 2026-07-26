package net.engineerAnsh.journalApp.Dto.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageUploadResponse {

    private String imageUrl;

    private String publicId;

}