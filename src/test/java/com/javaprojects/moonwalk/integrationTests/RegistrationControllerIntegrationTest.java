package com.javaprojects.moonwalk.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaprojects.moonwalk.model.Role;
import com.javaprojects.moonwalk.model.User;
import com.javaprojects.moonwalk.security.auth.RegistrationRequest;
import com.javaprojects.moonwalk.security.auth.VerificationToken;
import com.javaprojects.moonwalk.service.HotelService;
import com.javaprojects.moonwalk.service.OrderService;
import com.javaprojects.moonwalk.service.UserService;
import com.javaprojects.moonwalk.service.auth.VerificationTokenService;
import com.javaprojects.moonwalk.service.rocket.RocketService;
import com.javaprojects.moonwalk.service.rocket.RocketTripBookingService;
import com.javaprojects.moonwalk.service.rocket.RocketTripReturnService;
import com.javaprojects.moonwalk.service.rocket.RocketTripService;
import com.javaprojects.moonwalk.service.room.RoomBookingService;
import com.javaprojects.moonwalk.service.room.RoomService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RegistrationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private RocketService rocketService;
    @Autowired
    private RocketTripService tripService;
    @Autowired
    private RocketTripReturnService tripReturnService;
    @Autowired
    private HotelService hotelService;
    @Autowired
    private RoomService roomService;
    @Autowired
    private RocketTripBookingService tripBookingService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private RoomBookingService roomBookingService;
    @Autowired
    private VerificationTokenService verificationTokenService;

    @BeforeAll
    void setUp() {
        orderService.deleteAll();
        tripBookingService.deleteAll();
        roomBookingService.deleteAll();
        userService.deleteAll();
        tripService.deleteAll();
        tripReturnService.deleteAll();
        rocketService.deleteAll();
        roomService.deleteAll();
        hotelService.deleteAll();
    }

    @Test
    @Order(1)
    void testRegister() throws Exception {
        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setEmail("passenger@example.com");
        registrationRequest.setPassword(passwordEncoder.encode("password"));
        registrationRequest.setFirstName("Passenger");
        registrationRequest.setLastName("Passenger");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated());
    }
    @Test
    @Order(2)
    void testVerifyAndEnable() throws Exception {
        User user = new User();
        user.setFirstName("Passenger2");
        user.setLastName("Passenger2");
        user.setEmail("passenger2@example.com");
        user.setRole(Role.PASSENGER);
        user.setPassword(passwordEncoder.encode("password"));
        user.setEnabled(false);
        userService.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken
                .builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();
        verificationTokenService.save(verificationToken);
        mockMvc.perform(get("/auth/verify")
                .param("token", verificationToken.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Email verified"));

    }

}
