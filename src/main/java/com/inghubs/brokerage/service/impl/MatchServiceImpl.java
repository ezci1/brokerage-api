package com.inghubs.brokerage.service.impl;

import java.math.BigDecimal;
import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.inghubs.brokerage.dto.enumeration.OrderStatus;
import com.inghubs.brokerage.dto.enumeration.Side;
import com.inghubs.brokerage.dto.model.Asset;
import com.inghubs.brokerage.dto.model.Order;
import com.inghubs.brokerage.repository.AssetRepository;
import com.inghubs.brokerage.repository.OrderRepository;
import com.inghubs.brokerage.service.MatchService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;

    @Transactional
    @Override
    public void processOrder(Order newOrder) {
        if (newOrder.getSide() == Side.SELL) {
            matchSellOrder(newOrder);
        } else if (newOrder.getSide() == Side.BUY) {
            matchBuyOrder(newOrder);
        }
    }
    
    private void matchSellOrder(Order sellOrder) {
        // Find the oldest matching buy order with price >= sellOrder's price to match
        var matchingBuyOrder = orderRepository.findTopByAssetNameAndSideAndStatusAndPriceGreaterThanEqualOrderByCreateDateAsc(
                sellOrder.getAssetName(), Side.BUY, OrderStatus.PENDING, sellOrder.getPrice());

        if(matchingBuyOrder.isEmpty()) {
        	return;
        }
        
        var isExecuted = executeTransaction(sellOrder, matchingBuyOrder.get());
        
        //matching is successful but there is still some quantity to sell
        if(isExecuted && sellOrder.getSize().compareTo(BigDecimal.ZERO) == 1) {
        	matchSellOrder(sellOrder);
        }
    }

    private void matchBuyOrder(Order buyOrder) {
        // Find the oldest matching sell order with price <= buyOrder's price to match
        var matchingSellOrder = orderRepository.findTopByAssetNameAndSideAndStatusAndPriceLessThanEqualOrderByCreateDateAsc(
                buyOrder.getAssetName(), Side.SELL, OrderStatus.PENDING, buyOrder.getPrice());

        if(matchingSellOrder.isEmpty()) {
        	return;
        }

        var isExecuted = executeTransaction(buyOrder, matchingSellOrder.get());

        //matching is successful but there is still some quantity to buy
        if(isExecuted && buyOrder.getSize().compareTo(BigDecimal.ZERO) == 1) {
        	matchSellOrder(buyOrder);
        }
        
    }

    @Transactional
    private boolean executeTransaction(Order sellOrder, Order buyOrder) {
        //resting price
        var matchedPrice = sellOrder.getPrice();
        var matchedSize = sellOrder.getSize().min(buyOrder.getSize());
        
        updateAssetBalances(buyOrder, sellOrder, matchedPrice, matchedSize);

        // Update the sizes of the orders involved in the transaction
        sellOrder.setSize(sellOrder.getSize().subtract(matchedSize));
        buyOrder.setSize(buyOrder.getSize().subtract(matchedSize));

        if (sellOrder.getSize().compareTo(BigDecimal.ZERO) == 0) {
            sellOrder.setStatus(OrderStatus.MATCHED);
        }
        if (buyOrder.getSize().compareTo(BigDecimal.ZERO) == 0) {
            buyOrder.setStatus(OrderStatus.MATCHED);
        }

        orderRepository.save(sellOrder);
        orderRepository.save(buyOrder);
        
        return sellOrder.getStatus() == OrderStatus.MATCHED || buyOrder.getStatus() == OrderStatus.MATCHED;
    }
    
    private void updateAssetBalances(Order buyOrder, Order sellOrder, BigDecimal matchedPrice, BigDecimal matchedSize) {
        var buyerAsset = assetRepository.findByCustomerIdAndAssetName(buyOrder.getCustomerId(), buyOrder.getAssetName()).orElseThrow();
        var sellerAsset = assetRepository.findByCustomerIdAndAssetName(sellOrder.getCustomerId(), sellOrder.getAssetName()).orElseThrow();
        
        var totalCost = matchedPrice.multiply(matchedSize);

        var buyerFiatAsset = assetRepository.findByCustomerIdAndAssetName(buyOrder.getCustomerId(), "TRY").orElseThrow();
        var sellerFiatAsset = assetRepository.findByCustomerIdAndAssetName(sellOrder.getCustomerId(), "TRY").orElseGet(() -> createFiatAssetSeller(sellOrder.getCustomerId()));
        
        buyerFiatAsset.setSize(buyerFiatAsset.getSize().subtract(totalCost));
        sellerFiatAsset.setSize(sellerFiatAsset.getSize().add(totalCost));
        sellerFiatAsset.setUsableSize(sellerFiatAsset.getUsableSize().add(totalCost));

        buyerAsset.setUsableSize(buyerAsset.getUsableSize().subtract(matchedSize));
        sellerAsset.setUsableSize(sellerAsset.getUsableSize().add(matchedSize));

        assetRepository.saveAll(Arrays.asList(buyerAsset, sellerAsset, buyerFiatAsset, sellerFiatAsset));
    }

    private Asset createFiatAssetSeller(Long customerId) {
		var asset = new Asset();
		asset.setAssetName("TRY");
		asset.setCustomerId(customerId);
		asset.setSize(BigDecimal.ZERO);
		asset.setUsableSize(BigDecimal.ZERO);
		return asset;
	}
}
