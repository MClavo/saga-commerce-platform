package com.mclavo.ecommerce.payment.domain;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


@Validated
public record Customer(
    String id,
    
    @NotBlank(message = "First name is required")
    String firstname,
    
    @NotBlank(message = "Last name is required")
    String lastname,

    @Email(message = "Email is required")
    String email

) {}
