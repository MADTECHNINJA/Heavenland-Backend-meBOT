package io.heavenland.mebot.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FutureWrapper {

	private final Collection<Future<?>> futures;

	public FutureWrapper() {
		this(null);
	}

	public FutureWrapper(Collection<Future<?>> futures) {
		if (futures == null) {
			this.futures = new HashSet<>();
		} else {
			this.futures = new HashSet<>(futures);
		}
	}

	public static FutureWrapper empty() {
		return new FutureWrapper(null);
	}

	public void blockUntilDone() throws InterruptedException, ExecutionException {
		for (Future<?> f : futures) {
			if (f != null) {
				f.get();
			}
		}
	}

	public boolean isDone() {
		for (Future<?> f : futures) {
			if (f != null && !f.isDone()) {
				return false;
			}
		}
		return true;
	}

}
