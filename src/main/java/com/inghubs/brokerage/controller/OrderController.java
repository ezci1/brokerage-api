package com.inghubs.brokerage.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inghubs.brokerage.dto.model.Order;
import com.inghubs.brokerage.dto.request.CreateOrderRequest;
import com.inghubs.brokerage.dto.request.GetOrdersRequest;
import com.inghubs.brokerage.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	public void createOrder(@RequestBody CreateOrderRequest request) {
		orderService.createOrder(request);
	}

	@GetMapping
	public List<Order> getOrders(@RequestBody GetOrdersRequest request) {
		return orderService.getOrders(request);
	}

	@DeleteMapping("{orderId}")
	public void deleteOrder(@PathVariable Long orderId) {
		orderService.deleteOrder(orderId);
	}
}
