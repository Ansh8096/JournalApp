package net.engineerAnsh.journalApp.Service;

import net.engineerAnsh.journalApp.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service // → Marks this class as a Spring service component, so it can be injected where needed...
public class UserDetailsServiceImpl implements UserDetailsService { // UserDetailsService → This is the core interface Spring Security uses to look up users during login , Spring Security calls this service automatically whenever someone tries to log in (using HTTP Basic, form login, JWT, etc.)...

    @Autowired
    UserRepository userRepository;

    // Spring Security calls this method whenever a login attempt is made with a username (because of our logic)...
    // The userName argument comes from the login credentials (from the HTTP Basic Auth header ,for example)...
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        net.engineerAnsh.journalApp.Entity.User userByName = userRepository.findByUserName(userName);

        // Since this method needs instance of 'UserDetails' , so we will build this instance with the help of our 'userByName'...
        if(userByName != null){
            return org.springframework.security.core.userdetails.User.builder()
                    .username(userByName.getUserName()) // → sets the username for authentication...
                    .password(userByName.getPassword()) // → sets the hashed password (must be encoded with BCrypt)...
                    .roles(userByName.getRoles().toArray(new String[0])) // assigns roles (like "USER", "ADMIN", etc...
                    .build(); // returns a UserDetails object that Spring Security uses internally to authenticate and authorize.
        }
        throw new UsernameNotFoundException("User not found with username: " + userName);
    }
}
