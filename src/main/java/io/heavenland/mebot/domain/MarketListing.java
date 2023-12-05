package io.heavenland.mebot.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MarketListing {

	private NftCollection collection;
	private String pdaAddress;
	private String auctionHouse;
	private String tokenAddress;
	private String tokenMint;
	private String seller;

	private BigDecimal price;

}
