package com.inghubs.brokerage.dto.model;

import java.time.LocalDateTime;

import com.inghubs.brokerage.dto.enumeration.Side;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "TRANSACTIONS")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerId;
    private Side side;
    private double amount;
    private LocalDateTime transactionDate = LocalDateTime.now();

}