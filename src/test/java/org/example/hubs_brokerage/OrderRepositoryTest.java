package org.example.hubs_brokerage;

import org.example.hubs_brokerage.model.Order;
import org.example.hubs_brokerage.model.OrderSide;
import org.example.hubs_brokerage.model.OrderStatus;
import org.example.hubs_brokerage.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TestEntityManager entityManager;
/*
    @Test
    void findByCustomerIdAndStatus_shouldReturnFilteredResults() {
        // Given
        Order order1 = new Order("cust1", "AAPL", OrderSide.BUY, 10, 150.0);
        order1.setStatus(OrderStatus.PENDING);
        entityManager.persist(order1);

        Order order2 = new Order("cust1", "TSLA", OrderSide.SELL, 5, 300.0);
        order2.setStatus(OrderStatus.MATCHED);
        entityManager.persist(order2);

        // When
        Page<Order> result = orderRepository.findByCustomerIdAndStatus(
                "cust1",
                OrderStatus.PENDING,
                PageRequest.of(0, 10)
        );

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("AAPL", result.getContent().get(0).getAssetName());
    }

 */
}
