package com.thakur.scheduler.task.security;

import com.thakur.scheduler.task.model.entity.User;
import com.thakur.scheduler.task.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(@NonNull String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail,usernameOrEmail)
                                .orElseThrow(() -> new UsernameNotFoundException(usernameOrEmail));

        return new CustomUserDetails(user);
    }

    public UserDetails loadUserById(@NonNull String id) throws UsernameNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(id));
        return new CustomUserDetails(user);
    }
}
