package io.heavenland.mebot.context.account;

import io.heavenland.mebot.clients.magiceden.MagicEdenClient;
import io.heavenland.mebot.context.collection.CollectionResolver;
import io.heavenland.mebot.domain.*;
import io.heavenland.mebot.context.wallet.WalletFacade;
import jakarta.ejb.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.p2p.solanaj.rpc.RpcException;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
@Slf4j
public class AccountService {

	private final WalletFacade walletFacade;
	private final Map<Account, Wallet> subscribedWallets;

	private boolean backgroundRefreshStarted = false;
	private ScheduledExecutorService executorService;

	public AccountService() {
		walletFacade = new WalletFacade();
		subscribedWallets = new HashMap<>();
	}

	private void refreshBalances() {
		walletFacade.updateBalances(subscribedWallets.values());
	}

	private void refreshListings() {
		// See https://api.magiceden.dev/#11c4283e-7940-4592-b445-f9b512ccf21c

		var client = MagicEdenClient.instance();
		var collectionResolver = CollectionResolver.instance();

		subscribedWallets.values().forEach(wallet -> {
			var magicEdenListings = client.getWalletTokens(wallet.getAddress());
			var listings = new HashMap<NftCollection, Set<AccountListing>>();

			magicEdenListings.forEach(listing -> {
				var collection = collectionResolver.resolveCollection(listing.mintAddress);

				if (collection == null) {
					return;
				}

				var accountListing = listing.toAccountListing(collection);

				if (!listings.containsKey(collection)) {
					listings.put(collection, new HashSet<>());
				}

				listings.get(collection).add(accountListing);
			});

			wallet.setListings(listings);
		});
	}

	public void subscribeAccount(Account account) {
		subscribedWallets.put(account, new Wallet(account.getAddress()));
		log.info("account {}: {} subscribed", account, account.getAddress());
		runUpdateThreads();
	}

	public void subscribeToken(Account account, Token token) {
		if (!subscribedWallets.containsKey(account)) {
			subscribeAccount(account);
		}
		subscribedWallets.get(account).setBalance(token, null);
		log.info("token {} on account {}, subscribed", token, account);
		runUpdateThreads();
	}

	public BigDecimal getSolBalance(Account account) {
		if (!subscribedWallets.containsKey(account)) {
			log.error("account {} not subscribed", account);
			return BigDecimal.ZERO;
		}
		return subscribedWallets.get(account).getBalance();
	}

	public BigDecimal getTokenBalance(Account account, Token token) {
		if (!subscribedWallets.containsKey(account) || !subscribedWallets.get(account).getTokens().contains(token)) {
			log.error("account {} or token {} not subscribed", account, token);
			return BigDecimal.ZERO;
		}
		return subscribedWallets.get(account).getTokenBalance(token);
	}

	public Set<AccountListing> getListings(Account account, NftCollection collection) {
		Set<AccountListing> listings = subscribedWallets.get(account).getListings().get(collection);
		if (CollectionUtils.isEmpty(listings)) {
			return new HashSet<>();
		}
		return new HashSet<>(listings);
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
				log.debug("updating balances");
				refreshBalances();
			}
		}, 1000, 15_000, TimeUnit.MILLISECONDS);

		executorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				log.debug("updating listings");
				refreshListings();
			}
		}, 1000, 15_000, TimeUnit.MILLISECONDS);

		backgroundRefreshStarted = true;
	}
}
