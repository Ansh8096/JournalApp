package net.engineerAnsh.journalApp.Service;

import net.engineerAnsh.journalApp.model.SentimentData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class SentimentConsumerService {

    @Autowired
    private  EmailService emailService;

//    @KafkaListener(topics = "weekly-sentiments",groupId ="weekly-sentiment-group")
    public void consume(SentimentData sentimentData){
        System.out.println(" Received SentimentData: " + sentimentData);
        sendingEmail(sentimentData);
    }


    public void sendingEmail(SentimentData sentimentData){
        emailService.sendingEmail(sentimentData.getEmail(),"Sentiment for last 7 days ", sentimentData.getSentiment());
    }
}
