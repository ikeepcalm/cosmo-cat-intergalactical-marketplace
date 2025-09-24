package net.cosmocat.marketplace.database.dto.entity;

import lombok.Data;
import net.cosmocat.marketplace.database.entity.Customer;

import java.time.LocalDateTime;

@Data
public class CustomerDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String city;
    private String postalCode;
    private String country;
    private LocalDateTime createdAt;

    public CustomerDTO(Customer customer) {
        this.id = customer.getId();
        this.username = customer.getUsername();
        this.email = customer.getEmail();
        this.firstName = customer.getFirstName();
        this.lastName = customer.getLastName();
        this.phone = customer.getPhone();
        this.address = customer.getAddress();
        this.city = customer.getCity();
        this.postalCode = customer.getPostalCode();
        this.country = customer.getCountry();
        this.createdAt = customer.getCreatedAt();
    }
}