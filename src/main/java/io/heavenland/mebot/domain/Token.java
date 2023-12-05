package io.heavenland.mebot.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Token {

	WSOL("So11111111111111111111111111111111111111112", 9),
	HTO("htoHLBJV1err8xP5oxyQdV2PLQhtVjxLXpKB7FsgJQD", 9),
	USDC("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v", 6);

	private final String address;
	private final int decimals;

}
