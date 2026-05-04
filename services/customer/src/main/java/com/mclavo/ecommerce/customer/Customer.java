package com.mclavo.ecommerce.customer;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Document
public class Customer {

    @Id
    private String id;
    private String firstname;
    private String lastname;
    private String email;
    private Address address;
}
