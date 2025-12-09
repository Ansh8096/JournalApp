package net.engineerAnsh.journalApp.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.engineerAnsh.journalApp.Entity.User;
import net.engineerAnsh.journalApp.Repository.JournalEntryRepository;
import net.engineerAnsh.journalApp.Repository.UserRepository;
import net.engineerAnsh.journalApp.Service.UserService;
import net.engineerAnsh.journalApp.Service.WeatherService;
import net.engineerAnsh.journalApp.api.responses.WeatherResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController // @RestController: Marks this as a REST controller — meaning it will handle HTTP requests and automatically convert Java objects to JSON in responses...
@RequestMapping("/Users")
@Tag(name = "User APIs" , description = "Read, Update & Delete User")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @PutMapping
    @Operation(summary = "Update the current user")
    public ResponseEntity<?> updateUserEntry(@RequestBody User user){
        Authentication userAuthenticated = SecurityContextHolder.getContext().getAuthentication(); // whenever a user gets authenticated , it's details get stored in 'SecurityContextHolder'...
        String userName  = userAuthenticated.getName(); // Extracts the username (the one used to log in)...
        User userInDb = userService.findByUserName(userName); // getting the user by its userName...
        if(!user.getUserName().isEmpty()) {
            userInDb.setUserName(user.getUserName());
        }
        if(!user.getPassword().isEmpty()) {
            userInDb.setPassword(user.getPassword());
        }
        userService.saveNewUser(userInDb);
        return new ResponseEntity<>(userInDb,HttpStatus.NO_CONTENT);
    }

    @Autowired
    private UserRepository userRepository;

    @DeleteMapping
    @Operation(summary = "Delete the current user")
    public ResponseEntity<?> DeleteUserById(){
        Authentication userAuthenticated = SecurityContextHolder.getContext().getAuthentication(); // Again, 'SecurityContextHolder.getContext().getAuthentication() '-> gives the current logged-in user...
        String userName = userAuthenticated.getName();
        User user = userService.findByUserName(userName);
        user.getJournalEntries().forEach(entry -> journalEntryRepository.deleteById(entry.getId()));
        userRepository.deleteByUserName(userName);// Deletes the user directly from MongoDB using their username...
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Autowired
    private WeatherService weatherService;

    @GetMapping
    @Operation(summary = "Greetings from the user")
    public ResponseEntity<?> greetingsByUser(){
        Authentication userAuthenticated = SecurityContextHolder.getContext().getAuthentication();
        String name = userAuthenticated.getName();
        String userCity = userService.findByUserName(name).getCity();
        WeatherResponse weatherResponse = weatherService.getWeather(userCity);
        String greeting = "";
        if(weatherResponse != null){
           greeting = " weather feels like " + weatherResponse.getCurrent().getFeelsLike();
        }
        return new ResponseEntity<>("Hi " + name + greeting ,HttpStatus.OK);
    }
}

