package org.example.hubs_brokerage.dto;

import org.example.hubs_brokerage.model.Asset;

public record AssetResponse(
        String assetName,
        double totalSize,
        double usableSize,
        double lockedSize ,
        double availablePercentage
) {

    public static AssetResponse fromEntity(Asset asset) {
        double availablePercentage = asset.getSize() > 0 ?
                (asset.getUsableSize() / asset.getSize()) * 100 : 0;

        return new AssetResponse(
                asset.getAssetName(),
                asset.getSize(),
                asset.getUsableSize(),
                asset.getSize() - asset.getUsableSize(),
                availablePercentage
        );
    }
}
