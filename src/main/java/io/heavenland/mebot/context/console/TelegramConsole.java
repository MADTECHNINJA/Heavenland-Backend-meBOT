package io.heavenland.mebot.context.console;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TelegramConsole implements IConsole {

	// Mluvici Toustovac in meBot INFO
	private final TelegramSender infoSender
			= new TelegramSender("866747267:AAFq50xTSdOAFOAe_fwPUNSkGrQSyiX1sWs", -690166189L);

	// Mluvici Toustovac in meBot WARN
	private final TelegramSender warnSender
			= new TelegramSender("866747267:AAFq50xTSdOAFOAe_fwPUNSkGrQSyiX1sWs", -765306073L);

	private final ExecutorService executor = Executors.newFixedThreadPool(5);

	@Override
	public void infoToTelegramAsync(String msg) {
		executor.execute(() -> infoSender.sendMessage(msg));
	}

	@Override
	public void warnToTelegramAsync(String msg) {
		executor.execute(() -> warnSender.sendMessage(msg));
	}

}
