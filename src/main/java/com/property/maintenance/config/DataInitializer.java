package com.property.maintenance.config;

import com.property.maintenance.model.Resource;
import com.property.maintenance.model.User;
import com.property.maintenance.repository.ResourceRepository;
import com.property.maintenance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Automatically seeds the database on startup if tables are empty.
 * Ensures the mandatory demonstration actors and services always exist.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;

    @Autowired
    public DataInitializer(UserRepository userRepository, ResourceRepository resourceRepository) {
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
    }

    @Override
    public void run(String... args) {
        // 1. Seed Users if not already present
        if (userRepository.count() == 0) {
            userRepository.save(new User(null, "USER1", "0771234567", "user1@property.lk", "USER", "ACTIVE"));
            userRepository.save(new User(null, "USER2", "0777654321", "user2@property.lk", "USER", "ACTIVE"));
            userRepository.save(new User(null, "ADMIN1", "0770000000", "admin@property.lk", "ADMINISTRATOR", "ACTIVE"));
            System.out.println(">> Seeded 3 default users (USER1, USER2, ADMIN1).");
        }

        // 2. Seed Resources if not already present
        if (resourceRepository.count() == 0) {
            // S101: Capacity 10, Active
            resourceRepository.save(new Resource(null, "S101", "Electrical Maintenance", 10, 10, "ACTIVE"));
            // S102: Capacity 5, Active
            resourceRepository.save(new Resource(null, "S102", "Plumbing & Drainage", 5, 5, "ACTIVE"));
            // S103: Capacity 0, Inactive
            resourceRepository.save(new Resource(null, "S103", "HVAC & Air Conditioning", 0, 0, "INACTIVE"));
            System.out.println(">> Seeded 3 default maintenance services (S101, S102, S103).");
        }
    }
}
