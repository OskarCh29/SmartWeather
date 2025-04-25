package pl.smartweather.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.smartweather.app.exception.UserNotFoundException;
import pl.smartweather.app.model.entity.User;
import pl.smartweather.app.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User saveNewUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    public User getUserByEmail(String userEmail) {
        return userRepository.findByEmailAddress(userEmail).orElseThrow(
                () -> new UserNotFoundException("No user found with provided credentials"));
    }
}
