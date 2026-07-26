package net.engineerAnsh.journalApp.Dto.journals;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalPageResponseDto {

    private List<JournalSummaryDto> journals;

    private int page;

    private int size;

    private long totalElements;

    private int totalPages;

    private boolean first;

    private boolean last;
}