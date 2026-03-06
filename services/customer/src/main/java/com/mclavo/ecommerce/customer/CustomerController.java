package com.mclavo.ecommerce.customer;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;

    @PostMapping()
    public ResponseEntity<CustomerResponse> createCustomer(
            @RequestBody @Valid CustomerRequest request) {

        return ResponseEntity.ok(service.createCustomer(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateCustomer(
            @PathVariable String id,
            @RequestBody @Valid CustomerRequest request) {
        service.updateCustomer(id, request);
        return ResponseEntity.accepted().build();
    }

    @GetMapping()
    public ResponseEntity<List<CustomerResponse>> findAll() {
        return ResponseEntity.ok(service.findAllCustomers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(
            @PathVariable String id) {
        return ResponseEntity.ok(service.getCustomer(id));
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> existsById(
            @PathVariable String id) {
        return ResponseEntity.ok(service.existsById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id) {
        service.deleteById(id);
        return ResponseEntity.accepted().build();
    }

}
