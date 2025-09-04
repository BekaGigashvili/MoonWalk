package com.javaprojects.moonwalk.unitTests;

import com.javaprojects.moonwalk.model.User;
import com.javaprojects.moonwalk.repository.UserRepository;
import com.javaprojects.moonwalk.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceUnitTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    void save_callsRepositorySave() {
        User user = new User();
        user.setEmail("test@example.com");

        userService.save(user);

        verify(userRepository).save(user);
    }

    @Test
    void deleteAll_callsRepositoryDeleteAll() {
        userService.deleteAll();
        verify(userRepository).deleteAll();
    }

    @Test
    void findByEmail_existingUser_returnsUser() {
        User user = new User();
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void findByEmail_nonExistingUser_returnsEmpty() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByEmail("notfound@example.com");

        assertTrue(result.isEmpty());
        verify(userRepository).findByEmail("notfound@example.com");
    }

    @Test
    void loadUserByUsername_existingUser_returnsUserDetails() {
        User user = new User();
        user.setEmail("user@example.com");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        UserDetails result = userService.loadUserByUsername("user@example.com");

        assertEquals(user, result);
        verify(userRepository).findByEmail("user@example.com");
    }

    @Test
    void loadUserByUsername_nonExistingUser_throwsException() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("missing@example.com")
        );

        assertEquals("missing@example.com", exception.getMessage());
        verify(userRepository).findByEmail("missing@example.com");
    }
}
