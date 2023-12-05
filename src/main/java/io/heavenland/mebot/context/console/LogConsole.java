package io.heavenland.mebot.context.console;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogConsole implements IConsole {

	@Override
	public void infoToTelegramAsync(String msg) {
		log.info(msg);
	}

	@Override
	public void warnToTelegramAsync(String msg) {
		log.warn(msg);
	}

}
