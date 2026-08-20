package net.engineerAnsh.journalApp.Entity;

import lombok.*;
import net.engineerAnsh.journalApp.enums.JournalStatus;
import net.engineerAnsh.journalApp.enums.Mood;
import net.engineerAnsh.journalApp.model.JournalImage;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// This annotation maps (known as ORM) the class as a MongoDB document, meaning each object of this class will be stored as a document in a MongoDB collection...
// By default, the collection name will be the class name (journalEntries), but you can set it manually:--
// @Document(collection = "Journal_entries")
@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Journal {

    @Id // @Id → Marks the field as the primary key in MongoDB...
    private ObjectId id; // ObjectId → Is MongoDB’s built-in unique identifier type...

    private String title;

    private String content;

    private Mood mood;

    // Identifier of the image used as the journal cover.
    // References one of the images stored in {@code images}.
    private String coverImageUrl;

    @Builder.Default // Using @Builder.Default ensures that Lombok's builder initializes it to false unless explicitly overridden.
    private List<JournalImage> images = new ArrayList<>();

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Builder.Default
    private JournalStatus status = JournalStatus.PUBLISHED;

    @Builder.Default
    private boolean favorite = false;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

}