package io.heavenland.mebot.clients.jupiter.dto;

import lombok.Data;

import java.math.BigInteger;
import java.util.List;

@Data
public class JupiterRouteFees {

	private BigInteger signatureFee;
	private List<BigInteger> openOrdersDeposits;
	private List<BigInteger> ataDeposits;
	private BigInteger totalFeeAndDeposits;
	private BigInteger minimumSOLForTransaction;

}
