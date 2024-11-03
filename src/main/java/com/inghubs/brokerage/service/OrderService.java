package com.inghubs.brokerage.service;

import java.util.List;

import com.inghubs.brokerage.dto.model.Order;
import com.inghubs.brokerage.dto.request.CreateOrderRequest;
import com.inghubs.brokerage.dto.request.GetOrdersRequest;

public interface OrderService {

	void createOrder(CreateOrderRequest request);

	void deleteOrder(Long id);

	List<Order> getOrders(GetOrdersRequest request);
}