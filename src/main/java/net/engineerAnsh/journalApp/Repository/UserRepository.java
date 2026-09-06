package net.engineerAnsh.journalApp.Repository;

import net.engineerAnsh.journalApp.Entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository
        extends MongoRepository<User, ObjectId> {

    User findByUsername(
            String username
    );

    void deleteByUsername(
            String username
    );

    boolean existsByEmail(
            String email
    );

    boolean existsByUsername(
            String username
    );

    /**
     * Finds a user linked to a Google account.
     */
    Optional<User> findByGoogleSubject(
            String googleSubject
    );

    /**
     * Checks whether a Google identity is already linked
     * to a JournalFlow account.
     */
    boolean existsByGoogleSubject(
            String googleSubject
    );

    Optional<User> findByEmail(String email);
}