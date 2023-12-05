package io.heavenland.mebot.bots;

import io.heavenland.mebot.IBot;
import io.heavenland.mebot.context.Context;
import io.heavenland.mebot.context.account.AccountService;
import io.heavenland.mebot.context.console.IConsole;
import io.heavenland.mebot.context.engine.EngineService;
import io.heavenland.mebot.context.market_data.MarketDataService;
import io.heavenland.mebot.domain.MarketListing;
import io.heavenland.mebot.domain.NftCollection;
import io.heavenland.mebot.domain.NftMetadata;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class SolamidsBot implements IBot {

	private final SolamidsBotProps props;

	private MarketDataService marketDataService;
	private AccountService accountService;
	private EngineService engineService;
	private IConsole console;

	private final static int C_HL = 90;

	public SolamidsBot(SolamidsBotProps props) {
		this.props = props;
	}

	@Override
	public String getName() {
		return "solamids_bot";
	}

	@Override
	public void onStart(Context context) {
		this.marketDataService = context.getMarketData();
		this.accountService = context.getAccount();
		this.engineService = context.getEngine();
		this.console = context.getConsole();

		accountService.subscribeAccount(props.getAccount());
		marketDataService.subscribeCollection(NftCollection.SOLAMIDS);
	}

	@Override
	public void onTick() throws Exception {
		Set<MarketListing> marketListings = marketDataService.getListings(NftCollection.SOLAMIDS);
		if (CollectionUtils.isEmpty(marketListings)) {
			Thread.sleep(1_000);
			return;
		}

		Set<NftData> datas = new HashSet<>();
		for (MarketListing marketListing : marketListings) {
			NftMetadata meta = marketDataService.getMetadata(marketListing.getTokenMint());
			if (meta == null) {
				Thread.sleep(30_000);
				continue;
			}
			double maxHtoStake = getMaxHtoStake(
					meta.getAttributes().get("Room Size"),
					Integer.parseInt(meta.getAttributes().get("Building Floor"))
			);

			NftData data = new NftData();
			data.setMeta(meta);
			data.setPrice(marketListing.getPrice().doubleValue());
			data.setMaxHtoStake(maxHtoStake);
			datas.add(data);
		}

		List<NftData> bestDeals = datas.stream()
				.sorted(Comparator.comparing(NftData::getHtoStakedPerSol).reversed())
				.limit(10)
				.collect(Collectors.toList());

		System.out.println("_______");
		for (NftData bestDeal : bestDeals) {
			System.out.println("" + bestDeal.getHtoStakedPerSol() + ": " + bestDeal);
		}

		List<String> lines = new ArrayList<>();
		lines.add("price[SOL],maxStake[HTO],stakePerSol[HTO],link");
		for (SolamidsBot.NftData nftData : bestDeals) {
			lines.add(String.format("%.2f,%.0f,%.0f,%s",
					nftData.getPrice(),
					nftData.getMaxHtoStake(),
					nftData.getHtoStakedPerSol(),
					"https://www.magiceden.io/item-details/"+nftData.getMeta().getMintAddress()
			));
		}
		FileUtils.writeLines(new File("best-solamids.csv"), lines);

		Thread.sleep(60_000);
	}

	@Override
	public void onStop() {

	}

	private double getMaxHtoStake(String roomSize, int buildingFloor) {
		double roomSizeBonus = 1.;
		switch (roomSize) {
			case "M":
				roomSizeBonus = 1.2;
				break;
			case "L":
				roomSizeBonus = 1.5;
				break;
			case "XL":
				roomSizeBonus = 2.;
				break;
		}

		double roomsPerFloor = 54 + (21 - buildingFloor) * 18;

		return C_HL * 4000. * roomSizeBonus / (roomsPerFloor * 1.22848);
	}

	@Data
	private final static class NftData {

		private double price;
		private double maxHtoStake;

		private NftMetadata meta;

		public double getHtoStakedPerSol() {
			return maxHtoStake / price;
		}

	}

}
