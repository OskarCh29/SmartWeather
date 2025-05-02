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
import pl.smartweather.app.entity.AppConfig;
import pl.smartweather.app.exception.ConfigurationException;
import pl.smartweather.app.model.request.ConfigUpdateRequest;
import pl.smartweather.app.model.request.LocationRequest;
import pl.smartweather.app.model.request.RootPasswordRequest;
import pl.smartweather.app.service.ConfigService;
import pl.smartweather.app.service.EmailService;
import pl.smartweather.app.service.TokenService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ConfigurationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ConfigurationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private ConfigService configService;

    @MockitoBean
    private TokenService tokenService;


    @Test
    void checkConfigStatusShouldReturn200() throws Exception {
        AppConfig appConfig = new AppConfig();
        appConfig.setInitialized(true);
        appConfig.setLocation("Test location");

        doNothing().when(configService).checkConfigStatus();
        when(configService.getAppConfig()).thenReturn(appConfig);

        mockMvc.perform(MockMvcRequestBuilders.get("/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value(appConfig.getLocation()))
                .andExpect(jsonPath("$.initialized").value(true));

        verify(configService).checkConfigStatus();
    }

    @Test
    void checkConfigStatusShouldReturn400NotConfigured() throws Exception {
        doThrow(new ConfigurationException("First configuration")).when(configService).checkConfigStatus();

        mockMvc.perform(MockMvcRequestBuilders.get("/config"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("First configuration")));

        verify(configService).checkConfigStatus();
    }

    @Test
    void checkConfigStatusShouldReturn400RootInitializedWithoutConfig() throws Exception {
        doThrow(new ConfigurationException("Root did not provide config")).when(configService).checkConfigStatus();

        mockMvc.perform(MockMvcRequestBuilders.get("/config"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Root did not provide config")));

        verify(configService).checkConfigStatus();
    }

    @Test
    void validateRootShouldReturn200withGeneratedToken() throws Exception {
        RootPasswordRequest request = new RootPasswordRequest("TestPassword123");

        when(configService.validateRootPassword(request.getRootPassword())).thenReturn("Test token123");

        mockMvc.perform(MockMvcRequestBuilders.post("/config/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userToken").value("Test token123"));

        verify(configService).validateRootPassword(any());
    }

    @Test
    void validateRootShouldReturn400causedByValidation() throws Exception {
        RootPasswordRequest request = new RootPasswordRequest("12");

        mockMvc.perform(MockMvcRequestBuilders.post("/config/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(content().string(containsString("Password not valid - Check Requirements")));
    }

    @Test
    void validateRootShouldReturn401causedByAuthorization() throws Exception {
        RootPasswordRequest request = new RootPasswordRequest("Root123");

        when(configService.validateRootPassword(any())).thenThrow(new SecurityException("Unauthorized"));

        mockMvc.perform(MockMvcRequestBuilders.post("/config/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(content().string(containsString("Unauthorized")));
        verify(configService).validateRootPassword(any());
    }

    @Test
    void updateRootPasswordShouldReturn200() throws Exception {
        RootPasswordRequest request = new RootPasswordRequest("Root123");

        doNothing().when(configService).setRootPassword(request.getRootPassword());

        mockMvc.perform(MockMvcRequestBuilders.post("/config/root")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Root password updated"));

        verify(configService).setRootPassword(any());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/testCase/passwordValidation_cases.csv")
    void updateRootPasswordShouldReturn400causedByValidation(String password) throws Exception {
        RootPasswordRequest request = new RootPasswordRequest(password);


        mockMvc.perform(MockMvcRequestBuilders.post("/config/root")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Password not valid - Check Requirements")));
    }
    @Test
    void updateRootPasswordShouldReturn400causedByValidation() throws Exception {
        RootPasswordRequest request = new RootPasswordRequest();


        mockMvc.perform(MockMvcRequestBuilders.post("/config/root")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Password not valid - Check Requirements")));
    }

    @Test
    void updateRootPasswordShouldReturn400rootAlreadyConfigured() throws Exception {
        RootPasswordRequest request = new RootPasswordRequest("Root123");

        doThrow(new ConfigurationException("Root password was initialized"))
                .when(configService).setRootPassword(request.getRootPassword());

        mockMvc.perform(MockMvcRequestBuilders.post("/config/root")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Root password was initialized")));

        verify(configService).setRootPassword(any());
    }

    @Test
    void updateConfigLocationShouldReturn200withProperMessage() throws Exception {
        LocationRequest request = new LocationRequest("Warsaw");

        doNothing().when(configService).setLocationConfiguration(any());

        mockMvc.perform(MockMvcRequestBuilders.post("/config/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Location updated to: " + request.getLocation()));

        verify(configService).setLocationConfiguration(any());
    }

    @Test
    void updateConfigLocationShouldReturn400causedByValidation() throws Exception {
        LocationRequest request = new LocationRequest("123 Non existing");

        mockMvc.perform(MockMvcRequestBuilders.post("/config/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Location cannot contain numbers")));

    }

    @Test
    void updateConfigurationShouldReturn200withProperMessage() throws Exception {
        ConfigUpdateRequest request = getTestRequest();

        doNothing().when(emailService).validateEmailConfiguration(any());
        doNothing().when(configService).validateApiKey(any());
        doNothing().when(tokenService).validateToken(any());
        doNothing().when(configService).setupAppConfiguration(any());

        mockMvc.perform(MockMvcRequestBuilders.post("/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Configuration provided"));

        verify(emailService).validateEmailConfiguration(any());
        verify(configService).validateApiKey(any());
        verify(tokenService).validateToken(any());
        verify(configService).setupAppConfiguration(any());
    }

    @Test
    void updateConfigurationShouldReturn400causedByValidationTokenMissing() throws Exception {
        ConfigUpdateRequest request = getTestRequest();
        request.setToken("");
        mockMvc.perform(MockMvcRequestBuilders.post("/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Authorization token missing")));
    }
    @Test
    void updateConfigurationShouldReturn400causedByValidationConfigMissing() throws Exception {
        ConfigUpdateRequest request = new ConfigUpdateRequest();
        request.setToken("validToken");

        mockMvc.perform(MockMvcRequestBuilders.post("/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Configuration missing")));
    }
    @ParameterizedTest
    @CsvFileSource(resources = "/testCase/configValidation_cases.csv")
    void updateConfigurationShouldReturn400causedByValidationConfigInvalid(
            String host, String port, String mailName, String mailPass, String apiKey, String mess) throws Exception {
        ConfigUpdateRequest request = new ConfigUpdateRequest();
        request.setToken("validToken");
        Map<String,String> newConfig = new HashMap<>();
        newConfig.put("mail_host",host);
        newConfig.put("mail_port", port);
        newConfig.put("mail_name",mailName);
        newConfig.put("mail_pass",mailPass);
        newConfig.put("api_key",apiKey);
        request.setNewConfig(newConfig);

        mockMvc.perform(MockMvcRequestBuilders.post("/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(mess)));
    }

    @Test
    void updateConfigurationShouldReturn401EmailAccessUnauthorized() throws Exception{
        ConfigUpdateRequest request = getTestRequest();

        doThrow(new SecurityException("Unauthorized access to email"))
                .when(emailService).validateEmailConfiguration(request.getNewConfig());

        mockMvc.perform(MockMvcRequestBuilders.post("/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Unauthorized access to email")));

        verify(emailService).validateEmailConfiguration(any());
    }

    @Test
    void updateConfigurationShouldReturn401ApiKeyUnauthorized() throws Exception{
        ConfigUpdateRequest request = getTestRequest();

        doThrow(new SecurityException("Unauthorized access to API - invalid key"))
                .when(configService).validateApiKey(any());

        mockMvc.perform(MockMvcRequestBuilders.post("/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Unauthorized access to API - invalid key")));

        verify(configService).validateApiKey(any());
    }

    @Test
    void updateConfigurationShouldReturn401TokenUnauthorized() throws Exception{
        ConfigUpdateRequest request = getTestRequest();

        doThrow(new SecurityException("Token for request is invalid"))
                .when(tokenService).validateToken(any());

        mockMvc.perform(MockMvcRequestBuilders.post("/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Token for request is invalid")));

        verify(tokenService).validateToken(any());
    }

    private ConfigUpdateRequest getTestRequest(){
        ConfigUpdateRequest request = new ConfigUpdateRequest();
        request.setToken("Valid token");
        request.setToken("validToken");
        Map<String,String> newConfig = new HashMap<>();
        newConfig.put("mail_host","host");
        newConfig.put("mail_port", "587");
        newConfig.put("mail_name","mailName");
        newConfig.put("mail_pass","mailPass");
        newConfig.put("api_key","apiKey");
        request.setNewConfig(newConfig);
        return request;
    }

}
