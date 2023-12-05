package io.heavenland.mebot.bots;

import io.heavenland.mebot.domain.Account;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class JupiterBotProps {

	private List<Account> accounts;

	// how much SOL to spend
	private BigDecimal amount;

	// how long to sleep after trade
	private long sleepMillis;

	// scale for listing price (scale=1 means 1 decimal)
	@Builder.Default
	private int minAmountScale = 1;
	@Builder.Default
	private int maxAmountScale = 3;

}
