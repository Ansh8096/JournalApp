package net.engineerAnsh.journalApp.Services;

import net.engineerAnsh.journalApp.Entity.User;
import net.engineerAnsh.journalApp.Repository.UserRepository;
import net.engineerAnsh.journalApp.Service.UserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest // This notation is used when we want to start application context
@ActiveProfiles("dev") // It means all the things which will of 'dev' profile will get used here...
public class UserServiceTest {

    @AfterAll // Runs once after all tests have finished...
    public static void setUp(){

    }

    @BeforeEach // Runs before every test method...
    public void setUpBeforeEveryTest(){

    }

    @AfterEach // Runs after every test method...
    public void setUpAfterEveryTest(){
    }


    @Autowired
    UserRepository userRepository; // these will only get inject by springBoot during runTime, so we should run this file for our tests to work smoothly...

    @Disabled // It disables the test...
    @Test
    public void testAdd(){
        assertEquals(4,2+2);
    }


    @Disabled
    @ParameterizedTest // '@ParameterizedTest' is used when we want to give our own custom input...
    @ValueSource(strings = {
            "AnshVerma",
            "AV",
            "Shyam"
    })
    public void testByUsername(String name){
        assertNotNull(userRepository.findByUserName(name),"Message failed for : " + name);
    }


    @Disabled
    @Test
    public void testForJournalEntries(){
        User user = userRepository.findByUserName("AnshVerma");
        assertTrue(!user.getJournalEntries().isEmpty()); // it will tell us if there is journal entries present or not...
    }


    @Disabled
    @ParameterizedTest // '@ParameterizedTest' is used when we want to give our own custom input...
    // '@CsvSource' is used to run test for multiple inputs...
    @CsvSource({
           "2,2,4",  // a,b,expected
           "5,5,10",
           "2,5,8"
    })
    public void testForMultipleInput(int a, int b, int expected){
        assertEquals(expected,a+b);
    }


    @Autowired
    UserService userService;

    @Disabled
    @ParameterizedTest
    @ArgumentsSource(UserArgumentProvider.class) // This tells 'JUnit' to get the input values from a custom provider class named UserArgumentProvider...
    public void testSavingTheUser(User user){
        assertTrue(userService.testingSaveNewUser(user));
    }
}
