package net.engineerAnsh.journalApp.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.engineerAnsh.journalApp.Entity.JournalEntries;
import net.engineerAnsh.journalApp.Entity.User;
import net.engineerAnsh.journalApp.Service.JournalEntryService;
import net.engineerAnsh.journalApp.Service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.*;


@RestController
@RequestMapping("/Journal")
@Tag(name = "Journal APIs" , description = "Read, Create, Update or Delete Journals")
public class JournalEntryControllerV2 {

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private UserService userService;

    @GetMapping // This handles HTTP 'GET' requests at '/Journal'...
    @Operation(summary = "Get all the journal entries of a user")
    public ResponseEntity<List<JournalEntries>> getAllJournalEntriesOfUser() {
        Authentication userVerified = SecurityContextHolder.getContext().getAuthentication();
        String userName = userVerified.getName();
        User userbyName = userService.findByUserName(userName);
        List<JournalEntries> userJournalEntries = userbyName.getJournalEntries();
        if (!userJournalEntries.isEmpty()) {
            return new ResponseEntity<>(userJournalEntries, HttpStatus.OK);
        }
        else return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    @Operation(summary = "Create a new journal entry for a user")
    public ResponseEntity<JournalEntries> createEntry(@RequestBody JournalEntries entry) { // @RequestBody → tells Spring to automatically convert the JSON request body into a JournalEntries Java object...
        Authentication userVerified = SecurityContextHolder.getContext().getAuthentication();
        String userName = userVerified.getName();
        try {
            journalEntryService.saveEntry(entry, userName); // here: we should use 'saveEntry()' instead of 'saveNewUser()' because we don't want password to get encoded multiple times...
            return new ResponseEntity<>(entry, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("id/{Id}")
    @Operation(summary = "Get journal entry of a user by id")
    public ResponseEntity<JournalEntries> getJournalEntriesOfUserById(@PathVariable String Id) { // '@PathVariable long myId' This tells Spring Boot: “ Take the value from the URL (inside { }) and pass it to this method parameter. ”
        ObjectId myId = new ObjectId(Id);
        // we will get authentication of particular user, when we will send the right credentials of the user...
        Authentication userVerified = SecurityContextHolder.getContext().getAuthentication();
        String userVerifiedName = userVerified.getName();

        // After getting the user, we will check if the journal entry stored in the user has same id , to what we give in 'pathVar' (i.e myId)...
        // If the user journalEntry 'id' == myId , then our collectedList will not be empty...
        // else our collectedList will be empty...
        User user = userService.findByUserName(userVerifiedName);
        List<JournalEntries> collectedList = user.getJournalEntries().stream().filter(x -> x.getId().equals(myId)).toList();
        if (!collectedList.isEmpty()) { // means (journalEntry 'id' == myId)...
            Optional<JournalEntries> opEntry = journalEntryService.findByID(myId);
            if (opEntry.isPresent()) {
                return new ResponseEntity<>(opEntry.get(), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("id/{Id}")
    @Operation(summary = "Delete the journal entry of the user by id")
    // → This defines an endpoint that listens for HTTP DELETE requests, (e.g - DELETE /Journal/id/101) ...
    public ResponseEntity<?> deleteJournalEntryById(@PathVariable String Id) { // '@PathVariable long myId' → Binds the '101' from the URL to the Java variable myId.
        ObjectId myId = new ObjectId(Id);
        Authentication userVerified = SecurityContextHolder.getContext().getAuthentication();
        String userVerifiedName = userVerified.getName();
        boolean removed = journalEntryService.deleteById(userVerifiedName, myId);
        if (removed) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("id/{Id}") // → Defines an endpoint for HTTP POST requests that will modify (or add) a journal entry for a specific ID...
    @Operation(summary = "Update the journal entry of a user by id")
    public ResponseEntity<?> modifyingJournalEntryById(@PathVariable String Id, @RequestBody JournalEntries newEntry) {
        ObjectId myId = new ObjectId(Id);
        Authentication userVerified = SecurityContextHolder.getContext().getAuthentication();
        String userVerifiedName = userVerified.getName();
        User user = userService.findByUserName(userVerifiedName);
        List<JournalEntries> collectedList = user.getJournalEntries().stream().filter(x -> x.getId().equals(myId)).toList();
        if (!collectedList.isEmpty()) {
            Optional<JournalEntries> journalEntry = journalEntryService.findByID(myId);
            if (journalEntry.isPresent()) {
                JournalEntries oldEntry = journalEntry.get();
                oldEntry.setTitle(newEntry.getTitle() != null && !newEntry.getTitle().equals("") ? newEntry.getTitle() : oldEntry.getTitle());
                oldEntry.setContent(newEntry.getContent() != null && !newEntry.getContent().equals("") ? newEntry.getContent() : oldEntry.getContent());
                journalEntryService.saveEntry(oldEntry);
                return new ResponseEntity<>(oldEntry, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}

// Best practice :
// Controller ---> Service ---> Repository