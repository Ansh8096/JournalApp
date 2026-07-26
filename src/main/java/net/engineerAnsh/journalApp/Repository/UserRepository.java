package net.engineerAnsh.journalApp.Repository;

import jakarta.validation.constraints.Email;
import net.engineerAnsh.journalApp.Entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {
    User findByUsername(String username);

    void deleteByUsername(String username);

    boolean existsByEmail(@Email String email);

    boolean existsByUsername(String username);
}

