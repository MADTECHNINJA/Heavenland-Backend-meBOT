package io.heavenland.mebot.bots;

import io.heavenland.mebot.IBot;
import io.heavenland.mebot.context.Context;
import io.heavenland.mebot.context.account.AccountService;
import io.heavenland.mebot.context.console.IConsole;
import io.heavenland.mebot.context.engine.EngineService;
import io.heavenland.mebot.context.market_data.MarketDataService;
import io.heavenland.mebot.domain.Account;
import io.heavenland.mebot.domain.LiquidityPool;
import io.heavenland.mebot.domain.LiquidityPoolBalance;
import io.heavenland.mebot.domain.Token;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Random;

@Slf4j
public class JupiterBot implements IBot {

	private final JupiterBotProps props;
	private final Random rand = new Random(Instant.now().toEpochMilli());

	private MarketDataService marketDataService;
	private AccountService accountService;
	private EngineService engineService;
	private IConsole console;

	private static final BigDecimal MAX_SOL_FEE = BigDecimal.valueOf(0.001);
	private static final BigDecimal SLIPPAGE = BigDecimal.valueOf(1.);
	private static final boolean ONLY_DIRECT_ROUTES = false;
	private static final boolean WRAP_UNWRAP_SOL = true;
	private static final double RAND_FACTOR = 10.;

	public JupiterBot(JupiterBotProps props) {
		this.props = props;
	}

	@Override
	public String getName() {
		return "jupiter";
	}

	@Override
	public void onStart(Context context) {
		this.marketDataService = context.getMarketData();
		this.accountService = context.getAccount();
		this.engineService = context.getEngine();
		this.console = context.getConsole();

		marketDataService.subscribeLiquidityPool(LiquidityPool.RAY_V4_HTO_USDC);

		for (Account account : props.getAccounts()) {
			accountService.subscribeAccount(account);
			accountService.subscribeToken(account, Token.HTO);
		}
	}

	@Override
	public void onTick() throws Exception {
		BigDecimal htoPrice = getHtoPriceInUsdc();

		Account account = props.getAccounts().get(rand.nextInt(props.getAccounts().size()));
		BigDecimal solBalance = accountService.getSolBalance(account);
		BigDecimal htoBalance = accountService.getTokenBalance(account, Token.HTO);

		if (htoPrice.compareTo(BigDecimal.ZERO) == 0 || solBalance == null || solBalance.compareTo(MAX_SOL_FEE) < 0) {
			Thread.sleep(1_000);
			return;
		}

		// mean value of abs(gauss(0,1)) ~ 0.8
		BigDecimal amount = props.getAmount().multiply(BigDecimal.valueOf(1 / RAND_FACTOR + (RAND_FACTOR - 1) / RAND_FACTOR / 0.8 * Math.abs(rand.nextGaussian())));
		amount = amount.setScale(getRandBetween(props.getMinAmountScale(), props.getMaxAmountScale()), RoundingMode.HALF_UP);
		if (amount.compareTo(BigDecimal.ZERO) == 0 || solBalance.compareTo(amount) < 0) {
			Thread.sleep(1_000);
			return;
		}
		log.info("trying to swap on {}", account);

		boolean success = false;
		try {
			success = engineService.swap(
					account, Token.WSOL, Token.HTO,
					amount,
					SLIPPAGE, ONLY_DIRECT_ROUTES, WRAP_UNWRAP_SOL
			);
		} catch (ArrayIndexOutOfBoundsException e) {
			log.warn("Problem decoding transaction", e);
		}
		if (success) {
			console.infoToTelegramAsync(String.format(
					"all: %,.2f SOL, %,.0f HTO" + System.lineSeparator() +
							"%s [%s]: %,.2f SOL, %,.0f HTO" + System.lineSeparator() +
							"swap %." + props.getMaxAmountScale() + "f SOL -> HTO",
					getTotalSolBalance(), getTotalTokenBalance(Token.HTO),
					account, account.getAddress().substring(0, 5) + "...",
					solBalance, htoBalance,
					amount
			));
			long sleepMillis = (long) (props.getSleepMillis() * (1 / RAND_FACTOR + (RAND_FACTOR - 1) / RAND_FACTOR / 0.8 * Math.abs(rand.nextGaussian())));
			log.info("sleeping for {} s", sleepMillis / 1000);
			Thread.sleep(sleepMillis);
		} else {
			log.info("problem");
			Thread.sleep(10_000);
		}

	}

	private BigDecimal getTotalSolBalance() {
		BigDecimal balance = BigDecimal.ZERO;
		for (Account account : props.getAccounts()) {
			BigDecimal accountBalance = accountService.getSolBalance(account);
			if (accountBalance != null) {
				balance = balance.add(accountService.getSolBalance(account));
			}
		}
		return balance;
	}

	private BigDecimal getTotalTokenBalance(Token token) {
		BigDecimal balance = BigDecimal.ZERO;
		for (Account account : props.getAccounts()) {
			BigDecimal tokenBalance = accountService.getTokenBalance(account, token);
			if (tokenBalance != null) {
				balance = balance.add(tokenBalance);
			}
		}
		return balance;
	}

	private BigDecimal getHtoPriceInUsdc() {
		LiquidityPoolBalance lpBalance = marketDataService.getLpBalance(LiquidityPool.RAY_V4_HTO_USDC);
		if (lpBalance == null) {
			return BigDecimal.ZERO;
		}
		BigDecimal htoInPool = lpBalance.getTokenBalance(Token.HTO);
		BigDecimal usdcInPool = lpBalance.getTokenBalance(Token.USDC);
		if (htoInPool == null || usdcInPool == null) {
			return BigDecimal.ZERO;
		}
		if (htoInPool.multiply(usdcInPool).compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}
		return usdcInPool.divide(htoInPool, RoundingMode.DOWN);
	}

	private int getRandBetween(int minIncl, int maxIncl) {
		return minIncl + rand.nextInt(maxIncl - minIncl + 1);
	}

	@Override
	public void onStop() {

	}

}
