package com.inghubs.brokerage.dto.request;

import java.time.LocalDate;

import lombok.Data;

@Data
public class GetOrdersRequest {
	private Long customerId;
	private LocalDate startDate;
	private LocalDate endDate;	
}
