package com.inghubs.brokerage.dto.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.inghubs.brokerage.dto.enumeration.OrderStatus;
import com.inghubs.brokerage.dto.enumeration.Side;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "ORDERS")
public class Order {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long customerId;
	private String assetName;
	private LocalDateTime createDate;

	@Enumerated(EnumType.STRING)
	private Side side;

	@Enumerated(EnumType.STRING)
	private OrderStatus status;
	
	@Column(precision = 15, scale = 2)
	private BigDecimal size;
	
	@Column(precision = 15, scale = 2)
	private BigDecimal price;
	
	@PrePersist
	protected void onCreate() {
	    createDate = LocalDateTime.now();
	}
}