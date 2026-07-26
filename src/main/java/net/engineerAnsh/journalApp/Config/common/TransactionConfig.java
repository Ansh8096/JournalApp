package net.engineerAnsh.journalApp.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement // Enables declarative transaction management, meaning you can use the @Transactional annotation in your service methods...
public class TransactionConfig {

    @Bean // '@Bean' :- Tells Spring to create and manage this object in the application context...
    // 'PlatformTransactionManager' : This is Spring’s standard interface for transaction management , we need to tell our project to use object of 'MongoTransactionManager' ...
    public PlatformTransactionManager falana(MongoDatabaseFactory dbFactory){
        return new MongoTransactionManager(dbFactory); // 'MongoDatabaseFactory' helps us to make connection with the dataBase...
    }
}

// 'PlatformTransactionManager' --> 'MongoTransactionManager' (it is the implementation of 'PlatformTransactionManager')...