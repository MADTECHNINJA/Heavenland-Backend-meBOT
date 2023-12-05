package io.heavenland.mebot.clients.jupiter.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Data
public class JupiterRouteDTO {

	private BigInteger inAmount;
	private BigInteger outAmount;
	private BigInteger amount;
	private BigInteger otherAmountThreshold;
	private BigInteger outAmountWithSlippage;

	private String swapMode;
	private BigDecimal priceImpactPct;

	private List<JupiterMarketInfoDTO> marketInfos;
	private JupiterRouteFees fees;


}
