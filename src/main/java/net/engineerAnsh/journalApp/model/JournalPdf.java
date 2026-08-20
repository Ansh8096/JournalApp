package net.engineerAnsh.journalApp.model;

public record JournalPdf(
        byte[] content,
        String fileName
) {}