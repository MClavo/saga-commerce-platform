package com.mclavo.ecommerce.notification;

public record RecipientSnapshot(
        String customerId,
        String firstName,
        String lastName,
        String email
) {
    public String fullName() {
        return firstName + " " + lastName;
    }
}
