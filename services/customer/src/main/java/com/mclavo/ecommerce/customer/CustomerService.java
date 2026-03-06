package com.mclavo.ecommerce.customer;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mclavo.ecommerce.exception.CustomerNotFoundException;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    public CustomerResponse createCustomer(CustomerRequest request) {
        Customer customer = repository.save(mapper.toCustomer(request));
        return mapper.toCustomerResponse(customer);
    }

    public void updateCustomer(String id, CustomerRequest request) {
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(
                        String.format("Cannot update customer:: No customer found with the provided ID:: %s", id)));

        mergeCustomer(customer, request);
        repository.save(customer);

    }

    private void mergeCustomer(Customer customer, CustomerRequest request) {
        if (StringUtils.isNotBlank(request.firstname())) {
            customer.setFirstname(request.firstname());
        }

        if (StringUtils.isNotBlank(request.lastname())) {
            customer.setLastname(request.lastname());
        }

        if (StringUtils.isNotBlank(request.email())) {
            customer.setEmail(request.email());
        }

        if (request.address() != null) {
            customer.setAddress(request.address());
        }
    }

    public CustomerResponse getCustomer(String id) {
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(
                        String.format("Cannot get customer:: No customer found with the provided ID:: %s", id)));

        return mapper.toCustomerResponse(customer);

    }

    public List<CustomerResponse> findAllCustomers() {
        return repository.findAll().stream()
                .map(mapper::toCustomerResponse)
                .toList();
    }

    public Boolean existsById(String id) {
        return repository.existsById(id);
    }

    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
