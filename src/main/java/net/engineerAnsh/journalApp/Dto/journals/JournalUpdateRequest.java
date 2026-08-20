package net.engineerAnsh.journalApp.Dto.journals;

import net.engineerAnsh.journalApp.enums.Mood;

import java.util.List;

public interface JournalUpdateRequest {

    String getTitle();

    String getContent();

    Mood getMood();

    List<String> getTags();

    List<String> getRemoveImagePublicIds();
}