package com.inghubs.brokerage.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.inghubs.brokerage.dto.enumeration.OrderStatus;
import com.inghubs.brokerage.dto.enumeration.Side;
import com.inghubs.brokerage.dto.model.Asset;
import com.inghubs.brokerage.dto.model.Order;
import com.inghubs.brokerage.repository.AssetRepository;
import com.inghubs.brokerage.repository.OrderRepository;

class MatchServiceImplTest {

    @InjectMocks
    private MatchServiceImpl matchService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AssetRepository assetRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessOrder_SellOrderSuccessfullyMatched() {
        var sellOrder = generateOrder(1L, BigDecimal.valueOf(100), BigDecimal.valueOf(10), Side.SELL);
        var buyOrder = generateOrder(2L, BigDecimal.valueOf(110), BigDecimal.valueOf(10), Side.BUY);

        sellOrder.setStatus(OrderStatus.PENDING);
        buyOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findTopByAssetNameAndSideAndStatusAndPriceGreaterThanEqualOrderByCreateDateAsc(
                sellOrder.getAssetName(), Side.BUY, OrderStatus.PENDING, sellOrder.getPrice()))
                .thenReturn(Optional.of(buyOrder));
        when(assetRepository.findByCustomerIdAndAssetName(buyOrder.getCustomerId(), buyOrder.getAssetName()))
		.thenReturn(Optional.of(generateAsset()));
        when(assetRepository.findByCustomerIdAndAssetName(sellOrder.getCustomerId(), sellOrder.getAssetName()))
		.thenReturn(Optional.of(generateAsset()));
        when(assetRepository.findByCustomerIdAndAssetName(buyOrder.getCustomerId(), "TRY"))
		.thenReturn(Optional.of(generateAsset()));
        when(assetRepository.findByCustomerIdAndAssetName(sellOrder.getCustomerId(), "TRY"))
		.thenReturn(Optional.of(generateAsset()));

        matchService.processOrder(sellOrder);

        verify(orderRepository).save(sellOrder);
        verify(orderRepository).save(buyOrder);
    }

	private Order generateOrder(Long customerId, BigDecimal price, BigDecimal size, Side side) {
		var buyOrder = new Order();
        buyOrder.setSide(side);
        buyOrder.setSize(size);
        buyOrder.setPrice(price);
        buyOrder.setAssetName("BTC");
        buyOrder.setCustomerId(customerId);
		return buyOrder;
	}

    @Test
    void testProcessOrder_BuyOrderSuccessfullyMatched() {
        var buyOrder = generateOrder(1L, BigDecimal.valueOf(100), BigDecimal.valueOf(10), Side.BUY);
        var sellOrder = generateOrder(2L, BigDecimal.valueOf(90), BigDecimal.valueOf(10), Side.SELL);

        sellOrder.setStatus(OrderStatus.PENDING);
        buyOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findTopByAssetNameAndSideAndStatusAndPriceLessThanEqualOrderByCreateDateAsc(
                buyOrder.getAssetName(), Side.SELL, OrderStatus.PENDING, buyOrder.getPrice()))
                .thenReturn(Optional.of(sellOrder));

        when(assetRepository.findByCustomerIdAndAssetName(buyOrder.getCustomerId(), buyOrder.getAssetName()))
		.thenReturn(Optional.of(generateAsset()));
        when(assetRepository.findByCustomerIdAndAssetName(sellOrder.getCustomerId(), sellOrder.getAssetName()))
		.thenReturn(Optional.of(generateAsset()));
        when(assetRepository.findByCustomerIdAndAssetName(buyOrder.getCustomerId(), "TRY"))
		.thenReturn(Optional.of(generateAsset()));
        when(assetRepository.findByCustomerIdAndAssetName(sellOrder.getCustomerId(), "TRY"))
		.thenReturn(Optional.of(generateAsset()));
        
        matchService.processOrder(buyOrder);

        verify(orderRepository).save(sellOrder);
        verify(orderRepository).save(buyOrder);
        verify(assetRepository).saveAll(any());
    }

    private Asset generateAsset() {
		var asset = new Asset();
		asset.setAssetName("AAPL");
		asset.setSize(BigDecimal.ONE);
		asset.setUsableSize(BigDecimal.ONE);
		return asset;
	}

	@Test
    void testProcessOrder_SellOrderWithNoMatchingBuyOrder() {
        var sellOrder = generateOrder(2L, BigDecimal.valueOf(90), BigDecimal.valueOf(10), Side.SELL);

        when(orderRepository.findTopByAssetNameAndSideAndStatusAndPriceGreaterThanEqualOrderByCreateDateAsc(
                sellOrder.getAssetName(), Side.BUY, OrderStatus.PENDING, sellOrder.getPrice()))
                .thenReturn(Optional.empty());

        matchService.processOrder(sellOrder);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void testProcessOrder_BuyOrderWithNoMatchingSellOrder() {
        Order buyOrder = new Order();
        buyOrder.setSide(Side.BUY);
        buyOrder.setSize(BigDecimal.valueOf(10));
        buyOrder.setPrice(BigDecimal.valueOf(100));
        buyOrder.setAssetName("BTC");
        buyOrder.setCustomerId(1L);

        when(orderRepository.findTopByAssetNameAndSideAndStatusAndPriceLessThanEqualOrderByCreateDateAsc(
                buyOrder.getAssetName(), Side.SELL, OrderStatus.PENDING, buyOrder.getPrice()))
                .thenReturn(Optional.empty());

        matchService.processOrder(buyOrder);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void testProcessOrder_SequentialSellOrders() {
        var firstSellOrder = generateOrder(1L, BigDecimal.valueOf(90), BigDecimal.valueOf(20), Side.SELL);
        var secondSellOrder = generateOrder(2L, BigDecimal.valueOf(90), BigDecimal.valueOf(10), Side.SELL);
        var buyOrder = generateOrder(3L, BigDecimal.valueOf(110), BigDecimal.valueOf(15), Side.BUY);

        firstSellOrder.setStatus(OrderStatus.PENDING);
        secondSellOrder.setStatus(OrderStatus.PENDING);
        buyOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findTopByAssetNameAndSideAndStatusAndPriceGreaterThanEqualOrderByCreateDateAsc(
                firstSellOrder.getAssetName(), Side.BUY, OrderStatus.PENDING, firstSellOrder.getPrice()))
                .thenReturn(Optional.of(buyOrder))
                .thenReturn(Optional.empty());

        when(orderRepository.findTopByAssetNameAndSideAndStatusAndPriceGreaterThanEqualOrderByCreateDateAsc(
                secondSellOrder.getAssetName(), Side.BUY, OrderStatus.PENDING, secondSellOrder.getPrice()))
                .thenReturn(Optional.of(buyOrder))
                .thenReturn(Optional.empty());

        when(assetRepository.findByCustomerIdAndAssetName(any(), any()))
		.thenReturn(Optional.of(generateAsset()));
        
        matchService.processOrder(firstSellOrder);
        matchService.processOrder(secondSellOrder);

        verify(orderRepository, times(2)).save(any());
        verify(assetRepository, times(1)).saveAll(any());
    }
}
