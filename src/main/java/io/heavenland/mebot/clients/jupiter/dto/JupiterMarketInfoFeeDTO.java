package io.heavenland.mebot.clients.jupiter.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class JupiterMarketInfoFeeDTO {

	private BigDecimal amount;
	private String mint;
	private BigDecimal pct;

}
