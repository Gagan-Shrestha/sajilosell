package sajilosell.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import sajilosell.UserRegistration.UserRegistration;
import sajilosell.user.User;

import java.util.Optional;

public interface UserService extends UserDetailsService {
    User save(UserRegistration userRegistration);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id); // Add this method
}
