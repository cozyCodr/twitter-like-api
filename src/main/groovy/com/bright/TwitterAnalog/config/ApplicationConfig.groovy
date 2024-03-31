package com.bright.TwitterAnalog.config

import com.bright.TwitterAnalog.model.User
import com.bright.TwitterAnalog.repository.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class ApplicationConfig {
    private final UserRepository userRepository

    ApplicationConfig(UserRepository userRepository) {
        this.userRepository = userRepository
    }

    @Bean
    UserDetailsService userDetailsService() {
        UserRepository userRepositoryRef = userRepository // Capturing the reference outside closure
        return (username) -> userRepositoryRef.findByUsername(username)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        true,
                        true,
                        true,
                        true,
                        Collections.emptyList()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username))
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        println "In auth provider"
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider()
        authProvider.setUserDetailsService(userDetailsService())
        authProvider.setPasswordEncoder(passwordEncoder())
        return authProvider
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        println "In auth manager"
        return config.getAuthenticationManager()
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder()
    }

}