package net.engineerAnsh.journalApp.Service;

import net.engineerAnsh.journalApp.Repository.UserRepository;
import net.engineerAnsh.journalApp.enums.Role;
import net.engineerAnsh.journalApp.exception.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service // → Marks this class as a Spring service component, so it can be injected where needed...
public class UserDetailsServiceImpl implements UserDetailsService { // UserDetailsService → This is the core interface Spring Security uses to look up users during login , Spring Security calls this service automatically whenever someone tries to log in (using HTTP Basic, form login, JWT, etc.)...

    @Autowired
    UserRepository userRepository;

    // Spring Security calls this method whenever a login attempt is made with a username (because of our logic)...
    // The userName argument comes from the login credentials (from the HTTP Basic Auth header ,for example)...
    @Override
    public UserDetails loadUserByUsername(
            String userName
    ) {

        net.engineerAnsh.journalApp.Entity.User userByName =
                userRepository.findByUsername(
                        userName
                );

        if (userByName != null) {

            /*
             * ----------------------------------------
             * Spring Security requires a NON-NULL
             * password inside UserDetails.
             *
             * A Google-only JournalFlow account has
             * no local password in the database.
             *
             * Therefore:
             *
             * LOCAL user   -> use actual BCrypt hash
             * GOOGLE user  -> use empty placeholder
             *
             * The placeholder is NOT stored in MongoDB.
             * ----------------------------------------
             */
            String springSecurityPassword =
                    userByName.getPassword() != null
                            ? userByName.getPassword()
                            : "";

            return org.springframework.security.core.userdetails.User
                    .builder()
                    .username(
                            userByName.getUsername()
                    )

                    /*
                     * LOCAL users have their BCrypt password.
                     * OAuth-only users don't have a local password,
                     * so Spring Security receives an empty placeholder.
                     */

                    .password(
                            springSecurityPassword
                    )
                    .roles(
                            userByName
                                    .getRoles()
                                    .stream()
                                    .map(Role::name)
                                    .toArray(String[]::new)
                    )
                    .build();
        }

        throw new ResourceNotFoundException(
                "Username or Password incorrect."
        );
    }
}
