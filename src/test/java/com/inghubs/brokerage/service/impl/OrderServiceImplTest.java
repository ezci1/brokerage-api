package com.inghubs.brokerage.service.impl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.annotation.Transactional;

import com.inghubs.brokerage.dto.enumeration.OrderStatus;
import com.inghubs.brokerage.dto.enumeration.Side;
import com.inghubs.brokerage.dto.mapper.OrderMapper;
import com.inghubs.brokerage.dto.model.Asset;
import com.inghubs.brokerage.dto.model.Order;
import com.inghubs.brokerage.dto.request.CreateOrderRequest;
import com.inghubs.brokerage.dto.request.GetOrdersRequest;
import com.inghubs.brokerage.repository.AssetRepository;
import com.inghubs.brokerage.repository.OrderRepository;

class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private OrderMapper orderMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetOrders() {
        GetOrdersRequest request = new GetOrdersRequest();
        request.setCustomerId(1L);
        request.setStartDate(LocalDate.now().minusDays(1));
        request.setEndDate(LocalDate.now());

        Order order = new Order();
        order.setCustomerId(1L);

        when(orderRepository.findByCustomerIdAndCreateDateBetween(request.getCustomerId(), 
                request.getStartDate(), request.getEndDate())).thenReturn(Collections.singletonList(order));

        List<Order> result = orderService.getOrders(request);

        assertEquals(1, result.size());
        verify(orderRepository).findByCustomerIdAndCreateDateBetween(request.getCustomerId(), 
                request.getStartDate(), request.getEndDate());
    }

    @Test
    @Transactional
    void testCreateOrder_BuyOrder_Success() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(1L);
        request.setSide(Side.BUY);
        request.setAssetName("BTC");
        request.setSize(BigDecimal.valueOf(1));
        request.setPricePerShare(BigDecimal.valueOf(100));

        Asset fiatAsset = new Asset();
        fiatAsset.setUsableSize(BigDecimal.valueOf(150)); // Sufficient funds

        when(assetRepository.findByCustomerIdAndAssetName(1L, "TRY"))
                .thenReturn(Optional.of(fiatAsset));
        when(orderMapper.mapRequestToOrder(request)).thenReturn(new Order());

        orderService.createOrder(request);

        verify(assetRepository).save(fiatAsset);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @Transactional
    void testCreateOrder_SellOrder_Success() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(1L);
        request.setSide(Side.SELL);
        request.setAssetName("BTC");
        request.setSize(BigDecimal.valueOf(1));

        Asset asset = new Asset();
        asset.setUsableSize(BigDecimal.valueOf(5)); // Sufficient asset quantity

        when(assetRepository.findByCustomerIdAndAssetName(1L, "BTC"))
                .thenReturn(Optional.of(asset));
        when(orderMapper.mapRequestToOrder(request)).thenReturn(new Order());

        orderService.createOrder(request);

        verify(assetRepository).save(asset);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testCreateOrder_BuyOrder_InsufficientFunds() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(1L);
        request.setSide(Side.BUY);
        request.setAssetName("BTC");
        request.setSize(BigDecimal.valueOf(1));
        request.setPricePerShare(BigDecimal.valueOf(100));

        Asset fiatAsset = new Asset();
        fiatAsset.setUsableSize(BigDecimal.valueOf(50)); // Insufficient funds

        when(assetRepository.findByCustomerIdAndAssetName(1L, "TRY"))
                .thenReturn(Optional.of(fiatAsset));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(request);
        });

        assertEquals("Insufficient funds to buy", exception.getMessage());
    }

    @Test
    void testCreateOrder_SellOrder_InsufficientQuantity() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(1L);
        request.setSide(Side.SELL);
        request.setAssetName("BTC");
        request.setSize(BigDecimal.valueOf(10)); // More than available

        Asset asset = new Asset();
        asset.setUsableSize(BigDecimal.valueOf(5)); // Insufficient asset quantity

        when(assetRepository.findByCustomerIdAndAssetName(1L, "BTC"))
                .thenReturn(Optional.of(asset));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(request);
        });

        assertEquals("Insufficient asset quantity to sell", exception.getMessage());
    }

    @Test
    @Transactional
    void testDeleteOrder_Success() {
        Order order = new Order();
        order.setId(1L);
        order.setSide(Side.BUY);
        order.setCustomerId(1L);
        order.setSize(BigDecimal.valueOf(1));
        order.setPrice(BigDecimal.valueOf(100));
        order.setStatus(OrderStatus.PENDING);

        Asset fiatAsset = new Asset();
        fiatAsset.setUsableSize(BigDecimal.valueOf(150)); // Sufficient funds

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), "TRY"))
                .thenReturn(Optional.of(fiatAsset));

        orderService.deleteOrder(1L);

        verify(assetRepository).save(fiatAsset);
        verify(orderRepository).save(order);
        assertEquals(OrderStatus.CANCELED, order.getStatus());
    }

    @Test
    void testDeleteOrder_OrderNotFound() {
        var exception = assertThrows(RuntimeException.class, () -> {
            orderService.deleteOrder(1L);
        });

        assertEquals("Order not found", exception.getMessage());
    }

    @Test
    void testDeleteOrder_NonPendingOrder() {
        var order = new Order();
        order.setId(1L);
        order.setSide(Side.BUY);
        order.setCustomerId(1L);
        order.setSize(BigDecimal.valueOf(1));
        order.setPrice(BigDecimal.valueOf(100));
        order.setStatus(OrderStatus.MATCHED); // Non-pending status

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        var exception = assertThrows(RuntimeException.class, () -> {
            orderService.deleteOrder(1L);
        });

        assertEquals("Only pending orders can be deleted", exception.getMessage());
    }
}
