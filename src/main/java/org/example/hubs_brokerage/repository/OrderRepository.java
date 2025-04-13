package org.example.hubs_brokerage.repository;

import org.example.hubs_brokerage.model.Order;
import org.example.hubs_brokerage.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Müşteriye ve statüye göre filtreleme (Pagination destekli)
    Page<Order> findByCustomerIdAndStatus(String customerId, OrderStatus status, Pageable pageable);

    // JPQL ile özelleştirilmiş sorgu
    @Query("SELECT o FROM Order o WHERE " +
            "o.customerId = :customerId AND " +
            "(:status IS NULL OR o.status = :status) AND " +
            "o.createdAt BETWEEN :startDate AND :endDate")
    Page<Order> findFilteredOrders(
            @Param("customerId") String customerId,
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // Statüye göre sayım
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") OrderStatus status);

    // Dinamik sorgu için default metod
    default Page<Order> findOrdersByCustomer(String customerId, OrderStatus status, Pageable pageable) {
        if (status != null) {
            return findByCustomerIdAndStatus(customerId, status, pageable);
        }
        return findByCustomerId(customerId, pageable);
    }

    // Müşteriye göre emirleri listeleme
    Page<Order> findByCustomerId(String customerId, Pageable pageable);

    // Tarih aralığına göre filtreleme
    List<Order> findByCustomerIdAndCreatedAtBetween(
            String customerId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // Statüye göre sorgulama
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.customerId = :customerId")
    List<Order> findOrdersByStatus(
            @Param("customerId") String customerId,
            @Param("status") OrderStatus status
    );

    // İptal işlemi için custom update
    @Modifying
    @Query("UPDATE Order o SET o.status = 'CANCELLED' WHERE o.id = :id AND o.status = 'PENDING'")
    int cancelPendingOrder(@Param("id") Long id);
}
