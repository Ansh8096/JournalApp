package net.engineerAnsh.journalApp.Schedular;

import lombok.extern.slf4j.Slf4j;
import net.engineerAnsh.journalApp.Cache.AppCache;
import net.engineerAnsh.journalApp.Entity.JournalEntries;
import net.engineerAnsh.journalApp.Entity.User;
import net.engineerAnsh.journalApp.Repository.UserRepositoryImpl;
import net.engineerAnsh.journalApp.Service.EmailService;
import net.engineerAnsh.journalApp.enums.Sentiment;
import net.engineerAnsh.journalApp.model.SentimentData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class UserSchedular {

    @Autowired
    private UserRepositoryImpl userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private KafkaTemplate<String ,SentimentData> kafkaTemplate;

    @Scheduled(cron = "0 0 9 * * SUN")
    public void fetchUsersAndSendSaMail() {
        List<User> Users = userRepository.getUserForSA();
        for(User user : Users){
            List<JournalEntries> journalEntries = user.getJournalEntries();
            List<Sentiment> sentimentsList = journalEntries.stream()
                    .filter(x -> x.getDate()
                            .isAfter(LocalDateTime.now()
                                    .minus(7, ChronoUnit.DAYS)))
                    .map(x -> x.getSentiment())
                    .toList();

            // We will be mapping all the sentiments present in the journal entries with their frequency into a map...
            Map<Sentiment,Integer> mppOfSentiments = new HashMap<>();
            for(Sentiment sentiment : sentimentsList){
                if(sentiment != null){
                    mppOfSentiments.put(sentiment,mppOfSentiments.getOrDefault(sentiment,0)+1);
                }
            }

            // We will be storing the sentiment that has maximum frequency the user's journal entries...
            int maxCount = 0;
            Sentiment mostFrequentSentiment = null;
            for(Map.Entry<Sentiment, Integer> sentimentInMap : mppOfSentiments.entrySet()){
                if(sentimentInMap.getValue() > maxCount){
                    maxCount = sentimentInMap.getValue();
                    mostFrequentSentiment = sentimentInMap.getKey();
                }
            }

            // we are sending mail to the user about their highest frequency sentiment...
            if(mostFrequentSentiment != null){
                // emailService.sendingEmail(user.getEmail(), "Sending the sentiment for last 7 days", mostFrequentSentiment.toString());

                SentimentData sentimentData = SentimentData.builder().email(user.getEmail()).sentiment("Sentiment for last 7 days " + mostFrequentSentiment).build();

                // We will be sending mail synchronously if kafka throws an error...
                try {
                    kafkaTemplate.send("weekly-sentiments",sentimentData.getEmail(),sentimentData);
                    System.out.println("Serializer: " + kafkaTemplate);
                } catch (Exception e) {
                    log.info("kafka is not active, mail is send synchronously...");
                    emailService.sendingEmail(sentimentData.getEmail(),"Sentiment for last 7 days ", sentimentData.getSentiment());
                }
            }
        }
    }


    @Autowired
    private AppCache appCache;

    @Scheduled (cron = "0 */10 * ? * *")
    public void clearAppCache(){
        appCache.init();
    }
}
