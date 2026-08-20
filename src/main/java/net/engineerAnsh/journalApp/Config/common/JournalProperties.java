package net.engineerAnsh.journalApp.Config.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "journal")
public class JournalProperties {

    private final Tags tags = new Tags();

    @Data
    public static class Tags {

        private int maxCount = 10;

        private int maxLength = 30;
    }

}