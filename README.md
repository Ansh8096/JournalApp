<h1 style="font-size: 2.5em; color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px">📔 Journal APP – Spring Boot Backend</h1>

<p style="font-size: 1.2em; color: #7f8c8d">
A secure and scalable <strong>Spring Boot backend application</strong> for managing personal journal entries, storing user sentiment, and sending <strong>weekly scheduled sentiment emails</strong>.
</p>

<hr style="border: 2px solid #3498db">

<h2 style="font-size: 2em; color: #2c3e50; margin-top: 30px">1. Project Overview</h2>

<p style="font-size: 1.1em">
<strong>Journal APP</strong> is a Spring Boot–based backend application that allows users to store and manage their personal journal entries securely. In addition to CRUD operations on journals, the application analyzes user sentiment and sends <strong>scheduled weekly emails every Sunday</strong> summarizing the user's last week sentiment.
</p>

<p style="font-size: 1.1em; background: #f8f9fa; padding: 15px; border-left: 4px solid #3498db">
Only eligible users (active users with journal entries) receive these emails.
</p>

<h2 style="font-size: 2em; color: #2c3e50; margin-top: 30px">2. Main Purpose</h2>

<ul style="font-size: 1.1em">
<li>Store user journal entries securely</li>
<li>Send <strong>weekly scheduled emails</strong> containing last 7 days sentiments</li>
<li>Fetch and cache external API data (e.g., weather)</li>
<li>Provide secure authentication and authorization</li>
</ul>

<h2 style="font-size: 2em; color: #2c3e50; margin-top: 30px">3. Key Features</h2>

<ul style="font-size: 1.1em">
<li>User authentication using <strong>JWT & OAuth2 (Google Login)</strong></li>
<li>Journal CRUD operations (Create, Read, Update, Delete)</li>
<li>Weekly sentiment email using <strong>JavaMailSender</strong></li>
<li><strong>Spring Scheduler</strong> to trigger email jobs every Sunday</li>
<li><strong>Redis caching</strong> for external API responses</li>
<li><strong>Kafka integration</strong> for event-driven communication</li>
<li>External API integration using <strong>RestTemplate</strong></li>
<li>API documentation using <strong>Swagger (Springdoc OpenAPI)</strong></li>
</ul>

<h2 style="font-size: 2em; color: #2c3e50; margin-top: 30px">4. Technology Stack</h2>

<ul style="font-size: 1.1em">
<li><strong>Language:</strong> Java</li>
<li><strong>Framework:</strong> Spring Boot</li>
<li><strong>Database:</strong> MongoDB</li>
<li><strong>Caching:</strong> Redis</li>
<li><strong>Messaging:</strong> Apache Kafka</li>
<li><strong>Security:</strong> Spring Security, JWT, OAuth2</li>
<li><strong>Email Service:</strong> JavaMailSender</li>
<li><strong>API Docs:</strong> Swagger (Springdoc OpenAPI)</li>
<li><strong>Deployment:</strong> Render</li>
</ul>

<h2 style="font-size: 2em; color: #2c3e50; margin-top: 30px">5. Project Architecture</h2>

<p style="font-size: 1.1em">
The application follows a <strong>layered architecture</strong>:
</p>

<ul style="font-size: 1.1em">
<li><strong>Controller Layer:</strong> Handles incoming HTTP requests</li>
<li><strong>Service Layer:</strong> Contains business logic</li>
<li><strong>Repository Layer:</strong> Communicates with MongoDB</li>
<li><strong>Config Layer:</strong> Handles security, Redis, Swagger, transactions, and external APIs</li>
</ul>

<h2 style="font-size: 2em; color: #2c3e50; margin-top: 30px">6. Project Structure</h2>

<pre style="font-size: 1.1em; background: #2c3e50; color: #ecf0f1; padding: 15px; border-radius: 5px">
src/main/java
 └── net.engineerAnsh.journalApp
     ├── Cache
     ├── Constants
     ├── controller
     ├── Dto
     ├── Entity
     ├── Filter
     ├── Schedular
     ├── Utils
     ├── api/responses
     ├── model
     ├── service
     ├── repository
     ├── config
     ├── security
     └── JournalApp.java

src/main/resources
 ├── application-dev.yml
 ├── application-prod.yml
 ├── application.yml
 └── logback.xml
</pre>

