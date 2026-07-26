package net.engineerAnsh.journalApp.Repository;

import net.engineerAnsh.journalApp.Entity.Journal;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

// This is a Spring Data MongoDB repository interface...
// It acts as a bridge between your Java application and the MongoDB database...
// It provides built-in CRUD operations (Create, Read, Update, Delete) without writing any SQL or MongoDB queries manually...
@Repository
public interface JournalRepository extends MongoRepository<Journal, ObjectId> {

}

