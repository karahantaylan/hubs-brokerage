package org.example.hubs_brokerage;

import org.example.hubs_brokerage.model.Asset;
import org.example.hubs_brokerage.repository.AssetRepository;
import org.example.hubs_brokerage.service.AssetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetService assetService;
/*
    @Test
    void reserveAsset_shouldThrowWhenInsufficientBalance() {
        // Given
        when(assetRepository.deductUsableAmount(any(), any(), any()))
                .thenReturn(0);

        // When/Then
        assertThrows(IllegalStateException.class, () -> {
            assetService.reserveAsset("cust1", "TRY", 1000);
        });
    }

 */
}