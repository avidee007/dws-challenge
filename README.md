### Spring initializer configurations were updated to accommodate current dates changes.

The following updates were made to initializer: -
* Spring boot version upgraded to 3.3.4
* Java 17

### Code Refactoring to existing code
* Removed `@Autowired` annotation as this redundant with construction injection.
* Utilized `@RequiredArgsConstructor` lombok annotation to make code less verbose, cleaner and readable.
  This creates a constructor with all final member variables of constructing bean.
* Utilized `@Sl4j` annotation to get a logger object to add logging.
* Utilized `Mockito` in testcases to mock `NotificationService` functionality.

### New code addition to implement solution
*  Utilized `record` type to create data transfer objects reducing boiler plate code.
* `TransferAmountCommand` command object holding details of payer and payee accountIds and 
   amount needs to be transferred with required constraints.
* `transferAmount` method in `AccountService` class performing transfer business logic
  by acquiring a consistent synchronized lock on accountId object by comparing them
  lexicographically avoiding any deadlock situation.
* Added `GlobalExceptionHandler` to handle exception occurred in API execution. This class uses `ErrorResponse`
  DTO to give meaningful error response to a client in case of any exception happened.

### Implementation changes required for a database as persistence in a distributed microservice environment
* Must have utilized `Pessimistic Write Lock` provided by JPA to acquire lock on the modifying database rows 
  along with JVM based lock using `synchronized` keyword or any implementation of `Lock`
  interface like `ReentrantLock`.
* Used `@Transactional` to ensure ACID property of the transaction.
* This approach would ensure no deadlock at JVM level as well as no inconsistency in data at database level if we 
  have multiple instances are trying to do transfer on the same account.

  * Below is an example of how JPA repository will look like if Pessimistic lock is used.
  
        `@Repository
         public interface AccountRepository extends JpaRepository<AccountEntity, String> {

            @Lock(LockModeType.PESSIMISTIC_WRITE)
            @Query("SELECT a FROM Account a WHERE a.id = :id")
            AccountEntity findAccountById(String id);
         }`

### What could have added if given more time?
1. Implementation with **Spring Data JPA** to have an actual database functionality with the above-mentioned recommendation 
   for distributed microservice environment. 
2. **Swagger** for API documentation. 
3. **DockerFile** for building docker image to containerize the service. 
4. **docker-compose** file to run this service with all dependencies for local development. 
5. **Authentication** and **Role-based Authorization** to avoid unauthorized access.
6. Refactored complete code base with **Domain Driven Design (DDD)** guidelines, separating command and query services 
   to a leverage **Command Query Response Segregation (CQRS)** design pattern.
7. Applied **Event Driven Architecture (EDA)** to send email notification asynchronously, by triggering `AccountTransferEvent`
   and process that from decoupled **notification microservice** consuming the transfer event.