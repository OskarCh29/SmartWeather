package pl.smartweather.app.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import pl.smartweather.app.entity.User;
import pl.smartweather.app.exception.UserAlreadyExistsException;
import pl.smartweather.app.exception.UserNotFoundException;
import pl.smartweather.app.service.UserService;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void getAllUsersShouldReturn200() throws Exception {
        User testUser = initTestUser();
        List<User> userList = List.of(testUser);
        when(userService.getAllUsers()).thenReturn(userList);

        mockMvc.perform(MockMvcRequestBuilders.get("/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].emailAddress").value("testMail@example.com"))
                .andExpect(jsonPath("$", hasSize(1)));

        verify(userService).getAllUsers();
    }

    @Test
    void getUserByEmailShouldReturn200() throws Exception {
        User testUser = initTestUser();

        when(userService.getUserByEmail(testUser.getEmailAddress())).thenReturn(testUser);

        mockMvc.perform(MockMvcRequestBuilders.get("/user/{email}", "testMail@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailAddress").value("testMail@example.com"));

        verify(userService).getUserByEmail(testUser.getEmailAddress());
    }

    @Test
    void getUserByEmailShouldReturn404UserNotFound() throws Exception {
        when(userService.getUserByEmail(anyString())).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/user/{email}", "testMail@example.com"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("User not found")));

        verify(userService).getUserByEmail(anyString());
    }

    @Test
    void saveUserShouldReturn200() throws Exception {
        User testUser = initTestUser();

        when(userService.saveNewUser(any())).thenReturn(testUser);

        mockMvc.perform(MockMvcRequestBuilders.post("/user")
                        .content(new ObjectMapper().writeValueAsString(testUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.emailAddress").value(testUser.getEmailAddress()));

        verify(userService).saveNewUser(any());
    }

    @Test
    void saveUserShouldReturn409UserAlreadyExists() throws Exception {
        User testUser = initTestUser();
        when(userService.saveNewUser(any())).thenThrow(new UserAlreadyExistsException("User exists"));

        mockMvc.perform(MockMvcRequestBuilders.post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(testUser)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("User exists")));

        verify(userService).saveNewUser(any());
    }

    @Test
    void saveUserShouldReturn400CausedByValidation() throws Exception {
        User testUser = new User();
        testUser.setEmailAddress("invalid.com");

        mockMvc.perform(MockMvcRequestBuilders.post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid email address")));
    }

    @Test
    void saveUserShouldReturn400CausedByValidationEmptyField() throws Exception {
        User testUser = new User();
        testUser.setEmailAddress("");

        mockMvc.perform(MockMvcRequestBuilders.post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid email address")));
    }
    @Test
    void saveUserShouldReturn400CausedByValidationEmailMissing() throws Exception {
        User testUser = new User();

        mockMvc.perform(MockMvcRequestBuilders.post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid email address")));
    }

    @Test
    void deleteUserShouldReturn200() throws Exception {
        User testUser = initTestUser();

        doNothing().when(userService).deleteUser(testUser);

        mockMvc.perform(MockMvcRequestBuilders.delete("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(testUser)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(testUser.getEmailAddress() + " has been deleted")));
    }

    @Test
    void deleteUserShouldReturn400CausedByValidation() throws Exception {
        User testUser = new User();
        testUser.setEmailAddress("invalid.com");

        mockMvc.perform(MockMvcRequestBuilders.delete("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid email address")));
    }

    @Test
    void deleteUserShouldReturn404UserNotFound() throws Exception {
        User testUser = initTestUser();

        doThrow(new UserNotFoundException("User not found")).when(userService).deleteUser(any());

        mockMvc.perform(MockMvcRequestBuilders.delete("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(testUser)));

        verify(userService).deleteUser(any());
    }

    private User initTestUser() {
        User user = new User();
        user.setEmailAddress("testMail@example.com");
        return user;
    }

}
