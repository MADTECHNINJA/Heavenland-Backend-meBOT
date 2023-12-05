package io.heavenland.mebot.clients.magiceden.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.heavenland.mebot.domain.AccountListing;
import io.heavenland.mebot.domain.ListingStatus;
import io.heavenland.mebot.domain.NftCollection;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MagicEdenWalletTokenDTO {


    public String mintAddress;
    public String owner;
    public String name;
    public String updateAuthority;
    public ListingStatus listStatus;

    public AccountListing toAccountListing(NftCollection collection) {
        return new AccountListing(
                collection,
                mintAddress,
                owner,
                name,
                updateAuthority,
                listStatus
        );
    }
}
