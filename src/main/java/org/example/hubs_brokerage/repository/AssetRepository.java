package org.example.hubs_brokerage.repository;

import org.example.hubs_brokerage.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByCustomerId(String customerId);

    Optional<Asset> findByCustomerIdAndAssetName(String customerId, String assetName);

    @Modifying
    @Query("UPDATE Asset a SET a.usableSize = a.usableSize - :amount " +
            "WHERE a.customerId = :customerId AND a.assetName = :assetName AND a.usableSize >= :amount")
    int deductUsableAmount(
            @Param("customerId") String customerId,
            @Param("assetName") String assetName,
            @Param("amount") double amount
    );

    @Modifying
    @Query("UPDATE Asset a SET a.usableSize = a.usableSize + :amount " +
            "WHERE a.customerId = :customerId AND a.assetName = :assetName")
    void increaseUsableAmount(
            @Param("customerId") String customerId,
            @Param("assetName") String assetName,
            @Param("amount") double amount
    );

    @Query("SELECT a.usableSize FROM Asset a WHERE a.customerId = :customerId AND a.assetName = :assetName")
    Optional<Double> findUsableSizeByCustomerAndAsset(
            @Param("customerId") String customerId,
            @Param("assetName") String assetName
    );
}
