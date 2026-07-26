package net.engineerAnsh.journalApp.Dto.journals;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalImageResponseDto {

    //  Public Cloudinary URL used by the frontend.
    private String imageUrl;

    private String imagePublicId;
}