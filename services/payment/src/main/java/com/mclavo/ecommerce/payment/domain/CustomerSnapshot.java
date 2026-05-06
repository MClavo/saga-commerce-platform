package com.mclavo.ecommerce.payment.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerSnapshot(
        @NotBlank(message = "Customer ID is required")
        String customerId,

        @NotBlank(message = "First name is required")
        String firstname,

        @NotBlank(message = "Last name is required")
        String lastname,

        @Email(message = "Email is required")
        String email
) {
}
