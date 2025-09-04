package com.javaprojects.moonwalk.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaprojects.moonwalk.model.Role;
import com.javaprojects.moonwalk.model.User;
import com.javaprojects.moonwalk.security.auth.AuthenticationRequest;
import com.javaprojects.moonwalk.service.HotelService;
import com.javaprojects.moonwalk.service.OrderService;
import com.javaprojects.moonwalk.service.UserService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthenticationControllerIntegrationTest {

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

        User passenger = new User();
        passenger.setEmail("passenger@example.com");
        passenger.setFirstName("Passenger");
        passenger.setLastName("Passenger");
        passenger.setPassword(passwordEncoder.encode("password"));
        passenger.setRole(Role.PASSENGER);
        userService.save(passenger);
    }

    @Test
    @Order(1)
    void testAuthenticate() throws Exception {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setEmail("passenger@example.com");
        authenticationRequest.setPassword("password");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authenticationRequest)))
                .andExpect(status().isOk());
    }
}
