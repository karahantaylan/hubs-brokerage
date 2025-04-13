package org.example.hubs_brokerage.model;

import jakarta.persistence.*;
import jakarta.persistence.Id;

@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    private String role; // Örneğin, "ADMIN" veya "CUSTOMER"

    // Getterlar ve Setterlar
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String customerId) {
        this.username = customerId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Constructors
    public Customer() {
    }

    public Customer(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}