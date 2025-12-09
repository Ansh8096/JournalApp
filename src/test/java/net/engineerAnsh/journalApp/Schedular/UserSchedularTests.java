package net.engineerAnsh.journalApp.Schedular;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserSchedularTests {

    @Autowired
    private UserSchedular userSchedular;

    @Test
    public void fetchUsersAndSendSaMailTests(){
        userSchedular.fetchUsersAndSendSaMail();
    }

}











//    @Test
//    void testKafkaSend() {
//        SentimentData data = SentimentData.builder()
//                .email("anshv8096@gmail.com")
//                .sentiment("Feeling great this week!")
//                .build();
//        kafkaTemplate.send("weekly-sentiments", data.getEmail(), data);
//    }
