package com.inghubs.brokerage.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.inghubs.brokerage.dto.enumeration.OrderStatus;
import com.inghubs.brokerage.dto.enumeration.Side;
import com.inghubs.brokerage.dto.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	Optional<Order> findForUpdateByIdAndStatus(Long id, OrderStatus status);
	
	List<Order> findByCustomerIdAndCreateDateBetween(Long customerId, LocalDate startDate, LocalDate endDate);

	Optional<Order> findTopByAssetNameAndSideAndStatusAndPriceGreaterThanEqualOrderByCreateDateAsc(String asset, Side buy,
			OrderStatus pending, BigDecimal price);

	Optional<Order> findTopByAssetNameAndSideAndStatusAndPriceLessThanEqualOrderByCreateDateAsc(String asset, Side sell,
			OrderStatus pending, BigDecimal price);
}