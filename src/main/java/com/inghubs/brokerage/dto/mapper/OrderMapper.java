package com.inghubs.brokerage.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.inghubs.brokerage.dto.model.Order;
import com.inghubs.brokerage.dto.request.CreateOrderRequest;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

	Order mapRequestToOrder(CreateOrderRequest request);
}