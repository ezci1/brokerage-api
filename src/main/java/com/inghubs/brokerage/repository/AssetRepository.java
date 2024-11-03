package com.inghubs.brokerage.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.inghubs.brokerage.dto.model.Asset;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

	List<Asset> findByCustomerId(Long customerId);

	Optional<Asset> findByCustomerIdAndAssetName(Long customerId, String assetName);
}