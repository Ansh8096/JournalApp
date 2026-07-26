package net.engineerAnsh.journalApp.Dto.common;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponseDto {

    private LocalDateTime timestamp;

    private int status;

    private String error;

    private String message;

    private String path;

    private String field;

    private Map<String, String> validationErrors;
}