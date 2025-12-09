package net.engineerAnsh.journalApp.Config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Journal APP API's",
                version = "1.0",
                description = "By Ansh"
        ),
        tags = {
                @Tag(name = "Public APIs" , description = "healthCheck, login or signup"),
                @Tag(name = "User APIs" , description = "Read, Update & Delete User"),
                @Tag(name = "Journal APIs" , description = "Read, Create, Update or Delete Journals"),
                @Tag(name = "Admin APIs" , description = "Get-All-Users, Create-Admin & Run-App-Cache")
        }
)
public class SwaggerTagConfig {

}
