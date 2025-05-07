package pl.smartweather.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.smartweather.app.entity.User;
import pl.smartweather.app.exception.UserAlreadyExistsException;
import pl.smartweather.app.exception.UserNotFoundException;
import pl.smartweather.app.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserServiceTest {

    @Container
    static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:4.4.18")
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry){
        MONGO_DB_CONTAINER.start();
        registry.add("spring.data.mongodb.host",MONGO_DB_CONTAINER::getHost);
        registry.add("spring.data.mongodb.port",MONGO_DB_CONTAINER::getFirstMappedPort);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    void clearDataBase(){
        userRepository.deleteAll();
    }

    @Test
    void findAllUsers(){
        initTestUser();

        List<User> userList = userService.getAllUsers();

        assertEquals(1,userList.size(),"Only one user has been created");
        assertNotNull(userList.getFirst());
        assertEquals("userMail@example.com",userList.getFirst().getEmailAddress());
    }

    @Test
    void saveUserShouldBeSucceeded(){
        User testUser = new User();
        testUser.setEmailAddress("FirstUser@example.com");

        User savedUser = userService.saveNewUser(testUser);

        assertNotNull(savedUser);
        assertNotNull(savedUser.getId());
        assertEquals(testUser.getEmailAddress(), savedUser.getEmailAddress());
    }

    @Test
    void saveUserShouldThrowDuplicateKeyException(){
        initTestUser();
        User user = new User();
        user.setEmailAddress("userMail@example.com");

        assertThrows(UserAlreadyExistsException.class, () -> userService.saveNewUser(user));
    }

    @Test
    void deleteUserShouldBeSucceeded(){
        User user = new User();
        user.setEmailAddress("testMail@example.com");
        userRepository.save(user);

        userService.deleteUser(user);

        Optional<User> deletedUser = userRepository.findByEmailAddress("testMail@example.com");

        assertTrue(deletedUser.isEmpty(), "User should not be found");
    }
    @Test
    void deleteUserShouldThrowUserNotFoundException(){
        User user = new User();

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(user));
    }

    @Test
    void getUserByEmailShouldReturnTestUser(){
        initTestUser();

        User foundUser = userService.getUserByEmail("userMail@example.com");

        assertNotNull(foundUser,"User should be found");
        assertEquals("userMail@example.com",foundUser.getEmailAddress());
        assertNotNull(foundUser.getId());
    }

    @Test
    void getUserByEmailShouldThrowUserNotFoundException(){
        String nonExisting = "notExisting@example.com";

        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail(nonExisting));
    }

    private void initTestUser(){
        User testUser = new User();
        testUser.setEmailAddress("userMail@example.com");
        userRepository.save(testUser);
    }
}
