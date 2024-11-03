package com.inghubs.brokerage.dto.request;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MatchRequest {

    private String league;
    private String homeTeam;
    private String awayTeam;
    
    private LocalDateTime startTime;
}
