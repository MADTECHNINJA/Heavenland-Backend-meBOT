package io.heavenland.mebot.clients.jupiter.dto;

import lombok.Data;

@Data
public class JupiterTransactionsDTO {

	private String setupTransaction;
	private String swapTransaction;
	private String cleanupTransaction;

}
