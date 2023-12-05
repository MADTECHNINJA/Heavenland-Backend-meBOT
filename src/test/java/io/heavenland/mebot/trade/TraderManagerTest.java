package io.heavenland.mebot.trade;

import io.heavenland.mebot.TraderManager;
import io.heavenland.mebot.bots.*;
import io.heavenland.mebot.context.Context;
import io.heavenland.mebot.domain.Account;
import io.heavenland.mebot.domain.NftCollection;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class TraderManagerTest {

	private final static double NFT_HOLDINGS_PERC = 99.;
	private final TraderManager traderManager = new TraderManager(new Context());

	@Test
	public void testTraderManger() throws InterruptedException {
		//startJupiter();

		startSolamids();
		startParcels();
		// TODO: loyalty
		//startAlphas();

		Thread.sleep(Duration.ofHours(24L).toMillis());
	}

	private void startJupiter() {
		JupiterBotProps props = JupiterBotProps.builder()
				.accounts(List.of(
						Account.JL1
				))
				.amount(BigDecimal.valueOf(0.01))
				.sleepMillis(Duration.ofMinutes(3L).toMillis())
				.minAmountScale(1)
				.maxAmountScale(3)
				.build();
		JupiterBot bot = new JupiterBot(props);
		traderManager.start(bot);
	}

	private void startSolamids() {
		SolamidsBotProps props = SolamidsBotProps.builder()
				.account(Account.TEST)
				.build();
		SolamidsBot bot = new SolamidsBot(props);
		traderManager.start(bot);
	}

	private void startParcels() {
		ParcelsBotProps props = ParcelsBotProps.builder()
				.account(Account.TEST)
				.build();
		ParcelsBot bot = new ParcelsBot(props);
		traderManager.start(bot);
	}

	private void startAlphas() {
		traderManager.start(new AlphasBot(AlphasBotProps.builder()
				.account(Account.JL1)
				.collection(NftCollection.HL_ALPHAS)
				.minFloorUpPerc(30.)
				.maxFloorUpPerc(60.)
				.minPercentNftsListed(60.)
				.nftHoldingsPerc(NFT_HOLDINGS_PERC)
				.build())
		);

		traderManager.start(new AlphasBot(AlphasBotProps.builder()
				.account(Account.JL2)
				.collection(NftCollection.HL_ALPHAS)
				.minFloorUpPerc(25.)
				.maxFloorUpPerc(50.)
				.minPercentNftsListed(70.)
				.nftHoldingsPerc(NFT_HOLDINGS_PERC)
				.build())
		);

		traderManager.start(new AlphasBot(AlphasBotProps.builder()
				.account(Account.JL3)
				.collection(NftCollection.HL_ALPHAS)
				.minFloorUpPerc(20.)
				.maxFloorUpPerc(40.)
				.minPercentNftsListed(80.)
				.nftHoldingsPerc(NFT_HOLDINGS_PERC)
				.build())
		);

		traderManager.start(new AlphasBot(AlphasBotProps.builder()
				.account(Account.JL4)
				.collection(NftCollection.HL_ALPHAS)
				.minFloorUpPerc(15.)
				.maxFloorUpPerc(30.)
				.minPercentNftsListed(90.)
				.nftHoldingsPerc(NFT_HOLDINGS_PERC)
				.build())
		);

		traderManager.start(new AlphasBot(AlphasBotProps.builder()
				.account(Account.JL5)
				.collection(NftCollection.HL_ALPHAS)
				.minFloorUpPerc(10.)
				.maxFloorUpPerc(20.)
				.minPercentNftsListed(100.)
				.nftHoldingsPerc(NFT_HOLDINGS_PERC)
				.build())
		);
	}

}
