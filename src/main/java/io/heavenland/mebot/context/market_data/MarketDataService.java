package io.heavenland.mebot.context.market_data;

import io.heavenland.mebot.Constants;
import io.heavenland.mebot.clients.magiceden.MagicEdenClient;
import io.heavenland.mebot.clients.magiceden.dto.MagicEdenListingDTO;
import io.heavenland.mebot.context.collection.CollectionResolver;
import io.heavenland.mebot.context.wallet.WalletFacade;
import io.heavenland.mebot.domain.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// singleton
// po subscribnuti kolekce automaticky refreshuje kolekci, aby mel aktualni data
// #getListings, #getNftCollection, #getAuctionHouseAddress... uz nevolaji api, pouze poskytuji pristup k datum
@Slf4j
public class MarketDataService {

	private Set<NftCollection> subscribedCollections;
	private Set<LiquidityPool> subscribedPools;

	private Map<NftCollection, Set<MarketListing>> listings;
	private Map<LiquidityPool, LiquidityPoolBalance> lpBalances;

	private NftMetadataService metadataService;

	private boolean backgroundRefreshStarted = false;
	private ScheduledExecutorService executorService;

	public MarketDataService() {
		subscribedCollections = new HashSet<>();
		subscribedPools = new HashSet<>();
		listings = new HashMap<>();
		lpBalances = new HashMap<>();

		metadataService = new NftMetadataService();
	}

	private void refreshCollections() {
		// See https://api.magiceden.dev/#8e6d2f4f-6168-4dbd-a9ae-49e33fbf515e

		var client = MagicEdenClient.instance();

		subscribedCollections.forEach(collection -> {
			var magicEdenListings = client.getListings(collection);
			listings.put(
					collection,
					magicEdenListings.stream().map(l -> l.toMarketListing(collection)).collect(Collectors.toSet())
			);
		});
	}

	private void refreshLps() {
		WalletFacade walletFacade = new WalletFacade();
		for (LiquidityPool lp : subscribedPools) {
			LiquidityPoolBalance lpBalance = new LiquidityPoolBalance(lp);
			lpBalance.setBaseBalance(walletFacade.getTokenBalance(lp.getAddress(), lp.getBase(), Constants.CLUSTER));
			lpBalance.setQuoteBalance(walletFacade.getTokenBalance(lp.getAddress(), lp.getQuote(), Constants.CLUSTER));
			lpBalances.put(lp, lpBalance);
		}
	}

	public void subscribeCollection(NftCollection collection) {
		subscribedCollections.add(collection);
		log.info("collection {} subscribed", collection);
		runUpdateThreads();
	}

	public void subscribeLiquidityPool(LiquidityPool liquidityPool) {
		subscribedPools.add(liquidityPool);
		log.info("lp {} subscribed", liquidityPool);
		runUpdateThreads();
	}

	public Set<MarketListing> getListings(NftCollection collection) {
		return listings.get(collection);
	}

	public LiquidityPoolBalance getLpBalance(LiquidityPool lp) {
		return LiquidityPoolBalance.copyOf(lpBalances.get(lp));
	}

	public NftCollection getNftCollection(String mintAddress) {
		return CollectionResolver.instance().resolveCollection(mintAddress);
	}

	public String getAuctionHouseAddress(NftCollection collection) {
		return getListings(collection).stream().findFirst().map(MarketListing::getAuctionHouse).orElse(null);
	}

	public NftMetadata getMetadata(String nftMint) {
		return metadataService.getMetadata(nftMint);
	}

	//

	private void runUpdateThreads() {
		if (backgroundRefreshStarted) {
			return;
		}

		executorService = Executors.newScheduledThreadPool(2);
		executorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				log.debug("updating collections");
				refreshCollections();
			}
		}, 1, 15, TimeUnit.SECONDS);

		executorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				log.debug("updating lps");
				refreshLps();
			}
		}, 1, 15, TimeUnit.SECONDS);

		backgroundRefreshStarted = true;
	}
}
