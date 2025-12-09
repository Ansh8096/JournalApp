package net.engineerAnsh.journalApp.Cache;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import net.engineerAnsh.journalApp.Entity.ConfigJournalAppEntity;
import net.engineerAnsh.journalApp.Repository.ConfigJournalAppRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AppCache {

    @Getter
    public enum Keys{
        WEATHER_API
    }

    @Autowired
    ConfigJournalAppRepo configJournalAppRepository;

    @Getter
    @Setter
    private Map<String,String> appCache = new HashMap<>();

    @PostConstruct // It will automatically run the 'init()' method just after our application is done with creating all beans and injecting dependency...
    public void init(){
        List<ConfigJournalAppEntity> all = configJournalAppRepository.findAll();
        for(ConfigJournalAppEntity entities : all){
            appCache.put(entities.getKey(), entities.getValue());
        }
    }
}