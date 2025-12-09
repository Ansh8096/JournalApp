package net.engineerAnsh.journalApp.Service;

import lombok.extern.slf4j.Slf4j;
import net.engineerAnsh.journalApp.Entity.JournalEntries;
import net.engineerAnsh.journalApp.Entity.User;
import net.engineerAnsh.journalApp.Repository.JournalEntryRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// You could also use @Service here (both work similarly in this case)...
@Component
public class JournalEntryService {

    // '@Autowired' Automatically injects (connects) an instance of JournalEntryRepository into this class...
    // So you don’t need to create an object manually — Spring does it for you...
    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private UserService userService;

    @Transactional // @Transactional → ensures the whole process happens as one unit of work. If any step fails, all DB operations in this method are rolled back...
    public void saveEntry(JournalEntries myEntry, String userName){
        try {
            User userByName = userService.findByUserName(userName);
            myEntry.setDate(LocalDateTime.now());
            JournalEntries savedEntry = journalEntryRepository.save(myEntry);
            userByName.getJournalEntries().add(savedEntry);
//             userByName.setUserName(null);
            userService.saveEntry(userByName);
        }
        catch (Exception e){
            System.out.println(e);
            throw new RuntimeException("An error occurred while saving the data" + e);
        }
    }

    public void saveEntry(JournalEntries myEntry){
        myEntry.setDate(LocalDateTime.now());
        journalEntryRepository.save(myEntry);
    }

    public List<JournalEntries> getAll(){
        return journalEntryRepository.findAll();
    }

    // Optional means it can have data or not ...
    public Optional<JournalEntries> findByID(ObjectId id){
        return journalEntryRepository.findById(id);
    }

    @Transactional
    public boolean deleteById(String userName, ObjectId id){
        boolean removed = false;
        try {
            User userByName = userService.findByUserName(userName);
            removed = userByName.getJournalEntries().removeIf(x -> x.getId().equals(id));
            if(removed) {
                userService.saveEntry(userByName);
                journalEntryRepository.deleteById(id);
            }
            return removed;
        }catch (Exception e){
            System.out.println(e);
            throw new RuntimeException("An error occurred while deleting the entry...",e);
        }
    }

}

