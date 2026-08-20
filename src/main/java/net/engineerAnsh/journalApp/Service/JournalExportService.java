package net.engineerAnsh.journalApp.Service;

import lombok.RequiredArgsConstructor;
import net.engineerAnsh.journalApp.Utils.FileNameGenerator;
import net.engineerAnsh.journalApp.enums.JournalStatus;
import net.engineerAnsh.journalApp.export.JournalHtmlTemplateBuilder;
import net.engineerAnsh.journalApp.export.JournalPdfGenerator;
import net.engineerAnsh.journalApp.model.JournalPdf;
import net.engineerAnsh.journalApp.Entity.Journal;
import net.engineerAnsh.journalApp.Entity.User;
import net.engineerAnsh.journalApp.exception.exceptions.ResourceNotFoundException;
import org.bson.types.ObjectId;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JournalExportService {

    private final UserService userService;
    private final JournalPdfGenerator journalPdfGenerator;
    private final JournalHtmlTemplateBuilder journalHtmlTemplateBuilder;
    private final FileNameGenerator fileNameGenerator;

    private User getAuthenticatedUser() {

        String username =
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName();

        User user =
                userService.findUserByUserName(username);

        if (user == null) {
            throw new ResourceNotFoundException(
                    "User not found."
            );
        }

        return user;
    }

    private Journal findUserJournal(
            User user,
            ObjectId journalId
    ) {

        return user.getJournals()
                .stream()
                .filter(journal ->
                        journal.getId().equals(journalId))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Journal not found."
                        ));
    }

    @Transactional(readOnly = true)
    public JournalPdf downloadJournal(
            ObjectId journalId
    ) {

        User user = getAuthenticatedUser();

        Journal journal =
                findUserJournal(user, journalId);

        if (journal.getStatus() != null &&
                journal.getStatus() != JournalStatus.PUBLISHED) {

            throw new ResourceNotFoundException(
                    "Journal not found."
            );
        }

        String html =
                journalHtmlTemplateBuilder.build(journal);

        byte[] pdf =
                journalPdfGenerator.generate(html);

        return new JournalPdf(
                pdf,
                fileNameGenerator.journalPdfFileName(
                        journal.getTitle(),
                        journal.getCreatedAt()
                )
        );

    }
}