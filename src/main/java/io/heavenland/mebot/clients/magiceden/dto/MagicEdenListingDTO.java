package io.heavenland.mebot.clients.magiceden.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.heavenland.mebot.domain.MarketListing;
import io.heavenland.mebot.domain.NftCollection;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MagicEdenListingDTO {

    public String tokenMint;
    public String tokenAddress;

    public BigDecimal price;

    public String seller;

    public String pdaAddress;
    public String auctionHouse;

    public MarketListing toMarketListing(NftCollection collection) {
        return new MarketListing(
                collection,
                pdaAddress,
                auctionHouse,
                tokenAddress,
                tokenMint,
                seller,
                price
        );
    }
}
