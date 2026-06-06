package com.rauio.smartdangjian.server.user.pojo.dto;

import java.math.BigDecimal;

public record UserSimilaritySummaryDto(Long userId1, Long userId2, BigDecimal similarityScore) {}