<h2 style="font-size: 2em; color: #2c3e50; margin-top: 30px">7. Database Design (MongoDB)</h2>

<h3 style="font-size: 1.5em; color: #34495e">User Collection:</h3>

<pre style="font-size: 1.1em; background: #f8f9fa; padding: 15px; border-left: 4px solid #2ecc71">
{
  "_id": "ObjectId",
  "username": "String",
  "password": "Encrypted String",
  "email": "String",
  "sentimentAnalysis": "True/False",
  "journalEntries" :["journalEntryId's"],
  "roles": ["String"],
  "city": "String"
}
</pre>

<h3 style="font-size: 1.5em; color: #34495e">Journal Collection</h3>

<pre style="font-size: 1.1em; background: #f8f9fa; padding: 15px; border-left: 4px solid #2ecc71">
{
  "_id": "ObjectId",
  "title": "String",
  "content": "String",
  "Date": "String",
  "sentiment": "String"
}
</pre>

<h2 style="font-size: 2em; color: #2c3e50; margin-top: 30px">8. Authentication & Authorization</h2>

<h3 style="font-size: 1.5em; color: #34495e">Authentication</h3>

<ul style="font-size: 1.1em">
<li>JWT-based authentication for secured APIs</li>
<li>OAuth2 authentication for <strong>Google Login</strong></li>
</ul>

<h3 style="font-size: 1.5em; color: #34495e">Authorization</h3>

<ul style="font-size: 1.1em">
<li>Role-based access control
  <ul>
  <li><strong>USER:</strong> Journal and profile operations</li>
  <li><strong>ADMIN:</strong> User management and cache operations</li>
  </ul>
</li>
</ul>

<h2 style="font-size: 2em; color: #2c3e50; margin-top: 30px">9. API Endpoints Overview</h2>

<h3 style="font-size: 1.5em; color: #34495e">1. Public APIs</h3>
<ul style="font-size: 1.1em">
<li>Signup</li>
<li>Login</li>
<li>Application Status</li>
</ul>

<h3 style="font-size: 1.5em; color: #34495e">2. User APIs</h3>
<ul style="font-size: 1.1em">
<li>Get user details</li>
<li>Update user</li>
<li>Delete user</li>
<li>Fetch current city weather</li>
</ul>

<h3 style="font-size: 1.5em; color: #34495e">3. Admin APIs</h3>
<ul style="font-size: 1.1em">
<li>Get all users</li>
<li>Create admin</li>
<li>Run application cache</li>
</ul>

<h3 style="font-size: 1.5em; color: #34495e">4. Google Login APIs</h3>
<ul style="font-size: 1.1em">
<li>Login using Google OAuth2</li>
</ul>

<h3 style="font-size: 1.5em; color: #34495e">5. Journal APIs</h3>
<ul style="font-size: 1.1em">
<li>Create journal entry</li>
<li>Read journal entries</li>
<li>Update journal</li>
<li>Delete journal</li>
</ul>

<h2 style="font-size: 2em; color: #2c3e50; margin-top: 30px">10. External API Integration</h2>

<ul style="font-size: 1.1em">
<li>External APIs are consumed using <strong>RestTemplate</strong></li>
<li>API responses are cached in <strong>Redis</strong> to reduce latency and API calls</li>
</ul>

<h2 style="font-size: 2em; color: #2c3e50; margin-top: 30px">11. Redis Caching</h2>

<p style="font-size: 1.1em">
Redis is used to cache frequently accessed external API data such as weather information.
</p>

<pre style="font-size: 1.1em; background: #2c3e50; color: #ecf0f1; padding: 15px; border-radius: 5px">
redisTemplate.opsForValue().set(key, value, 600l);
</pre>

<p style="font-size: 1.1em; background: #fff3cd; padding: 10px; border-left: 4px solid #ffc107">
(Cache duration: 300 seconds)
</p>

<h2 style="font-size: 2em; color: #2c3e50; margin-top: 30px">12. Kafka Integration</h2>

<p style="font-size: 1.1em">
Kafka is used for event-driven tasks such as:
</p>

<ul style="font-size: 1.1em">
<li>Publishing journal or sentiment-related events</li>
<li>Asynchronous processing and scalability</li>
</ul>

<h2 style="font-size: 2em; color: #2c3e50; margin-top: 30px">13. Scheduled Tasks</h2>

<p style="font-size: 1.1em">
Spring's <code>@Scheduled</code> annotation is used to:
</p>

