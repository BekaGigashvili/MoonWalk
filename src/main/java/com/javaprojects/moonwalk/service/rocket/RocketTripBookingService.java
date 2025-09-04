package com.javaprojects.moonwalk.service.rocket;

import com.javaprojects.moonwalk.model.User;
import com.javaprojects.moonwalk.model.rocket.RocketTrip;
import com.javaprojects.moonwalk.model.rocket.RocketTripBooking;
import com.javaprojects.moonwalk.model.rocket.RocketTripBookingRequest;
import com.javaprojects.moonwalk.model.rocket.RocketTripReturn;
import com.javaprojects.moonwalk.repository.RocketTripBookingRepository;
import com.javaprojects.moonwalk.security.exceptions.UserNotFoundException;
import com.javaprojects.moonwalk.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RocketTripBookingService {
    private final RocketTripBookingRepository rocketTripBookingRepository;
    private final RocketTripService rocketTripService;
    private final RocketTripReturnService rocketTripReturnService;
    private final UserService userService;

    public RocketTripBooking save(RocketTripBooking rocketTripUser) {
        return rocketTripBookingRepository.save(rocketTripUser);
    }

    @Transactional
    public RocketTripBooking bookTrip(String userEmail, RocketTripBookingRequest request) {

        RocketTrip trip = rocketTripService.findByIdForUpdate(request.getTripId());
        int numPassengers = request.getNumPassengers();

        int seatsAvailable = trip.getRocket().getSeatsTotal() - trip.getSeatsTaken();
        if(numPassengers > seatsAvailable) {
            throw new RuntimeException("Not enough seats available");
        }

        RocketTripReturn returnTrip = rocketTripReturnService
                .findByIdForUpdate(request.getReturnTripId());

        int returnSeatsAvailable = returnTrip.getRocket().getSeatsTotal() - returnTrip.getSeatsTaken();
        if(numPassengers > returnSeatsAvailable) {
            throw new RuntimeException("Not enough seats available for return");
        }

        User user = userService
                .findByEmail(userEmail)
                .orElseThrow(UserNotFoundException::new);

        RocketTripBooking rocketTripBooking = new RocketTripBooking();
        rocketTripBooking.setUser(user);
        rocketTripBooking.setRocketTrip(trip);
        rocketTripBooking.setReturnTrip(returnTrip);
        StringBuilder seatNumbers = new StringBuilder();
        for(int i = trip.getSeatsTaken() + 1; i <= trip.getSeatsTaken() + numPassengers; i++) {
            seatNumbers.append("A").append(i).append(" ");
        }

        StringBuilder returnSeatNumbers = new StringBuilder();
        for(int i = returnTrip.getSeatsTaken() + 1; i <= returnTrip.getSeatsTaken() + numPassengers; i++) {
            returnSeatNumbers.append("A").append(i).append(", ");
        }

        rocketTripBooking.setSeatNumbers(seatNumbers.toString().trim());
        rocketTripBooking.setReturnSeatNumbers(returnSeatNumbers.toString().trim());

        trip.setSeatsTaken(trip.getSeatsTaken() + numPassengers);
        returnTrip.setSeatsTaken(returnTrip.getSeatsTaken() + numPassengers);

        rocketTripService.save(trip);
        rocketTripReturnService.save(returnTrip);
        rocketTripBookingRepository.save(rocketTripBooking);
        return rocketTripBooking;
    }

    public void delete(RocketTripBooking rocketTripBooking) {
        rocketTripBookingRepository.delete(rocketTripBooking);
    }

    public void deleteAll(){
        rocketTripBookingRepository.deleteAll();
    }
}
