package io.heavenland.mebot.context.console;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TelegramSender {

	private final String botToken;
	private final Long chatID;

	private final static int NUM_TRIES = 5;
	private final static int MAX_TELEGRAM_MSG_LENGTH = 4096;
	private final static Executor executor = Executors.newSingleThreadExecutor();

	public static OkHttpClient client;

	static {
		Dispatcher publicDispatcher = new Dispatcher();
		publicDispatcher.setMaxRequestsPerHost(100);
		publicDispatcher.setMaxRequests(1000);
		client = new OkHttpClient.Builder()
				.dispatcher(publicDispatcher)
				.connectTimeout(500, TimeUnit.MILLISECONDS)
				.readTimeout(10, TimeUnit.SECONDS)
				.writeTimeout(10, TimeUnit.SECONDS)
				.pingInterval(20, TimeUnit.SECONDS)
				.build();
	}

	public TelegramSender(String botToken, Long chatID) {
		this.botToken = botToken;
		this.chatID = chatID;
	}

	public void sendMessage(final String msg) {
		if (ObjectUtils.isEmpty(msg)) {
			log.warn("trying to send empty message to Telegram");
			return;
		}
		if (msg.length() > MAX_TELEGRAM_MSG_LENGTH) {
			log.warn("message exceeds max allowed length: {}, {}", MAX_TELEGRAM_MSG_LENGTH, msg);
			return;
		}

		executor.execute(() -> {
			Request request = new Request.Builder().url("https://api.telegram.org/bot"
					+ botToken + "/sendMessage?chat_id="
					+ chatID + "&parse_mode=HTML&text=" + URLEncoder.encode(msg, StandardCharsets.UTF_8)).build();
			Response response = executeRequest(request, NUM_TRIES);
			if (response == null) {
				return;
			}
			if (!response.isSuccessful()) {
				log.error("response [{}] not successful when sending {}", response, msg);
			}
			if (response.body() != null) {
				response.body().close();
			}
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
	}

	private Response executeRequest(Request request, int numTries) {
		try {
			return client.newCall(request).execute();
		} catch (IOException e) {
			log.warn("error when sending {}", request.body(), e);
			if (numTries <= 0) {
				return null;
			}
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			return executeRequest(request, numTries - 1);
		}
	}

}
