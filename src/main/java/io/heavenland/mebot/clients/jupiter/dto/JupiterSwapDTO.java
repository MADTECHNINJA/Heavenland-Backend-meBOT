package io.heavenland.mebot.clients.jupiter.dto;

import lombok.Data;

@Data
public class JupiterSwapDTO {

	private JupiterRouteDTO route;
	private boolean wrapUnwrapSOL;
	private String feeAccount;
	private String tokenLedger;
	private String userPublicKey;
	private String destinationWallet;

}
