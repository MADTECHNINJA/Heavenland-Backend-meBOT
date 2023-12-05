package io.heavenland.mebot.context.console;

public interface IConsole {

	void infoToTelegramAsync(String msg);

	void warnToTelegramAsync(String msg);

}
