package io.heavenland.mebot.domain;

import io.heavenland.mebot.clients.magiceden.dto.MagicEdenListingDTO;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Wallet {

	private String address;
	private BigDecimal balance;
	private Map<Token, BigDecimal> tokenBalances;
	private Map<NftCollection, Set<AccountListing>> listings;

	public Wallet(String address) {
		this.address = address;
		this.balance = null;
		this.tokenBalances = new HashMap<>();
		this.listings = new HashMap<>();
	}

	public String getAddress() {
		return address;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public Set<Token> getTokens() {
		return tokenBalances.keySet();
	}

	public BigDecimal getTokenBalance(Token token) {
		return tokenBalances.get(token);
	}

	public void setBalance(Token token, BigDecimal balance) {
		this.tokenBalances.put(token, balance);
	}

	public Map<NftCollection, Set<AccountListing>> getListings() {
		return listings;
	}

	public void setListings(Map<NftCollection, Set<AccountListing>> listings) {
		this.listings = listings;
	}
}
