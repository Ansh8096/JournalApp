package net.engineerAnsh.journalApp.export;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.engineerAnsh.journalApp.Entity.Journal;
import net.engineerAnsh.journalApp.exception.exceptions.PdfGenerationException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JournalHtmlTemplateBuilder {

    private final SpringTemplateEngine templateEngine;
    private final ResourceLoader resourceLoader;
    private String styles;

    private String loadCss() {

        try {

            Resource resource =
                    resourceLoader.getResource("classpath:pdf/journal.css");

            return new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

        } catch (IOException ex) {

            throw new PdfGenerationException(
                    "Failed to load PDF stylesheet.",
                    ex
            );
        }
    }

    @PostConstruct
    void initialize() {
        styles = loadCss();
    }

    public String build(Journal journal) {

        Context context = new Context();

        context.setVariable("journal", journal);
        context.setVariable("styles", styles);

        return templateEngine.process(
                "pdf/journal-template",
                context
        );
    }
}