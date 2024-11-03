package com.inghubs.brokerage.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.inghubs.brokerage.dto.model.Asset;
import com.inghubs.brokerage.dto.request.DepositMoneyRequest;
import com.inghubs.brokerage.dto.request.WithdrawMoneyRequest;
import com.inghubs.brokerage.repository.AssetRepository;
import com.inghubs.brokerage.service.AssetService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;

    @Override
    public void depositMoney(DepositMoneyRequest request) {
        Asset asset = assetRepository.findByCustomerIdAndAssetName(request.getCustomerId(), "TRY")
                .orElseGet(() -> createNewAsset(request));
        asset.setSize(asset.getSize().add(request.getAmount()));
        asset.setUsableSize(asset.getUsableSize().add(request.getAmount()));
        assetRepository.save(asset);
    }

	@Override
    public void withdrawMoney(WithdrawMoneyRequest request) {
        Asset asset = assetRepository.findByCustomerIdAndAssetName(request.getCustomerId(), "TRY")
                .orElseThrow(() -> new RuntimeException("Insufficient funds"));
        if (asset.getUsableSize().compareTo(request.getAmount()) == -1) {
            throw new RuntimeException("Insufficient funds");
        }
        asset.setUsableSize(asset.getUsableSize().subtract(request.getAmount()));
        assetRepository.save(asset);
    }

    @Override
    public List<Asset> getCustomerAssets(Long customerId) {
        return assetRepository.findByCustomerId(customerId);
    }  
    
    private Asset createNewAsset(DepositMoneyRequest request) {
		Asset asset = new Asset();
		asset.setCustomerId(request.getCustomerId());
		asset.setAssetName("TRY");
		asset.setSize(BigDecimal.ZERO);
		asset.setUsableSize(BigDecimal.ZERO);
		return asset;
	}
}

