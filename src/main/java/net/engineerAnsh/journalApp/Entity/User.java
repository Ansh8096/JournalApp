package net.engineerAnsh.journalApp.Entity;

import lombok.*;
import net.engineerAnsh.journalApp.enums.Role;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "Users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private ObjectId id;

    @Indexed(unique = true)
    @NonNull
    private String username;

    @NonNull
    private String password;

    private String email;

    private boolean sentimentAnalysis;

    private String city;

    private List<Role> roles;

    // '@DBRef' Stands for Database Reference And it Creates a relationship (link) between this User document and another collection (Journal)...
    // It means this 'journalEntries' arrayList will hold the reference of the 'journal_entries' that are present in the Journal...
    // After writing 'DBRef' the parent-child relationship will get established...
    @DBRef
    private List<Journal> journals;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private String profileImageUrl;

    private String profileImagePublicId;

}
