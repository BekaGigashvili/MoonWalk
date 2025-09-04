package com.javaprojects.moonwalk.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaprojects.moonwalk.model.Role;
import com.javaprojects.moonwalk.model.User;
import com.javaprojects.moonwalk.model.WeekDay;
import com.javaprojects.moonwalk.model.rocket.Rocket;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RocketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private RocketService rocketService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
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

        User admin = new User();
        admin.setEmail("system_admin@example.com");
        admin.setFirstName("System_admin");
        admin.setLastName("Admin");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole(Role.SYSTEM_ADMIN);
        userService.save(admin);
    }

    @Test
    @WithMockUser(username = "system_admin@example.com", roles = {"SYSTEM_ADMIN"})
    @Order(1)
    void testAddRocket() throws Exception {
        for (int i = 1; i <= 7; i++) {
            Rocket rocket = new Rocket();
            rocket.setLaunchDay(getWeekDay(i));
            rocket.setLaunchTime("12");
            rocket.setSeatsTotal(10);
            rocket.setPrice(1500);
            mockMvc.perform(post("/rocket")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(rocket)))
                    .andExpect(status().isOk());
        }
    }

    private static WeekDay getWeekDay(int i) {
        return switch (i) {
            case 1 -> WeekDay.MONDAY;
            case 2 -> WeekDay.TUESDAY;
            case 3 -> WeekDay.WEDNESDAY;
            case 4 -> WeekDay.THURSDAY;
            case 5 -> WeekDay.FRIDAY;
            case 6 -> WeekDay.SATURDAY;
            default -> WeekDay.SUNDAY;
        };
    }

    @Test
    @WithMockUser(username = "system_admin@example.com", roles = {"SYSTEM_ADMIN"})
    @Order(2)
    void testFindAll() throws Exception {
        mockMvc.perform(get("/rocket"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7));
    }
}
