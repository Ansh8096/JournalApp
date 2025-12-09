package net.engineerAnsh.journalApp.Services;

import net.engineerAnsh.journalApp.Entity.User;
import net.engineerAnsh.journalApp.Repository.UserRepository;
import net.engineerAnsh.journalApp.Service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.ArrayList;
import static org.mockito.Mockito.*;


// ModernWay of how Mockito automatically initializes all @Mock and @InjectMocks — no need for initMocks() or @BeforeEach...
// This is the recommended modern approach with JUnit 5...
@ExtendWith(MockitoExtension.class) // We use this instead of SpringBoot test...
public class UserDetailsImplTest {

    @InjectMocks // It tells Mockito: To create an instance of UserDetailsServiceImpl and inject any mocks (annotated with @Mock) into its dependencies."...
    private UserDetailsServiceImpl userDetailsService;

    @Mock // @Mock creates a mock object for UserRepository,This is not a real repository — it’s a fake object where you can control the return values...
    private UserRepository userRepository;


// It is an old way for Manual initialization of '@Mock' and '@InjectMocks'...
    // @BeforeEach
    // void setUp(){
        // tells Mockito to scan the current test class (this) for annotations like:
        // @Mock → create mock objects //@InjectMocks → create the class under test and inject mocks...
        // Without this, the @Mock and @InjectMocks annotations won’t be initialized, so your mocks would be null.
    //     MockitoAnnotations.initMocks(this);
    // }


    @Disabled
    @Test
    public void loadUserByUsernameTest(){
        // 'ArgumentMatchers.anyString()' It means return the particular created user when it is called for any String (i.e "Ram","Shyam" etc) ...
        when(userRepository.findByUserName(ArgumentMatchers.anyString())).thenReturn(User.builder().userName("Ram").password("asdfghjk").roles(new ArrayList<>()).build());
        UserDetails user = userDetailsService.loadUserByUsername("ram");
        Assertions.assertNotNull(user);
    }

}
