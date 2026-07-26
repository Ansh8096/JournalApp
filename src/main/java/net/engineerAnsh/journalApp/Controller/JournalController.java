package net.engineerAnsh.journalApp.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.engineerAnsh.journalApp.Dto.journals.CreateJournalRequestDto;
import net.engineerAnsh.journalApp.Dto.journals.JournalResponseDto;
import net.engineerAnsh.journalApp.Service.JournalService;
import net.engineerAnsh.journalApp.Service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/Journal")
@Tag(name = "Journal APIs", description = "Read, Create, Update or Delete Journals")
public class JournalEntryController {

    @Autowired
    private JournalService journalService;

    @Autowired
    private UserService userService;

    @PostMapping
    @Operation(summary = "Create a new journal entry for a user")
    public ResponseEntity<JournalResponseDto> createEntry(@RequestBody CreateJournalRequestDto entry) { // @RequestBody → tells Spring to automatically convert the JSON request body into a Journal Java object...
        try {
            JournalResponseDto journalEntry = journalService.saveEntry(entry);// here: we should use 'saveEntry()' instead of 'saveNewUser()' because we don't want password to get encoded multiple times...
            return new ResponseEntity<>(journalEntry, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("id/{id}")
    @Operation(summary = "Get journal entry of a user by id")
    public ResponseEntity<?> getJournalEntriesOfUserById(@PathVariable String id) { // '@PathVariable long myId' This tells Spring Boot: “ Take the value from the URL (inside { }) and pass it to this method parameter. ”
        // validate ObjectId first
        if (!ObjectId.isValid(id)) {
            return ResponseEntity
                    .badRequest()
                    .body("Invalid journal entry id");
        }
        ObjectId myId = new ObjectId(id);
        try {
            JournalResponseDto journalEntry = journalService.getJournalEntryById(myId);
            if (journalEntry != null) return new ResponseEntity<>(journalEntry, HttpStatus.OK);
            else return new ResponseEntity<>("Journal Entry Not Found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("id/{id}")
    @Operation(summary = "Delete the journal entry of the user by id")
    // → This defines an endpoint that listens for HTTP DELETE requests, (e.g - DELETE /Journal/id/101) ...
    public ResponseEntity<?> deleteJournalEntryById(@PathVariable String id)  // '@PathVariable long myId' → Binds the '101' from the URL to the Java variable myId.
    {   // validate ObjectId first
        if (!ObjectId.isValid(id)) {
            return ResponseEntity
                    .badRequest()
                    .body("Invalid journal entry id");
        }

        ObjectId myId = new ObjectId(id);
        boolean removed = journalService.deleteTheJounalEntryById( myId);
        if (removed) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("id/{id}")
    // → Defines an endpoint for HTTP POST requests that will modify (or add) a journal entry for a specific ID...
    @Operation(summary = "Update the journal entry of a user by id")
    public ResponseEntity<?> modifyingJournalEntryById(@PathVariable String id, @RequestBody CreateJournalRequestDto newEntry) {
        // validate ObjectId first
        if (!ObjectId.isValid(id)) {
            return ResponseEntity
                    .badRequest()
                    .body("Invalid journal entry id");
        }
        ObjectId myId = new ObjectId(id);
        try {
            JournalResponseDto journalEntry = journalService.modifyJournalEntryById(myId, newEntry);
            if (journalEntry != null) return new ResponseEntity<>(journalEntry, HttpStatus.OK);
            else return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/journal-entries")
    @Operation(summary = "Get all journal entries of a user")
    public ResponseEntity<?> getAllJournalEntries() {
        List<JournalResponseDto> journalEntriesOFUser = userService.getAllJournalEntriesOFUser();
        return new ResponseEntity<>(journalEntriesOFUser, HttpStatus.OK);
    }

}

// Best practice :
// Controller ---> Service ---> Repository