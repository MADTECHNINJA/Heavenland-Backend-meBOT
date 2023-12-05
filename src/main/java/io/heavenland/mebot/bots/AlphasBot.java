package io.heavenland.mebot.bots;

import io.heavenland.mebot.IBot;
import io.heavenland.mebot.context.Context;
import io.heavenland.mebot.context.account.AccountService;
import io.heavenland.mebot.context.console.IConsole;
import io.heavenland.mebot.context.engine.EngineService;
import io.heavenland.mebot.context.market_data.MarketDataService;
import io.heavenland.mebot.domain.AccountListing;
import io.heavenland.mebot.domain.ListingStatus;
import io.heavenland.mebot.domain.MarketListing;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class AlphasBot implements IBot {

	private final AlphasBotProps props;
	private final Random rand = new Random();

	private static final int NUM_CHEAPEST_LISTINGS = 3;
	private static final BigDecimal MAX_SOL_FEE = BigDecimal.valueOf(0.01);
	private static final Set<String> DONT_BUY_FROM = new HashSet<>();

	private MarketDataService marketDataService;
	private AccountService accountService;
	private EngineService engineService;
	private IConsole console;

	private boolean warningSent = false;

	static {
		DONT_BUY_FROM.add("jaxXDX1oyG12GuzNAgh32bBeUP5qMMmDCcuz9xi1qf3");
		DONT_BUY_FROM.add("tyE4Jor2vHHmdGpCQJ4ZGLHPd5fPfdcV3WzVbqsH6qy");
		DONT_BUY_FROM.add("onVqx452urx6qPJSScb6k3b5BEvYo5bipgJN8ZV1qyt");
		DONT_BUY_FROM.add("ona5HDMkGo3pAPFVXkt54J2YkiubWNE6fDaaJh6k3Te");
		DONT_BUY_FROM.add("ononzSXGv1e7SxJAr3DpUFk5UQ28y9VZv4Li4RE9rgs");
		DONT_BUY_FROM.add("bcNQSdM7zButrYn37TWLn5hoRBS3z6kEZnjXtVwHEBb");
		DONT_BUY_FROM.add("mQQ6464bPvsHcZDgFHwfAV6pHxsfuHAVzUwLtuZ6Hxw");
		DONT_BUY_FROM.add("qXJi4nzjyn7kyjL64TPKWjZS9SffiAzQrwd7U5P4KFp");
		DONT_BUY_FROM.add("s915EmP6nGzZnhh8XwpJ7iLpgsdZixdbUQPw3WP6WtT");
		DONT_BUY_FROM.add("XryaRBbaCTU2DZx7vxA4xKzsoYRmS6fyyQSVMJH3aVH");
	}

	public AlphasBot(AlphasBotProps props) {
		this.props = props;
	}

	@Override
	public String getName() {
		return "floor-up";
	}

	@Override
	public void onStart(Context context) {
		this.marketDataService = context.getMarketData();
		this.accountService = context.getAccount();
		this.engineService = context.getEngine();
		this.console = context.getConsole();

		accountService.subscribeAccount(props.getAccount());
		marketDataService.subscribeCollection(props.getCollection());
	}

	@Override
	public void onTick() throws Exception {
		BigDecimal solBalance = accountService.getSolBalance(props.getAccount());
		Set<AccountListing> accountListings = accountService.getListings(props.getAccount(), props.getCollection());
		Set<MarketListing> marketListings = marketDataService.getListings(props.getCollection());

		// check all data are loaded
		if (solBalance == null || accountListings == null || CollectionUtils.isEmpty(marketListings)) {
			Thread.sleep(1_000L);
			return;
		}

		List<MarketListing> cheapestListings = marketListings.stream()
				.filter(Objects::nonNull)
				.filter(ml -> ml.getPrice() != null)
				.filter(ml -> !DONT_BUY_FROM.contains(ml.getSeller()))
				.sorted(Comparator.comparing(MarketListing::getPrice))
				.limit(NUM_CHEAPEST_LISTINGS).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(cheapestListings)) {
			Thread.sleep(1_000);
			return;
		}
		MarketListing cheapestListing = cheapestListings.get(rand.nextInt(cheapestListings.size()));
		BigDecimal floorPrice = cheapestListing.getPrice();

		// check if there is some unlisted token
		List<AccountListing> unlisted = accountListings.stream()
				.filter(listing -> listing.getListingStatus() == ListingStatus.UNLISTED)
				.collect(Collectors.toList());
		List<AccountListing> listed = accountListings.stream()
				.filter(listing -> listing.getListingStatus() == ListingStatus.LISTED)
				.collect(Collectors.toList());
		if (unlisted.size() > 0
				&& 100. * listed.size() / (unlisted.size() + listed.size()) < props.getMinPercentNftsListed()) {
			// check there is enough sol balance
			if (solBalance.compareTo(MAX_SOL_FEE) < 0) {
				sendWarning("not enough SOL to pay basic listing fee: " + props.getAccount() + ", " + props.getCollection());
				Thread.sleep(1_000);
				return;
			}
			BigDecimal listingPrice = getListingPrice(floorPrice, getRandBetween(props.getMinPriceScale(), props.getMaxPriceScale()));
			String ahAddress = marketDataService.getAuctionHouseAddress(props.getCollection());
			boolean success = engineService.sell(props.getAccount(), unlisted.get(0), ahAddress, listingPrice);
			if (success) {
				console.infoToTelegramAsync(
						String.format(
								"listing %s for %.3f SOL on %s [%s]",
								unlisted.get(0).getName(), listingPrice, props.getAccount(),
								props.getAccount().getAddress().substring(0, 5) + "..."
						)
				);
			} else {
				log.error("problem when listing {} for {} SOL on {}", unlisted.get(0), listingPrice, props.getAccount());
			}
			Thread.sleep(getRandBetween(props.getMinSleepAfterTradeMillis(), props.getMaxSleepAfterTradeMillis()));
			return;
		}

		// buying floor
		int accountNfts = unlisted.size() + listed.size();
		BigDecimal listedSolBalance = floorPrice.multiply(BigDecimal.valueOf(accountNfts));
		BigDecimal totalSolBalance = solBalance.add(listedSolBalance);
		double percentageListed = 100 * listedSolBalance.doubleValue() / totalSolBalance.doubleValue();
		if (percentageListed < props.getNftHoldingsPerc() && solBalance.compareTo(floorPrice.add(MAX_SOL_FEE)) > 0) {
			// check there is enough sol balance
			if (solBalance.compareTo(floorPrice.add(MAX_SOL_FEE)) < 0) {
				sendWarning("not enough SOL to buy floor: " + props.getAccount() + ", " + props.getCollection());
				Thread.sleep(1_000);
			}
			boolean success = engineService.buyNow(props.getAccount(), cheapestListing);
			if (success) {
				console.infoToTelegramAsync(
						String.format(
								"buying %s for %.3f SOL on %s [%s]",
								cheapestListing.getCollection(), cheapestListing.getPrice(), props.getAccount(),
								props.getAccount().getAddress().substring(0, 5) + "..."
						)
				);
			} else {
				log.error("problem when buying {} on {}", cheapestListing, props.getAccount());
			}
			Thread.sleep(getRandBetween(props.getMinSleepAfterTradeMillis(), props.getMaxSleepAfterTradeMillis()));
			return;
		}

		Thread.sleep(1_000);
	}

	private BigDecimal getListingPrice(BigDecimal floorPrice, int scale) {
		double floorUpPerc = getRandBetween(props.getMinFloorUpPerc(), props.getMaxFloorUpPerc());
		BigDecimal listingPrice = floorPrice.multiply(BigDecimal.valueOf(100 + floorUpPerc).divide(BigDecimal.valueOf(100), RoundingMode.FLOOR));
		listingPrice = listingPrice.setScale(scale, RoundingMode.HALF_UP);
		return listingPrice;
	}

	@Override
	public void onStop() {

	}

	private void sendWarning(String msg) {
		if (!warningSent) {
			console.warnToTelegramAsync(msg);
			warningSent = true;
		}
	}

	private double getRandBetween(double minIncl, double maxIncl) {
		return minIncl + (maxIncl - minIncl) * rand.nextDouble();
	}

	private int getRandBetween(int minIncl, int maxIncl) {
		return minIncl + rand.nextInt(maxIncl - minIncl + 1);
	}

	private long getRandBetween(long minIncl, long maxIncl) {
		return minIncl + rand.nextLong(maxIncl - minIncl + 1);
	}

}
