package io.heavenland.mebot.trade;

import io.heavenland.mebot.TraderManager;
import io.heavenland.mebot.bots.AlphasBot;
import io.heavenland.mebot.bots.AlphasBotProps;
import io.heavenland.mebot.context.Context;
import io.heavenland.mebot.domain.Account;
import io.heavenland.mebot.domain.NftCollection;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class TraderManagerTestPG {

	private final TraderManager traderManager = new TraderManager(new Context());

	@Test
	public void testTraderManger() throws InterruptedException {
		startPG1();
		startPG2();
		startPG3();

		Thread.sleep(Duration.ofHours(24L).toMillis());
	}

	private void startPG1() {
		AlphasBotProps props = AlphasBotProps.builder()
				.account(Account.PG1)
				.collection(NftCollection.HL_ALPHAS)
				.minFloorUpPerc(50.)
				.maxFloorUpPerc(100.)
				.minPercentNftsListed(10.)
				.nftHoldingsPerc(80.)
				.build();
		AlphasBot bot = new AlphasBot(props);
		traderManager.start(bot);
	}

	private void startPG2() {
		AlphasBotProps props = AlphasBotProps.builder()
				.account(Account.PG2)
				.collection(NftCollection.HL_ALPHAS)
				.minFloorUpPerc(90.)
				.maxFloorUpPerc(120.)
				.minPercentNftsListed(15.)
				.nftHoldingsPerc(50.)
				.build();
		AlphasBot bot = new AlphasBot(props);
		traderManager.start(bot);
	}

	private void startPG3() {
		AlphasBotProps props = AlphasBotProps.builder()
				.account(Account.PG3)
				.collection(NftCollection.HL_ALPHAS)
				.minFloorUpPerc(80.)
				.maxFloorUpPerc(100.)
				.minPercentNftsListed(10.)
				.nftHoldingsPerc(60.)
				//.minSleepAfterTradeMillis(300_000)
				//.maxSleepAfterTradeMillis(600_000)
				.build();
		AlphasBot bot = new AlphasBot(props);
		traderManager.start(bot);
	}

}
