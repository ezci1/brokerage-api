package com.inghubs.brokerage.dto.request;

import java.math.BigDecimal;

import com.inghubs.brokerage.dto.enumeration.Side;

import lombok.Data;

@Data
public class CreateOrderRequest {
	private Long customerId;
    private Side side;
    private String assetName;
    private BigDecimal size;
    private BigDecimal pricePerShare;
}
