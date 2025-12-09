package net.engineerAnsh.journalApp.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.engineerAnsh.journalApp.Cache.AppCache;
import net.engineerAnsh.journalApp.Dto.JournalEntriesDto;
import net.engineerAnsh.journalApp.Dto.UserDto;
import net.engineerAnsh.journalApp.Entity.User;
import net.engineerAnsh.journalApp.Repository.JournalEntryRepository;
import net.engineerAnsh.journalApp.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Admin")
@Tag(name = "Admin APIs" , description = "Get-All-Users, Create-Admin & Run-App-Cache")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    AppCache appCache;

    @Autowired
    JournalEntryRepository journalEntryRepository;

    @GetMapping("/all-users")
    @Operation(summary = "See all the existing users")
    public ResponseEntity<?> getAllUsers(){
        List<User> allUsers = userService.getAll();

        if(allUsers != null ){
        // Making Sure that before returning the List of users, we do following steps:
            // 1. Make the journal entries hidden of every single user.(to do this we create a 'JournalEntriesDto' to return as response)
            // 1. Make the password hidden and to place 'JournalEntriesDto' in the users (to do this we create a 'UserDto' to return as response)

            String masked = "***************";
            List<UserDto> maskeduserDtoList = allUsers.stream().map(user -> {
                List<JournalEntriesDto> maskedJournalEntriesDtos = List.of(
                        new JournalEntriesDto(masked)
                );

                return new UserDto(
                        user.getId().getTimestamp(),
                        user.getUserName(),
                        masked,
                        user.getEmail(),
                        user.getRoles(),
                        user.getCity(),
                        user.isSentimentAnalysis(),
                        maskedJournalEntriesDtos
                );
            }).toList();

            return new ResponseEntity<>(maskeduserDtoList, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


    @PostMapping ("/create-admin")
    @Operation(summary = "Create a new admin")
    public ResponseEntity<?> CreateAdmins(@RequestBody User user){
        Authentication Admin = SecurityContextHolder.getContext().getAuthentication();
        userService.saveAdmin(user);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }


    @GetMapping("/clear-app-cache")
    @Operation(summary = "Load the cache manually")
    public void clearAppCache(){
        appCache.init();
    }
}
