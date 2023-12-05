package io.heavenland.mebot;

import io.heavenland.mebot.context.Context;

public interface IBot {

	String getName();

	void onStart(Context context);

	void onTick() throws Exception;

	void onStop();

}
