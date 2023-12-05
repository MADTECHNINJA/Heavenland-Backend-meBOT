package io.heavenland.mebot;

import io.heavenland.mebot.context.Context;
import io.heavenland.mebot.utils.ExecutorUtils;
import jakarta.annotation.PreDestroy;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

// singleton
@Slf4j
public class TraderManager {

	// singleton
	private final Context context;

	private final AtomicInteger botIdCounter = new AtomicInteger();
	private final Map<Long, BotThread> botThreads = new HashMap<>();

	private final static long WAIT_FOR_DATA_MILLIS = Duration.ofSeconds(10).toMillis();

	public TraderManager(Context context) {
		this.context = context;
	}

	@PreDestroy
	private void preDestroy() {
		Set<Runnable> runnables = new HashSet<>();
		synchronized (botThreads) {
			for (Long botID : botThreads.keySet()) {
				runnables.add(() -> {
					try {
						stop(botID);
					} catch (InterruptedException e) {
						log.warn("interrupted");
						Thread.currentThread().interrupt();
					}
				});
			}
		}

		final ExecutorService executorService = Executors.newFixedThreadPool(10);
		try {
			ExecutorUtils.execute(executorService, runnables).blockUntilDone();
		} catch (InterruptedException | ExecutionException e) {
			log.error("error when stopping bots", e);
		}
	}

	public synchronized long start(@NotNull IBot bot) {
		long botID = botIdCounter.getAndIncrement();
		bot.onStart(context);
		BotThread thread = new BotThread(bot, botID);
		thread.start();
		botThreads.put(botID, thread);
		return botID;
	}

	public void stop(long botID) throws InterruptedException {
		BotThread thread = botThreads.get(botID);
		if (thread != null) {
			String threadName = thread.getName();
			thread.stopThread();

			long startMillis = Instant.now().toEpochMilli();
			thread.join();
			botThreads.remove(botID);
			log.info("bot thread {} stopped in {}ms", threadName, Instant.now().toEpochMilli() - startMillis);
		}
	}

	private static class BotThread extends Thread {

		private final IBot bot;
		private final AtomicLong tickCounter = new AtomicLong();
		private volatile boolean stopped = false;

		public BotThread(IBot bot, long botId) {
			super(bot.getName() + "-trader-" + botId);
			this.bot = bot;
		}

		@Override
		public void run() {
			while (!stopped) {
				try {
					if (tickCounter.getAndIncrement() == 0) {
						Thread.sleep(WAIT_FOR_DATA_MILLIS);
					}
					bot.onTick();
				} catch (MeBotException e) {
					stopThread();
					log.error("error during tick", e);
				} catch (InterruptedException e) {
					stopBot();
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					stopThread();
					log.error("unknown exception during tick", e);
				}
			}
			stopBot();
		}

		private void stopBot() {
			bot.onStop();
		}

		public void stopThread() {
			this.stopped = true;
		}


	}

}
