package net.engineerAnsh.journalApp.Entity;

import lombok.*;
import net.engineerAnsh.journalApp.enums.Sentiment;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

// This is known as 'POJO' (Plain-Old-Java-Object) class...
// POJO — a simple Java class with fields, getters, and setters.

// This annotation maps (known as ORM) the class as a MongoDB document, meaning each object of this class will be stored as a document in a MongoDB collection...
// By default, the collection name will be the class name (journalEntries), but you can set it manually:--
//@Document(collection = "Journal_entries")
@Document
@Data // this single notation takes care for @Getter , @Setter , @EqualsAndHashCode, @AllArgsConstructor, @NoArgsConstructor...
@NoArgsConstructor // This is required during deserialization (JASON -> POJO) , we need to do manually because it will not be in '@Data'...
public class JournalEntries {

    @Id // @Id → Marks the field as the primary key in MongoDB...
    private ObjectId id; // ObjectId → Is MongoDB’s built-in unique identifier type...
    @NonNull
    private String title;
    private String content;
    private LocalDateTime date;
    private Sentiment sentiment;

}
