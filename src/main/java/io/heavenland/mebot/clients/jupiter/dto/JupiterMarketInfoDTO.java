package io.heavenland.mebot.clients.jupiter.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class JupiterMarketInfoDTO {

	private String id;
	private String label;
	private String inputMint;
	private String outputMint;
	private boolean notEnoughLiquidity;


	//private BigInteger minInAmount;
	private BigInteger inAmount;
	//private BigInteger minOutAmount;
	private BigInteger outAmount;
	private BigDecimal priceImpactPct;

	private JupiterMarketInfoFeeDTO lpFee;
	private JupiterMarketInfoFeeDTO platformFee;


}
