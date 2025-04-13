package org.example.hubs_brokerage.service;

import org.example.hubs_brokerage.dto.AssetResponse;
import org.example.hubs_brokerage.exception.InsufficientBalanceException;
import org.example.hubs_brokerage.exception.ResourceNotFoundException;
import org.example.hubs_brokerage.model.Asset;
import org.example.hubs_brokerage.model.Order;
import org.example.hubs_brokerage.model.OrderSide;
import org.example.hubs_brokerage.repository.AssetRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class AssetService {

    private final AssetRepository assetRepository;

    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    /**
     * Müşterinin tüm varlıklarını getirir (Cache'lenir)
     */
    @Cacheable(value = "customerAssets", key = "#customerId")
    public List<AssetResponse> listAssets(String customerId) {
        return assetRepository.findByCustomerId(customerId)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    /**
     * Belirli bir varlığın detayını getirir
     */
    @Transactional(readOnly = true)
    public AssetResponse getAssetDetails(String customerId, String assetName) {
        Asset asset = assetRepository.findByCustomerIdAndAssetName(customerId, assetName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Asset %s not found for customer %s", assetName, customerId))
                );
        return convertToDto(asset);
    }

    @Transactional(readOnly = true)
    public double getAvailableBalance(String customerId, String assetName) {
        return assetRepository.findByCustomerIdAndAssetName(customerId, assetName)
                .map(Asset::getUsableSize)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("%s asset not found for customer %s", assetName, customerId)
                ));
    }

    /**
     * Tüm varlıkların kullanılabilir bakiyelerini Map olarak getirir
     */
    @Transactional(readOnly = true)
    public Map<String, Double> getAvailableBalances(String customerId) {
        return assetRepository.findByCustomerId(customerId)
                .stream()
                .collect(Collectors.toMap(
                        Asset::getAssetName,
                        Asset::getUsableSize
                ));
    }

    /**
     * Varlık rezervasyonu yapar (Cache'i invalidate eder)
     */
    @CacheEvict(value = "customerAssets", key = "#customerId")
    public void reserveAsset(String customerId, String assetName, double amount) {
        int updatedRows = assetRepository.deductUsableAmount(customerId, assetName, amount);
        if (updatedRows == 0) {
            throw new IllegalStateException("Insufficient assets or not found");
        }
    }

    /**
     * Varlık iadesi yapar (Cache'i invalidate eder)
     */
    @CacheEvict(value = "customerAssets", key = "#customerId")
    public void refundAsset(String customerId, String assetName, double amount) {
        assetRepository.increaseUsableAmount(customerId, assetName, amount);
    }

    /**
     * Yeni varlık ekler
     */
    @CacheEvict(value = "customerAssets", key = "#customerId")
    public Asset createAsset(String customerId, String assetName, double totalAmount) {
        Asset asset = new Asset(customerId, assetName, totalAmount, totalAmount);
        return assetRepository.save(asset);
    }

    private AssetResponse convertToDto(Asset asset) {
        return new AssetResponse(
                asset.getAssetName(),
                asset.getSize(),
                asset.getUsableSize(),
                asset.getSize() - asset.getUsableSize(),
                calculatePercentage(asset.getUsableSize(), asset.getSize())
        );
    }

    private double calculatePercentage(double usable, double total) {
        return total > 0 ? (usable / total) * 100 : 0;
    }
}