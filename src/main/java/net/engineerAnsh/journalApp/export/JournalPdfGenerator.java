package net.engineerAnsh.journalApp.export;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import net.engineerAnsh.journalApp.exception.exceptions.PdfGenerationException;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class JournalPdfGenerator {

    public byte[] generate(String html) {

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PdfRendererBuilder builder = new PdfRendererBuilder();

            builder.useFastMode();

            builder.withHtmlContent(
                    html,
                    null
            );

            builder.toStream(outputStream);

            builder.run();

            return outputStream.toByteArray();

        } catch (Exception ex) {

            log.error(
                    "Failed to generate journal PDF.",
                    ex
            );

            throw new PdfGenerationException(
                    "Unable to generate journal PDF.",
                    ex
            );
        }
    }
}