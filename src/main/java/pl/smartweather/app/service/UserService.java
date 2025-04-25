package pl.smartweather.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.smartweather.app.entity.User;
import pl.smartweather.app.exception.UserAlreadyExistsException;
import pl.smartweather.app.exception.UserNotFoundException;
import pl.smartweather.app.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User saveNewUser(User user) {
        if (userRepository.findByEmailAddress(user.getEmailAddress()).isPresent()) {
            throw new UserAlreadyExistsException("Email already in use");
        } else {
            return userRepository.save(user);
        }

    }

    public void deleteUser(User user) {
        Optional<User> deletedUser = userRepository.findByEmailAddress(user.getEmailAddress());
        if (deletedUser.isPresent()) {
            userRepository.delete(deletedUser.get());
        } else {
            throw new UserNotFoundException("No user to be deleted");
        }
    }

    public User getUserByEmail(String userEmail) {
        return userRepository.findByEmailAddress(userEmail).orElseThrow(
                () -> new UserNotFoundException("No user found with provided credentials"));
    }
}
