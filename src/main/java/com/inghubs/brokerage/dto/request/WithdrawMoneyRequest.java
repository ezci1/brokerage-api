package com.inghubs.brokerage.dto.request;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class WithdrawMoneyRequest {
	private Long customerId;
    private BigDecimal amount;
}
