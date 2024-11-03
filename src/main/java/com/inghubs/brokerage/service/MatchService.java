package com.inghubs.brokerage.service;

import com.inghubs.brokerage.dto.model.Order;

public interface MatchService {

	void processOrder(Order newOrder);
}
