package com.javaprojects.moonwalk.integrationTests;

import com.javaprojects.moonwalk.model.Role;
import com.javaprojects.moonwalk.model.User;
import com.javaprojects.moonwalk.model.WeekDay;
import com.javaprojects.moonwalk.model.rocket.Rocket;
import com.javaprojects.moonwalk.model.rocket.RocketTripCreateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaprojects.moonwalk.service.UserService;
import com.javaprojects.moonwalk.service.rocket.RocketService;
import com.javaprojects.moonwalk.service.rocket.RocketTripReturnService;
import com.javaprojects.moonwalk.service.rocket.RocketTripService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RocketTripControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private RocketService rocketService;
    @Autowired
    private RocketTripService tripService;
    @Autowired
    private RocketTripReturnService returnTripService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeAll
    void setUp() {

        returnTripService.deleteAll();
        tripService.deleteAll();
        rocketService.deleteAll();
        userService.deleteAll();

        User admin = new User();
        admin.setEmail("system_admin@example.com");
        admin.setFirstName("System_admin");
        admin.setLastName("Admin");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole(Role.SYSTEM_ADMIN);
        userService.save(admin);

        User passenger = new User();
        passenger.setEmail("passenger@example.com");
        passenger.setFirstName("Passenger");
        passenger.setLastName("Passenger");
        passenger.setPassword(passwordEncoder.encode("password"));
        passenger.setRole(Role.PASSENGER);
        userService.save(passenger);
    }

    @Test
    @WithMockUser(username = "passenger@example.com", roles = {"PASSENGER"})
    @Order(2)
    void testFindCorrespondingTrips() throws Exception {
        mockMvc.perform(get("/trip/launch/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/trip/return/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(username = "system_admin@example.com", roles = {"SYSTEM_ADMIN"})
    @Order(1)
    void testCreateAndFetchTrips() throws Exception {
        Rocket rocket = new Rocket();
        rocket.setLaunchDay(WeekDay.MONDAY);
        rocket.setLaunchTime("13");
        rocket.setSeatsTotal(10);
        rocket.setPrice(1500);
        Rocket savedRocket = rocketService.save(rocket);

        LocalDate today = LocalDate.now();

        LocalDate nextOrSameMonday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

        LocalDateTime mondayTime = LocalDateTime.of(nextOrSameMonday, LocalTime.of(Integer.parseInt(savedRocket.getLaunchTime()), 0));
        mondayTime = mondayTime.plusDays(7);

        RocketTripCreateRequest request = new RocketTripCreateRequest();
        request.setRocketId(savedRocket.getId());
        request.setLaunchExactTime(mondayTime);

        mockMvc.perform(post("/trip/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/trip/earth-moon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seatsTaken").value(0));

        mockMvc.perform(get("/trip/moon-earth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seatsTaken").value(0));
    }
}
