package com.javaprojects.moonwalk.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaprojects.moonwalk.model.Role;
import com.javaprojects.moonwalk.model.RoomStatus;
import com.javaprojects.moonwalk.model.User;
import com.javaprojects.moonwalk.model.WeekDay;
import com.javaprojects.moonwalk.model.hotel.Hotel;
import com.javaprojects.moonwalk.model.order.CheckoutRequest;
import com.javaprojects.moonwalk.model.order.PaymentMethodType;
import com.javaprojects.moonwalk.model.order.StripeRequest;
import com.javaprojects.moonwalk.model.rocket.Rocket;
import com.javaprojects.moonwalk.model.rocket.RocketTripBookingRequest;
import com.javaprojects.moonwalk.model.rocket.RocketTripCreateRequest;
import com.javaprojects.moonwalk.model.room.Room;
import com.javaprojects.moonwalk.model.room.RoomBookingRequest;
import com.javaprojects.moonwalk.model.room.RoomRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CheckoutControllerIntegrationTest {

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

        Rocket rocket = new Rocket();
        rocket.setPrice(1500);
        rocket.setSeatsTotal(10);
        rocket.setLaunchDay(WeekDay.MONDAY);
        rocket.setLaunchTime("12");
        rocket = rocketService.save(rocket);

        RocketTripCreateRequest tripCreateRequest = new RocketTripCreateRequest();

        tripCreateRequest.setRocketId(rocket.getId());

        LocalDate today = LocalDate.now();

        LocalDate nextOrSameMonday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

        LocalDateTime mondayTime = LocalDateTime.of(nextOrSameMonday, LocalTime.of(Integer.parseInt(rocket.getLaunchTime()), 0));
        mondayTime = mondayTime.plusDays(7);

        tripCreateRequest.setLaunchExactTime(mondayTime);

        tripService.create(tripCreateRequest);

        User hotelAdmin = new User();
        hotelAdmin.setEmail("hotel_admin@example.com");
        hotelAdmin.setFirstName("Hotel_admin");
        hotelAdmin.setLastName("Admin");
        hotelAdmin.setPassword(passwordEncoder.encode("password"));
        hotelAdmin.setRole(Role.HOTEL_ADMIN);
        userService.save(hotelAdmin);

        Hotel hotel = new Hotel();
        hotel.setName("Moon Hotel");
        hotel.setEmail(hotelAdmin.getEmail());
        hotel.setRating(4.8);
        hotel.setDescription("Great Hotel For Moon Travellers");
        hotelService.save(hotel);

        List<Room> rooms = new ArrayList<>();
        int roomNumber = 101;
        for (int capacity = 1; capacity <= 5; capacity++) {
            for (int i = 0; i < 4; i++) {
                Room room = Room
                        .builder()
                        .roomNumber(roomNumber)
                        .capacity(capacity)
                        .price(capacity * 750.0)
                        .status(RoomStatus.AVAILABLE)
                        .hotel(hotel)
                        .build();

                rooms.add(room);
                roomNumber++;
            }
        }
        roomService.saveAll(rooms);
    }

    @Test
    @WithMockUser(username = "passenger@example.com", roles = {"PASSENGER"})
    @Order(1)
    void testCheckOut() throws Exception {
        CheckoutRequest checkoutRequest = new CheckoutRequest();

        RocketTripBookingRequest tripBookingRequest = new RocketTripBookingRequest();
        Long tripId = tripService.findAll().get(0).getId();
        Long returnTripId = tripReturnService.findAll().get(0).getId();
        tripBookingRequest.setTripId(tripId);
        tripBookingRequest.setReturnTripId(returnTripId);
        tripBookingRequest.setNumPassengers(5);

        RoomBookingRequest roomBookingRequest = new RoomBookingRequest();

        List<RoomRequest> roomRequests = new ArrayList<>();

        RoomRequest roomRequest = new RoomRequest();
        roomRequest.setCapacity(3);
        roomRequest.setQuantity(1);
        roomRequests.add(roomRequest);
        RoomRequest roomRequest2 = new RoomRequest();
        roomRequest2.setCapacity(2);
        roomRequest2.setQuantity(1);
        roomRequests.add(roomRequest2);

        roomBookingRequest.setRooms(roomRequests);
        roomBookingRequest.setHotelId(hotelService.findByEmail("hotel_admin@example.com").getId());
        roomBookingRequest.setTripId(tripId);
        roomBookingRequest.setReturnTripId(returnTripId);

        StripeRequest stripeRequest = new StripeRequest();
        stripeRequest.setPaymentMethodType(PaymentMethodType.CARD);

        checkoutRequest.setRoomBookingRequest(roomBookingRequest);
        checkoutRequest.setStripeRequest(stripeRequest);
        checkoutRequest.setTripBookingRequest(tripBookingRequest);

        mockMvc.perform(post("/order/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isOk());

    }
}
