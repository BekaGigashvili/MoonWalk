package com.javaprojects.moonwalk.integrationTests;

import com.javaprojects.moonwalk.model.Role;
import com.javaprojects.moonwalk.model.User;
import com.javaprojects.moonwalk.model.hotel.Hotel;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HotelControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private HotelService hotelService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private RocketService rocketService;
    @Autowired
    private RocketTripService tripService;
    @Autowired
    private RocketTripReturnService tripReturnService;
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
        admin.setEmail("passenger@example.com");
        admin.setFirstName("Passenger");
        admin.setLastName("Passenger");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole(Role.PASSENGER);
        userService.save(admin);

        for (int i = 1; i <= 10; i++) {
            Hotel hotel = Hotel
                    .builder()
                    .name("Moon Hotel " + i)
                    .email(admin.getEmail())
                    .description("Great hotel for moon travellers")
                    .rating(4.8)
                    .build();
            hotelService.save(hotel);
        }
    }

    @Test
    @WithMockUser(username = "passenger@example.com", roles = {"PASSENGER"})
    @Order(1)
    void testGetAllHotels() throws Exception {
        mockMvc.perform(get("/hotels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(10));

    }
}
