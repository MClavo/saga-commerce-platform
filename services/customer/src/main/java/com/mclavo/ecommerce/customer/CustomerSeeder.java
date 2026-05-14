package com.mclavo.ecommerce.customer;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

// This class should use a profile. But for simplicity, we will run it every time the application starts. In a real application, you would want to use a profile to only run this seeder in development or testing environments.
@Component
@RequiredArgsConstructor
public class CustomerSeeder implements ApplicationRunner {

    private final CustomerRepository customerRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (customerRepository.count() > 0) {
            return;
        }

        var customers = List.of(
                Customer.builder()
                        .firstname("John")
                        .lastname("Doe")
                        .email("john.doe@email.com")
                        .address(Address.builder()
                                .street("Main Street")
                                .houseNumber("12A")
                                .zipCode("28001")
                                .build())
                        .build(),

                Customer.builder()
                        .firstname("Jane")
                        .lastname("Smith")
                        .email("jane.smith@email.com")
                        .address(Address.builder()
                                .street("Oak Avenue")
                                .houseNumber("45")
                                .zipCode("29010")
                                .build())
                        .build(),

                Customer.builder()
                        .firstname("Alice")
                        .lastname("Johnson")
                        .email("alice.johnson@email.com")
                        .address(Address.builder()
                                .street("Sunset Boulevard")
                                .houseNumber("8")
                                .zipCode("08015")
                                .build())
                        .build(),

                Customer.builder()
                        .firstname("Bob")
                        .lastname("The Builder")
                        .email("bob.the.builder@email.com")
                        .address(Address.builder()
                                .street("Builder Street")
                                .houseNumber("100")
                                .zipCode("10001")
                                .build())
                        .build(),

                Customer.builder()
                        .firstname("Lucas")
                        .lastname("Clark")
                        .email("lucas.clark@email.com")
                        .address(Address.builder()
                                .street("Clark Street")
                                .houseNumber("200")
                                .zipCode("20002")
                                .build())
                        .build()
        );

        customerRepository.saveAll(customers);
    }
}