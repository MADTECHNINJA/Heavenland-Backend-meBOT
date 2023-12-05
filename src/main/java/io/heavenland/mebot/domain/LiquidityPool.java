package io.heavenland.mebot.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LiquidityPool {

	RAY_V4_HTO_USDC("AxHMeECpDZx8mXK7hpYA6sAFTSnggFUWCoC4WRFiLQUC", Token.HTO, Token.USDC),
	CROPPER_HTO_USDC("FHesu2QGraemTy99tUKeaAuCTy7Y6v45ywv7VTn37UDi", Token.HTO, Token.USDC);

	private final String address;
	private final Token base;
	private final Token quote;


}
