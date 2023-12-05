package io.heavenland.mebot.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Objects;

@Data
@Slf4j
public class LiquidityPoolBalance {

	private final LiquidityPool lp;
	private BigDecimal baseBalance;
	private BigDecimal quoteBalance;

	public LiquidityPoolBalance(LiquidityPool lp) {
		this.lp = lp;
	}

	public static LiquidityPoolBalance copyOf(LiquidityPoolBalance lpBalance) {
		if (lpBalance == null) {
			return null;
		}
		LiquidityPoolBalance copy = new LiquidityPoolBalance(lpBalance.getLp());
		copy.setBaseBalance(lpBalance.getBaseBalance());
		copy.setQuoteBalance(lpBalance.getQuoteBalance());
		return copy;
	}

	public void setTokenBalance(Token token, BigDecimal balance) {
		if (token == lp.getBase()) {
			baseBalance = balance;
		} else if (token == lp.getQuote()) {
			quoteBalance = balance;
		} else {
			log.warn("token {} is not in lp {}", token, lp);
		}
	}

	public BigDecimal getTokenBalance(Token token) {
		if (token == lp.getBase()) {
			return baseBalance;
		}
		if (token == lp.getQuote()) {
			return quoteBalance;
		}
		return BigDecimal.ZERO;
	}

}
