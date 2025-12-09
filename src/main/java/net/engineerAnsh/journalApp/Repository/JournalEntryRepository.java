package net.engineerAnsh.journalApp.Repository;

import net.engineerAnsh.journalApp.Entity.JournalEntries;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

// This is a Spring Data MongoDB repository interface...
// It acts as a bridge between your Java application and the MongoDB database...
// It provides built-in CRUD operations (Create, Read, Update, Delete) without writing any SQL or MongoDB queries manually...
public interface JournalEntryRepository extends MongoRepository<JournalEntries, ObjectId> {

}

