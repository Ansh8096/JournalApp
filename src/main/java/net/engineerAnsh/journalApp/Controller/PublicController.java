package net.engineerAnsh.journalApp.Controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.engineerAnsh.journalApp.Dto.LoginDto;
import net.engineerAnsh.journalApp.Dto.SignUpDto;
import net.engineerAnsh.journalApp.Entity.User;
import net.engineerAnsh.journalApp.Service.UserDetailsServiceImpl;
import net.engineerAnsh.journalApp.Service.UserService;
import net.engineerAnsh.journalApp.Utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
@Slf4j
@Tag(name = "Public APIs" , description = "healthCheck, login or signup")
public class PublicController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @GetMapping("health-Check")
    @Operation(summary = "Application's current health status")
    public String healthCheckup(){
        return "ok";
    }

    @PostMapping("/signup")
    @Operation(summary = "Registers a new user into the system")
    public ResponseEntity<User> signUp(@RequestBody SignUpDto signUpDto){
        try {
            // We are creating the newUser(obj. of User-entity), and fetching details from the UserDto object...
            // Then saving this newUser...
            User newUser = new User();
            newUser.setUserName(signUpDto.getUserName());
            newUser.setPassword(signUpDto.getPassword());
            newUser.setEmail(signUpDto.getEmail());
            newUser.setSentimentAnalysis(signUpDto.isSentimentAnalysis());

            userService.saveNewUser(newUser);
            return new ResponseEntity<>(newUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticates a user and returns an access token")
    public ResponseEntity<String> logIN(@RequestBody LoginDto loginDto){
        try {
            // This 'authenticate()' is internally calling our 'UserDetailsServiceImpl' to authenticate the requested user...
            authenticationManager.authenticate(new
                    UsernamePasswordAuthenticationToken(loginDto.getUserName(),
                    loginDto.getPassword()));

            // And also load the user by using 'loadUserByUsername()' (present inside the UserDetailsServiceImpl)...
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginDto.getUserName());

            String jwt = jwtUtils.generateToken(userDetails.getUsername());

            return new ResponseEntity<>(jwt,HttpStatus.OK);
        } catch (Exception e){
            log.error("Exception occurred while createAuthenticationToken ", e);
            return new ResponseEntity<>("Incorrect username or password" , HttpStatus.BAD_REQUEST);
        }
    }

}
