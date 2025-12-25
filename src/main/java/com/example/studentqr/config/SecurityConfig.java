package com.example.studentqr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Public pages
                        .requestMatchers("/", "/login", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers("/error", "/about").permitAll()

                        // Student pages
                        .requestMatchers("/student/list", "/student/view/**").hasAnyRole("USER", "TEACHER", "ADMIN")
                        .requestMatchers("/student/download/**", "/student/regenerate/**").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers("/student/form", "/student/generate-qr", "/student/delete/**").hasRole("ADMIN")

                        // Attendance pages
                        .requestMatchers("/attendance/**").hasAnyRole("TEACHER", "ADMIN")

                        // Holiday pages
                        .requestMatchers("/holidays/add", "/holidays/edit/**", "/holidays/delete/**",
                                "/holidays/save", "/holidays/toggle/**", "/holidays/initialize").hasRole("ADMIN")
                        .requestMatchers("/holidays").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers("/holidays/calendar", "/holidays/check-date").hasAnyRole("USER", "TEACHER", "ADMIN")

                        // All other pages require authentication
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/access-denied")
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN", "TEACHER", "USER")
                .build();

        UserDetails teacher = User.builder()
                .username("teacher")
                .password(passwordEncoder().encode("teacher123"))
                .roles("TEACHER", "USER")
                .build();

        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("user123"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, teacher, user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}