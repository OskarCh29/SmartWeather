package pl.smartweather.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.smartweather.app.entity.User;
import pl.smartweather.app.model.request.UserRequest;
import pl.smartweather.app.model.response.GenericServerResponse;
import pl.smartweather.app.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/user")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PostMapping("/user")
    public ResponseEntity<User> saveUser(@RequestBody(required = true) @Valid UserRequest userRequest) {
        User user = User.builder().emailAddress(userRequest.getEmailAddress()).build();
        userService.saveNewUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @DeleteMapping("/user")
    public ResponseEntity<GenericServerResponse> deleteUser(
            @RequestBody(required = true) @Valid UserRequest userRequest) {
        User user = User.builder().emailAddress(userRequest.getEmailAddress()).build();
        userService.deleteUser(user);
        return ResponseEntity.ok(new GenericServerResponse("User: " + user.getEmailAddress() + " has been deleted"));
    }
}
