package com.inghubs.brokerage.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inghubs.brokerage.dto.model.Asset;
import com.inghubs.brokerage.dto.request.DepositMoneyRequest;
import com.inghubs.brokerage.dto.request.WithdrawMoneyRequest;
import com.inghubs.brokerage.service.AssetService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AssetController {

	private final AssetService assetService;

	@PostMapping("deposit")
	public void depositMoney(@RequestBody DepositMoneyRequest request) {
		assetService.depositMoney(request);
	}

	@GetMapping
	public void withdrawMoney(@RequestBody WithdrawMoneyRequest request) {
		assetService.withdrawMoney(request);
	}

	@GetMapping("{customerId}")
	public List<Asset> getCustomerAssets(@PathVariable Long customerId) {
		return assetService.getCustomerAssets(customerId);
	}
}
