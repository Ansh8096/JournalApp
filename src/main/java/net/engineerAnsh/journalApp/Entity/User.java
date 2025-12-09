package net.engineerAnsh.journalApp.Entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "Users")
@Data // this single notation takes care for @Getter , @Setter , @EqualsAndHashCode, @RequiredArgsConstructor...
@Builder // Lombok → adds the builder pattern for easy object creation...
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private ObjectId id;

    // These notations are available because of 'project lombok'...
    @Indexed(unique = true) // It will make sure that userName of every user must be unique...
    @NonNull
    private String userName;

    @NonNull
    private String password;

    private String email;
    private boolean sentimentAnalysis;
    private String city;

    private List<String> roles;

    // '@DBRef' Stands for Database Reference And it Creates a relationship (link) between this User document and another collection (JournalEntries)...
    // It means this 'journalEntries' arrayList will hold the reference of the 'journal_entries' that are present in the JournalEntries...
    // After writing 'DBRef' the parent-child relationship will get established...
    @DBRef
    private List<JournalEntries> journalEntries;


}
