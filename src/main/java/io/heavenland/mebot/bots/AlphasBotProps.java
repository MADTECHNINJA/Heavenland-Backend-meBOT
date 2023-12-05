package io.heavenland.mebot.bots;

import io.heavenland.mebot.domain.Account;
import io.heavenland.mebot.domain.NftCollection;
import lombok.Builder;
import lombok.Data;

import java.time.Duration;

@Data
@Builder
public class AlphasBotProps {

	private Account account;
	private NftCollection collection;

	// how much percent above floor price should listing happen
	private double minFloorUpPerc;
	private double maxFloorUpPerc;

	// how much percent of account NFTs should be listed
	private double minPercentNftsListed;

	// what percentage of account value is held in NFTs
	private double nftHoldingsPerc;

	// scale for listing price (scale=1 means 1 decimal)
	@Builder.Default
	private int minPriceScale = 1;
	@Builder.Default
	private int maxPriceScale = 3;

	// millis to sleep after executing transaction
	@Builder.Default
	private long minSleepAfterTradeMillis = Duration.ofSeconds(30L).toMillis();
	@Builder.Default
	private long maxSleepAfterTradeMillis = Duration.ofSeconds(120L).toMillis();


}
