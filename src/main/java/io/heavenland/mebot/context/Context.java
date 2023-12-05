package io.heavenland.mebot.context;

import io.heavenland.mebot.context.account.AccountService;
import io.heavenland.mebot.context.console.IConsole;
import io.heavenland.mebot.context.console.TelegramConsole;
import io.heavenland.mebot.context.engine.EngineService;
import io.heavenland.mebot.context.market_data.MarketDataService;
import lombok.Getter;

/**
 * Provides access to all what IBot needs
 */
@Getter
public class Context {

	// vsechno na sobe nezavisle singletony
	private final MarketDataService marketData;
	private final AccountService account;
	private final EngineService engine;
	private final IConsole console;

	public Context() {
		this.marketData = new MarketDataService();
		this.account = new AccountService();
		this.engine = new EngineService();
		this.console = new TelegramConsole();
	}

}
