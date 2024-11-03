package com.inghubs.brokerage.service;

import java.util.List;

import com.inghubs.brokerage.dto.model.Asset;
import com.inghubs.brokerage.dto.request.DepositMoneyRequest;
import com.inghubs.brokerage.dto.request.WithdrawMoneyRequest;

public interface AssetService {

	void depositMoney(DepositMoneyRequest request);

	void withdrawMoney(WithdrawMoneyRequest request);

	List<Asset> getCustomerAssets(Long customerId);
}