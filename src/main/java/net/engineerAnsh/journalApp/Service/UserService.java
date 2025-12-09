package net.engineerAnsh.journalApp.Service;

import lombok.extern.slf4j.Slf4j;
import net.engineerAnsh.journalApp.Entity.User;
import net.engineerAnsh.journalApp.Repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void saveEntry(User user){
        userRepository.save(user);
    }

    // Instead of creating instance again-again like this:--> (We can use '@Slf4j' notation)
    // private static final Logger logger  = LoggerFactory.getLogger(UserService.class);

    public boolean saveNewUser(User user){
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(List.of("User"));
            user.setJournalEntries(new ArrayList<>());
            userRepository.save(user);
            return true;
        } catch (Exception e) {

        // printing our owns logs...
            // logger.info("UserName must be unique..."); // (with instance)
             log.info("UserName must be unique..."); // (with notation)
             log.debug("UserName must be unique..."); // (with notation)

            // here 'e' will print the stack Trace...
            // here '{}' accepts parameter (here: .getUserName()) , We can use multiple '{}' to accept multiple parameters...
            log.error("error occurred for {}" , user.getUserName(),e);
            return false;
        }
    }

    public boolean testingSaveNewUser(User user){
        try {
            if(user.getPassword().isEmpty() && user.getPassword().equals("")) {
                throw new RuntimeException();
            }
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                user.setRoles(List.of("User"));
                userRepository.save(user);
                return true;
        } catch (Exception e) {
            return false;
        }
    }


    public List<User> getAll(){
        return userRepository.findAll();
    }

    // Optional means it can have data or not ...
    public Optional<User> findByID(ObjectId id){
        return userRepository.findById(id);
    }

    public void deleteById(ObjectId id){
        userRepository.deleteById(id);
    }

    public User findByUserName(String userName){
        return userRepository.findByUserName(userName);
    }

    public void saveAdmin(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(List.of("User","ADMIN"));
        User saved = userRepository.save(user);
    }

}

