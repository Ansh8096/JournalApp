package net.engineerAnsh.journalApp.Service;

import lombok.extern.slf4j.Slf4j;
import net.engineerAnsh.journalApp.Dto.journals.CreateJournalRequestDto;
import net.engineerAnsh.journalApp.Dto.journals.JournalResponseDto;
import net.engineerAnsh.journalApp.Entity.Journal;
import net.engineerAnsh.journalApp.Entity.User;
import net.engineerAnsh.journalApp.Repository.JournalEntryRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// You could also use @Service here (both work similarly in this case)...
@Component
@Slf4j
public class JournalEntryService {

    // '@Autowired' Automatically injects (connects) an instance of JournalEntryRepository into this class...
    // So you don’t need to create an object manually — Spring does it for you...
    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private UserService userService;

    private JournalResponseDto mapToJournalEntryResponse(Journal journalEntry) {
        return new JournalResponseDto(
                journalEntry.getId().toHexString(),
                journalEntry.getTitle(),
                journalEntry.getContent(),
                journalEntry.getMood(),
                journalEntry.getCreatedAt(),
                journalEntry.getUpdatedAt()
        );
    }

    private String getLoginUser() {
        Authentication userAuthenticated = SecurityContextHolder.getContext().getAuthentication(); // 'SecurityContextHolder.getContext().getAuthentication() '-> gives the current logged-in user...
        return userAuthenticated.getName();
    }

    @Transactional // @Transactional → ensures the whole process happens as one unit of work. If any step fails, all DB operations in this method are rolled back...
    public JournalResponseDto saveEntry(CreateJournalRequestDto request) {
        try {
            String userName = getLoginUser();
            User userByName = userService.findUserByUserName(userName);
            if(userByName == null) throw new RuntimeException("User not Found");
            Journal myEntry = Journal.builder()
                    .content(request.getContent())
                    .title(request.getTitle())
                    .mood(request.getMood())
                    .build();

            Journal savedEntry = journalEntryRepository.save(myEntry);
            userByName.getJournalEntries().add(savedEntry);
            userService.saveEntry(userByName);

            return mapToJournalEntryResponse(savedEntry);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("An error occurred while saving the Journal Entry ");
        }
    }

    @Transactional
    public boolean deleteTheJounalEntryById(ObjectId id) {
        String userName = getLoginUser();
        boolean removed = false;
        try {
            User userByName = userService.findUserByUserName(userName);
            if(userByName == null) throw new RuntimeException("User not Found");
            removed = userByName.getJournalEntries().removeIf(x -> x.getId().equals(id));
            if (removed) {
                userService.saveEntry(userByName);
                journalEntryRepository.deleteById(id);
            }
            return removed;
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("An error occurred while deleting the entry...", e);
        }
    }

    public JournalResponseDto getJournalEntryById(ObjectId myId) {
        // After getting the user, we will check if the journal entry stored in the user has same id , to what we give in 'pathVar' (i.e myId)...
        // If the user journalEntry 'id' == myId , then our collectedList will not be empty...
        // else our collectedList will be empty...
        String userVerifiedName = getLoginUser();
        User user = userService.findUserByUserName(userVerifiedName);
        if(user == null) throw new RuntimeException("User not Found");
        List<Journal> collectedList = user.getJournalEntries()
                .stream()
                .filter(x -> x.getId().equals(myId))
                .toList();
        if (!collectedList.isEmpty()) { // means (journalEntry 'id' == myId)...
            Journal journalEntry = journalEntryRepository.findById(myId)
                    .orElseThrow(() -> new RuntimeException("Journal Entry is not found..."));
            return mapToJournalEntryResponse(journalEntry);
        }
        return null;
    }

    public JournalResponseDto modifyJournalEntryById(ObjectId myId, CreateJournalRequestDto newEntry) {
        String userVerifiedName = getLoginUser();
        User user = userService.findUserByUserName(userVerifiedName);
        if(user == null) throw new RuntimeException("User not Found");

        List<Journal> collectedList = user.getJournalEntries()
                .stream()
                .filter(x -> x.getId().equals(myId))
                .toList();

        if (!collectedList.isEmpty()) {
            Journal journalEntry = journalEntryRepository.findById(myId)
                    .orElseThrow(() -> new RuntimeException("Journal Entry is not found..."));

            journalEntry.setTitle(newEntry.getTitle() != null && !newEntry.getTitle().equals("") ? newEntry.getTitle() : journalEntry.getTitle());
            journalEntry.setContent(newEntry.getContent() != null && !newEntry.getContent().equals("") ? newEntry.getContent() : journalEntry.getContent());
            journalEntry.setMood((newEntry.getMood() != null) ? newEntry.getMood() : journalEntry.getMood());
            journalEntryRepository.save(journalEntry);
            return mapToJournalEntryResponse((journalEntry));
        } else return null;
    }

}

