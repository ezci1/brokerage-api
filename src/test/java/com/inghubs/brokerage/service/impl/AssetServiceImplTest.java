package com.inghubs.brokerage.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.inghubs.brokerage.dto.model.Asset;
import com.inghubs.brokerage.dto.request.DepositMoneyRequest;
import com.inghubs.brokerage.dto.request.WithdrawMoneyRequest;
import com.inghubs.brokerage.repository.AssetRepository;

class AssetServiceImplTest {

    @InjectMocks
    private AssetServiceImpl assetServiceImpl;

    @Mock
    private AssetRepository assetRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        assetServiceImpl = new AssetServiceImpl(assetRepository);
    }

    @Test
    void testDepositMoney_Success() {
        DepositMoneyRequest request = new DepositMoneyRequest();
        request.setCustomerId(1L);
        request.setAmount(new BigDecimal("100"));
        
        Asset existingAsset = new Asset();
        existingAsset.setCustomerId(1L);
        existingAsset.setAssetName("TRY");
        existingAsset.setSize(new BigDecimal("200"));
        existingAsset.setUsableSize(new BigDecimal("200"));
        
        Mockito.when(assetRepository.findByCustomerIdAndAssetName(1L, "TRY"))
               .thenReturn(Optional.of(existingAsset));

        assetServiceImpl.depositMoney(request);

        assertEquals(new BigDecimal("300"), existingAsset.getSize());
        assertEquals(new BigDecimal("300"), existingAsset.getUsableSize());
        Mockito.verify(assetRepository).save(existingAsset);
    }

    @Test
    void testDepositMoney_NewAssetCreated() {
        DepositMoneyRequest request = new DepositMoneyRequest();
        request.setCustomerId(2L);
        request.setAmount(new BigDecimal("150"));
        
        Mockito.when(assetRepository.findByCustomerIdAndAssetName(2L, "TRY"))
               .thenReturn(Optional.empty());

        Asset savedAsset = new Asset();
        savedAsset.setCustomerId(2L);
        savedAsset.setAssetName("TRY");
        savedAsset.setSize(new BigDecimal("150"));
        savedAsset.setUsableSize(new BigDecimal("150"));
        
        Mockito.when(assetRepository.save(ArgumentMatchers.any(Asset.class)))
               .thenReturn(savedAsset);

        assetServiceImpl.depositMoney(request);

        Mockito.verify(assetRepository).save(ArgumentMatchers.any(Asset.class));
    }

    @Test
    void testWithdrawMoney_Success() {
        WithdrawMoneyRequest request = new WithdrawMoneyRequest();
        request.setCustomerId(1L);
        request.setAmount(new BigDecimal("50"));
        
        Asset existingAsset = new Asset();
        existingAsset.setCustomerId(1L);
        existingAsset.setAssetName("TRY");
        existingAsset.setSize(new BigDecimal("100"));
        existingAsset.setUsableSize(new BigDecimal("100"));
        
        Mockito.when(assetRepository.findByCustomerIdAndAssetName(1L, "TRY"))
               .thenReturn(Optional.of(existingAsset));

        assetServiceImpl.withdrawMoney(request);

        assertEquals(new BigDecimal("50"), existingAsset.getUsableSize());
        Mockito.verify(assetRepository).save(existingAsset);
    }

    @Test
    void testWithdrawMoney_InsufficientFunds() {
        WithdrawMoneyRequest request = new WithdrawMoneyRequest();
        request.setCustomerId(1L);
        request.setAmount(new BigDecimal("150"));
        
        Asset existingAsset = new Asset();
        existingAsset.setCustomerId(1L);
        existingAsset.setAssetName("TRY");
        existingAsset.setSize(new BigDecimal("100"));
        existingAsset.setUsableSize(new BigDecimal("100"));
        
        Mockito.when(assetRepository.findByCustomerIdAndAssetName(1L, "TRY"))
               .thenReturn(Optional.of(existingAsset));

        assertThrows(RuntimeException.class, () -> assetServiceImpl.withdrawMoney(request));
        assertEquals(new BigDecimal("100"), existingAsset.getUsableSize());
    }

    @Test
    void testGetCustomerAssets() {
        Long customerId = 1L;
        
        Asset asset = new Asset();
        asset.setCustomerId(customerId);
        asset.setAssetName("TRY");
        asset.setSize(new BigDecimal("100"));
        asset.setUsableSize(new BigDecimal("100"));
        
        Mockito.when(assetRepository.findByCustomerId(customerId))
               .thenReturn(Collections.singletonList(asset));

        List<Asset> assets = assetServiceImpl.getCustomerAssets(customerId);

        assertEquals(1, assets.size());
        assertEquals("TRY", assets.get(0).getAssetName());
        assertEquals(new BigDecimal("100"), assets.get(0).getSize());
        assertEquals(new BigDecimal("100"), assets.get(0).getUsableSize());
    }
}
