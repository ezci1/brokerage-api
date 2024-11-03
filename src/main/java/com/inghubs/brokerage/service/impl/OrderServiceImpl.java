package com.inghubs.brokerage.service.impl;
import java.util.List;

import org.springframework.stereotype.Service;
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
import com.inghubs.brokerage.service.OrderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;
    private final OrderMapper orderMapper;

    @Override
    public List<Order> getOrders(GetOrdersRequest request) {
        return orderRepository.findByCustomerIdAndCreateDateBetween(request.getCustomerId(), request.getStartDate(), request.getEndDate());
    }

    @Transactional
    @Override
    public void createOrder(CreateOrderRequest request) {
    	
        Asset asset;
        if (request.getSide() == Side.BUY) {
            asset = getUpdatedFiatAsset(request);
        } else {
            asset = getUpdatedAsset(request);
        }

		assetRepository.save(asset);

        var order = orderMapper.mapRequestToOrder(request);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);
    }

	private Asset getUpdatedAsset(CreateOrderRequest request) {
		var asset = assetRepository.findByCustomerIdAndAssetName(request.getCustomerId(), request.getAssetName())
		        .orElseThrow(() -> new RuntimeException("Asset not found for sale"));
		
		if (asset.getUsableSize().compareTo(request.getSize()) == -1) {
		    throw new RuntimeException("Insufficient asset quantity to sell");
		}
		
		var newUsableSize = asset.getUsableSize().subtract(request.getSize());
		asset.setUsableSize(newUsableSize);
		
		return asset;
	}

	private Asset getUpdatedFiatAsset(CreateOrderRequest request) {
		var asset = assetRepository.findByCustomerIdAndAssetName(request.getCustomerId(), "TRY")
		        .orElseThrow(() -> new RuntimeException("Customer TRY balance not found"));
		
		var totalCost = request.getSize().multiply(request.getPricePerShare());
		
		if (asset.getUsableSize().compareTo(totalCost) == -1) {
		    throw new RuntimeException("Insufficient funds to buy");
		}
		
		asset.setUsableSize(asset.getUsableSize().subtract(totalCost));
		
		return asset;
	}

    @Transactional
    @Override
    public void deleteOrder(Long orderId) {
        var order = getOrder(orderId);

        if (order.getSide() == Side.BUY) {
            var customerAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), "TRY")
                    .orElseThrow(() -> new RuntimeException("Customer TRY balance not found"));
            var newUsableSize = customerAsset.getUsableSize().add(order.getSize().multiply(order.getPrice()));

            customerAsset.setUsableSize(newUsableSize);
            assetRepository.save(customerAsset);
        } else {
            var asset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName())
                    .orElseThrow(() -> new RuntimeException("Asset not found"));
            asset.setUsableSize(asset.getUsableSize().add(order.getSize()));
            assetRepository.save(asset);
        }

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }

	private Order getOrder(Long orderId) {
		var order = orderRepository.findById(orderId)
        		.orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only pending orders can be deleted");
        }
		return order;
	}
}
