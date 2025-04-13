package org.example.hubs_brokerage.controller;

import org.example.hubs_brokerage.dto.AssetResponse;
import org.example.hubs_brokerage.service.AssetService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    public List<AssetResponse> listAssets(@RequestParam String customerId) {
        return assetService.listAssets(customerId);
    }

    @GetMapping("/{assetName}")
    public AssetResponse getAsset(
            @RequestParam String customerId,
            @PathVariable String assetName) {
        return assetService.getAssetDetails(customerId, assetName);
    }

    @PostMapping
    public AssetResponse createAsset(
            @RequestParam String customerId,
            @RequestParam String assetName,
            @RequestParam double initialAmount) {
        return AssetResponse.fromEntity(
                assetService.createAsset(customerId, assetName, initialAmount)
        );
    }
}
