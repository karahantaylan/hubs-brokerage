package org.example.hubs_brokerage.repository;

import org.example.hubs_brokerage.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    //Optional<Customer> findByUsername(String username);
}