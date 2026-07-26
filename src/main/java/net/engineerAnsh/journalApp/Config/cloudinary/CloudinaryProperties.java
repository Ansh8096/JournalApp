package net.engineerAnsh.journalApp.Config.cloudinary;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cloudinary")
public class CloudinaryProperties {

    private String cloudName;

    private String apiKey;

    private String apiSecret;

    private String profileImageFolder;

    private String journalImageFolder;

}