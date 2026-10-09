package com.donations.donations.config;

import com.donations.donations.repository.UserRepository;
import com.donations.donations.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1) // run before seeder
@RequiredArgsConstructor
public class DeleteAdminTempRunner implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        userRepository.findByEmail("mcumbane@speranza.co.mz").ifPresent(userRepository::delete);
    }
}
