package com.property.maintenance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Property Maintenance Request System.
 * Developed for Software Engineering SAT II assessment.
 */
@SpringBootApplication
public class PropertyMaintenanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PropertyMaintenanceApplication.class, args);
        System.out.println("\n=======================================================");
        System.out.println(" Property Maintenance Request System Started Successfully! ");
        System.out.println(" Access Application at: http://localhost:8080/        ");
        System.out.println("=======================================================\n");
    }
}
