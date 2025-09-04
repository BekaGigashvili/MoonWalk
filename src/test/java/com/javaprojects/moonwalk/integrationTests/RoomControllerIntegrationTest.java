package com.javaprojects.moonwalk.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaprojects.moonwalk.model.Role;
import com.javaprojects.moonwalk.model.RoomStatus;
import com.javaprojects.moonwalk.model.User;
import com.javaprojects.moonwalk.model.WeekDay;
import com.javaprojects.moonwalk.model.hotel.Hotel;
import com.javaprojects.moonwalk.model.rocket.Rocket;
import com.javaprojects.moonwalk.model.rocket.RocketTripCreateRequest;
import com.javaprojects.moonwalk.model.room.AddRoomRequest;
import com.javaprojects.moonwalk.model.room.SetStatusRequest;
import com.javaprojects.moonwalk.service.HotelService;
import com.javaprojects.moonwalk.service.OrderService;
import com.javaprojects.moonwalk.service.UserService;
import com.javaprojects.moonwalk.service.rocket.RocketService;
import com.javaprojects.moonwalk.service.rocket.RocketTripBookingService;
import com.javaprojects.moonwalk.service.rocket.RocketTripReturnService;
import com.javaprojects.moonwalk.service.rocket.RocketTripService;
import com.javaprojects.moonwalk.service.room.RoomBookingService;
import com.javaprojects.moonwalk.service.room.RoomService;
import org.hamcrest.Matchers;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoomControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private HotelService hotelService;
    @Autowired
    private RoomService roomService;
    @Autowired
    private RocketService rocketService;
    @Autowired
    private RocketTripService tripService;
    @Autowired
    private RocketTripReturnService tripReturnService;
    @Autowired
    private RocketTripBookingService tripBookingService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private RoomBookingService roomBookingService;
    @Autowired
    private RocketTripService rocketTripService;

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

        User hotelAdmin = new User();
        hotelAdmin.setEmail("hotel_admin@example.com");
        hotelAdmin.setFirstName("Hotel_admin");
        hotelAdmin.setLastName("Admin");
        hotelAdmin.setPassword(passwordEncoder.encode("password"));
        hotelAdmin.setRole(Role.HOTEL_ADMIN);
        userService.save(hotelAdmin);

        Hotel hotel = new Hotel();
        hotel.setRating(4.8);
        hotel.setName("Moon Hotel");
        hotel.setEmail(hotelAdmin.getEmail());
        hotelService.save(hotel);
    }

    @Test
    @WithMockUser(username = "hotel_admin@example.com", roles = {"HOTEL_ADMIN"})
    @Order(1)
    void testAddRoomAndFetchIt() throws Exception {
        int roomNumber = 101;
        for (int capacity = 1; capacity <= 5; capacity++) {
            for (int i = 0; i < 4; i++) {
                AddRoomRequest request = new AddRoomRequest(roomNumber, capacity, 750.0 * capacity);

                mockMvc.perform(post("/room/room")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());

                roomNumber++;
            }
        }

        mockMvc.perform(get("/room"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(20));
    }

    @Test
    @WithMockUser(username = "hotel_admin@example.com", roles = {"HOTEL_ADMIN"})
    @Order(2)
    void testSetStatus() throws Exception {
        int count = 1;
        int max = 5;
        for (int i = 0; i < max; i++) {
            SetStatusRequest request1 = new SetStatusRequest();
            request1.setRoomNumber(100 + count);
            request1.setStatus(RoomStatus.RESERVED);

            mockMvc.perform(post("/room/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request1)))
                    .andExpect(status().isOk());

            SetStatusRequest request2 = new SetStatusRequest();
            request2.setRoomNumber(100 + count + max);
            request2.setStatus(RoomStatus.OCCUPIED);

            mockMvc.perform(post("/room/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request2)))
                    .andExpect(status().isOk());

            count++;
        }
    }

    @Test
    @WithMockUser(username = "hotel_admin@example.com", roles = {"HOTEL_ADMIN"})
    @Order(3)
    void testFindAvailableRooms() throws Exception {
        mockMvc.perform(get("/room/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].status").value(Matchers.everyItem(Matchers.is("AVAILABLE"))));

        mockMvc.perform(get("/room/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10));
    }

    @Test
    @WithMockUser(username = "hotel_admin@example.com", roles = {"HOTEL_ADMIN"})
    @Order(4)
    void testFindReservedRooms() throws Exception {
        mockMvc.perform(get("/room/reserved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].status").value(Matchers.everyItem(Matchers.is("RESERVED"))));

        mockMvc.perform(get("/room/reserved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    @WithMockUser(username = "hotel_admin@example.com", roles = {"HOTEL_ADMIN"})
    @Order(5)
    void testFindOccupiedRooms() throws Exception {
        mockMvc.perform(get("/room/occupied"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].status").value(Matchers.everyItem(Matchers.is("OCCUPIED"))));

        mockMvc.perform(get("/room/occupied"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    @WithMockUser(username = "passenger@example.com", roles = {"PASSENGER"})
    @Order(6)
    void testGetCapacities() throws Exception {
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

        rocketTripService.create(request);

        Long hotelId = hotelService.findByEmail("hotel_admin@example.com").getId();

        mockMvc.perform(get("/room/capacities/"
                        + hotelId + "/" + tripService.findAll().get(0).getId()
                        + "/" + tripReturnService.findAll().get(0).getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(5));
    }
}
