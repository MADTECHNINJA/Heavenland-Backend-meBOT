package io.heavenland.mebot.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountListing {

	private NftCollection collection;
	private String mintAddress;
	private String owner;
	private String name;
	private String updateAuthority;
	private ListingStatus listingStatus;

}