<ul style="font-size: 1.1em">
<li>Send <strong>weekly emails every Sunday</strong></li>
<li>Include sentiment analysis summary of the last 7 days</li>
</ul>

<h2 style="font-size: 2em; color: #2c3e50; margin-top: 30px">14. Email Service</h2>

<ul style="font-size: 1.1em">
<li>Emails are sent using <strong>JavaMailSender</strong></li>
<li>Only eligible users receive weekly sentiment emails</li>
</ul>

<h2 style="font-size: 2em; color: #2c3e50; margin-top: 30px">15. Swagger API Documentation</h2>

<p style="font-size: 1.1em">
Swagger UI provides interactive API documentation.
</p>

<p style="font-size: 1.2em; background: #e8f4fd; padding: 15px; border-radius: 5px">
<strong>Swagger URL:</strong><br>
<code>https://journalapp-p6ur.onrender.com/swagger-ui/index.html</code>
</p>

<h2 style="font-size: 2em; color: #2c3e50; margin-top: 30px">16. Special Configurations</h2>

<ul style="font-size: 1.1em">
<li><strong>RedisConfig</strong> – Redis connection and template setup</li>
<li><strong>SecurityConfig</strong> – JWT & OAuth2 security configuration</li>
<li><strong>SwaggerConfig</strong> – OpenAPI configuration</li>
<li><strong>SwaggerCorsConfig</strong> – CORS handling for Swagger</li>
<li><strong>SwaggerTagConfig</strong> – API grouping and tagging</li>
<li><strong>TransactionConfig</strong> – Transaction management</li>
<li><strong>WeatherConfig</strong> – External weather API configuration</li>
</ul>

<h2 style="font-size: 2em; color: #2c3e50; margin-top: 30px">17. Main Dependencies</h2>

<ul style="font-size: 1.1em">
<li>spring-boot-starter-mail</li>
<li>spring-boot-starter-security</li>
<li>spring-boot-starter-data-redis</li>
<li>spring-kafka</li>
<li>jjwt (JWT authentication)</li>
<li>springdoc-openapi</li>
<li>Lombok</li>
<li>Spring Boot Test</li>
</ul>

<h2 style="font-size: 2em; color: #2c3e50; margin-top: 30px">18. Deployment</h2>

<ul style="font-size: 1.1em">
<li>Platform: <strong>Render</strong></li>
<li>Application is deployed as a Spring Boot backend service</li>
</ul>

<p style="font-size: 1.2em; background: #e8f4fd; padding: 15px; border-radius: 5px">
<strong>Live URL:</strong><br>
<code>https://journalapp-p6ur.onrender.com</code>
</p>

<h2 style="font-size: 2em; color: #2c3e50; margin-top: 30px">19. How to Run Locally</h2>

<h3 style="font-size: 1.5em; color: #34495e">1. Clone the repository</h3>
<pre style="font-size: 1.1em; background: #2c3e50; color: #ecf0f1; padding: 15px; border-radius: 5px">
git clone <repository-url>
</pre>

<h3 style="font-size: 1.5em; color: #34495e">2. Configure environment variables</h3>
<ul style="font-size: 1.1em">
<li>MongoDB URI</li>
<li>Redis credentials</li>
<li>Kafka configuration</li>
<li>Mail credentials</li>
</ul>

<h3 style="font-size: 1.5em; color: #34495e">3. Build and run</h3>
<pre style="font-size: 1.1em; background: #2c3e50; color: #ecf0f1; padding: 15px; border-radius: 5px">
mvn clean install
mvn spring-boot:run
</pre>

<h2 style="font-size: 2em; color: #2c3e50; margin-top: 30px">20. Author</h2>

<p style="font-size: 1.2em; background: #f8f9fa; padding: 20px; border-radius: 5px; border-left: 5px solid #3498db">
<strong>Ansh Verma</strong><br>
Backend Developer – Spring Boot
</p>

<hr style="border: 1px solid #ddd; margin: 40px 0">


<h2 style="font-size: 1.8em; color: #2c3e50">🤝 Contributing</h2>
<p style="font-size: 1.1em">Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.</p>

<h2 style="font-size: 1.8em; color: #2c3e50">📧 Contact</h2>
<p style="font-size: 1.1em">For any queries or suggestions, feel free to reach out at : anshv8096@gmail.com </p>
