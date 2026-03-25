package io.github.rafaviv.panther.pantherbackend.iam.infrastructure.authorization.sfs.services;

import io.github.rafaviv.panther.pantherbackend.iam.domain.model.aggregates.User;
import io.github.rafaviv.panther.pantherbackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import io.github.rafaviv.panther.pantherbackend.iam.infrastructure.authorization.sfs.model.UserDetailsServiceExtension;
import io.github.rafaviv.panther.pantherbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * User Details Service Implementation
 * <p>
 * This service is responsible for loading user details from the database
 * for Spring Security authentication.
 * </p>
 */
@Service(value = "defaultUserDetailsService")
public class UserDetailsServiceImpl implements UserDetailsServiceExtension {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return UserDetailsImpl.build(user);
    }

    @Override
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with userId: " + userId));

        return UserDetailsImpl.build(user);
    }
}
