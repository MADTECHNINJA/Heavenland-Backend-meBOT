package io.heavenland.mebot.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum NftCollection {

	SOLAMIDS("solamids"),
	HL_PARCELS("heavenland"),
	HL_LOYALTY("heavenlandloyalty"),
	HL_ALPHAS("heavenland_alphas");

	private final String symbol;

}
