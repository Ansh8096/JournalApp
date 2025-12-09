package net.engineerAnsh.journalApp.Service;

import net.engineerAnsh.journalApp.Entity.User;
import net.engineerAnsh.journalApp.Repository.UserRepository;
import net.engineerAnsh.journalApp.Utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class GoogleAuthService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    public ResponseEntity<?> GoogleCallback(String code,String clientId, String clientSecret){
        try {
            String tokenEndPoint = "https://oauth2.googleapis.com/token"; // this the end-point of the 'Google' which we will hit to get the token...

            // These are the parameters we must, send it along with the request of generating token...
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("redirect_uri","https://developers.google.com/oauthplayground"); // We need to send the 'redirect_uri' (which we send initially) again for security purposes...
            params.add("grant_type", "authorization_code"); // it means I have 'authorization_code' and I want to exchange it for the token...

            // With the help of headers, we tell google which type of request we are sending to it...
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // We are creating the object of the request which we will be sending to google with the help of restTemplate...
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            // Now we are using the restTemplate to Hit the 'tokenEndPoint'...
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenEndPoint, request, Map.class);

            String idToken = (String) tokenResponse.getBody().get("id_token"); // we have received the id_token...
            String userInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken; // Now we will create the url, which on hitting will give us user info...
            // Now we are hitting the 'userInfoUrl' to get the user info, by restTemplate...
            ResponseEntity<Map> UserInfoResponse = restTemplate.getForEntity(userInfoUrl, Map.class);

            if (UserInfoResponse.getStatusCode() == HttpStatus.OK) {
                Map<String,Object> userInfo = UserInfoResponse.getBody();
                String email = (String) userInfo.get("email"); // Extracting the email from the response...
                UserDetails userDetails = null;
                try{
                    // we are checking if the user is already present...
                    userDetails = userDetailsService.loadUserByUsername(email);
                }catch (Exception e){
                    // we are creating the new User, if it was not present...
                    User user = new User();
                    user.setEmail(email);
                    user.setUserName(email);
                    user.setRoles(List.of("USER"));
                    user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // this generates a random password...
                    user.setJournalEntries(new ArrayList<>());
                    userRepository.save(user); // saving user...
                }

                // Now returning a JWT-Token corresponding of user's email...
                String JwtToken = jwtUtils.generateToken(email);
                return ResponseEntity.ok(Collections.singletonMap("Token",JwtToken));
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch(Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}






// This is the API of Google that frontEnd is supposed to hit with redirected-uri
/*
https://accounts.google.com/o/oauth2/auth?
client_id=YOUR_CLIENT_ID
    &redirect_uri=YOUR_REDIRECT_URI
    &response_type=code
    &scope=email profile
    &access_type=offline
    &prompt=consent
*/