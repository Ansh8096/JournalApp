package net.engineerAnsh.journalApp.Services;

import net.engineerAnsh.journalApp.Service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailServiceTests {
    @Autowired
    EmailService emailService;

    @Test
    public void sendingEmailTest(){
        emailService.sendingEmail("anshverma72099@gmail.com",
                "Testing java mail sender",
                "Hi, app kaise ho? ");
    }
}
