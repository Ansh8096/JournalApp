package net.engineerAnsh.journalApp.Repository;

import lombok.Data;
import net.engineerAnsh.journalApp.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Data
public class UserRepositoryImpl {

    @Autowired
    private MongoTemplate mongoTemplate; // 'MongoTemplate' is a class that allows us  to directly interact with the mongoDB

    // This method is like creating a 'Query' to fetch details from the 'Database'...
    public List<User> getUserForSA(){
        Query query = new Query();
        query.addCriteria(Criteria.where("email").regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"));
        query.addCriteria(Criteria.where("sentimentAnalysis").is(true));
        query.addCriteria(Criteria.where("userName").nin("Gangodhar")); // It will exclude the Users who has name "Gangodhar" in the dataBase...
        query.addCriteria(Criteria.where("roles").in("User","ADMIN"));
        List<User> users = mongoTemplate.find(query, User.class); // this is best example of ORM(Object-relational-mapping)...
        return users;
    }









/*    // this is how we can use 'AND' or 'OR' operator...
    Query query = new Query();
    Criteria criteria = new Criteria();
        query.addCriteria(criteria.andOperator(
                Criteria.where("email").exists(true),
                criteria.where("sentimentAnalysis").is(true))
            );     */
}
