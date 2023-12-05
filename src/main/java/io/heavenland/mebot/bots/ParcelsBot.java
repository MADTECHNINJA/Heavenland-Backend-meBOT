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
import java.util.*;
import java.util.stream.Collectors;

public class ParcelsBot implements IBot {

	private final ParcelsBotProps props;

	private MarketDataService marketDataService;
	private AccountService accountService;
	private EngineService engineService;
	private IConsole console;

	private final static int C_HL = 90;

	public ParcelsBot(ParcelsBotProps props) {
		this.props = props;
	}

	@Override
	public String getName() {
		return "parcels_bot";
	}

	@Override
	public void onStart(Context context) {
		this.marketDataService = context.getMarketData();
		this.accountService = context.getAccount();
		this.engineService = context.getEngine();
		this.console = context.getConsole();

		accountService.subscribeAccount(props.getAccount());
		marketDataService.subscribeCollection(NftCollection.HL_PARCELS);
	}

	@Override
	public void onTick() throws Exception {
		Set<MarketListing> marketListings = marketDataService.getListings(NftCollection.HL_PARCELS);
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
					Integer.parseInt(meta.getAttributes().get("Max Height [m]")),
					Integer.parseInt(meta.getAttributes().get("Max Depth [m]")),
					meta.getAttributes().get("Drill Through"),
					meta.getAttributes().get("Parcel Count")
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
		for (NftData nftData : bestDeals) {
			lines.add(String.format("%.2f,%.0f,%.0f,%s",
					nftData.getPrice(),
					nftData.getMaxHtoStake(),
					nftData.getHtoStakedPerSol(),
					"https://www.magiceden.io/item-details/"+nftData.getMeta().getMintAddress()
					));
		}
		FileUtils.writeLines(new File("best-parcels.csv"), lines);

		Thread.sleep(60_000);
	}

	@Override
	public void onStop() {

	}

	private double getMaxHtoStake(int maxHeight, int maxDepth, String drillThrough, String parcelCount) {
		int numDrillThrough;
		if (drillThrough == null || drillThrough.equals("No")) {
			numDrillThrough = 0;
		} else if (drillThrough.equals("Yes")) {
			numDrillThrough = 1;
		} else {
			numDrillThrough = Integer.parseInt(drillThrough);
		}

		int numParcels = 1;
		if (parcelCount != null) {
			numParcels = Integer.parseInt(parcelCount);
		}

		return C_HL * (maxHeight + maxDepth) * (numParcels + 0.5 * numDrillThrough);
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
