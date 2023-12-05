package io.heavenland.mebot.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class ExecutorUtils {

	private ExecutorUtils() {
	}

	public static FutureWrapper execute(ExecutorService executor, Runnable process) {
		if (process == null) {
			return new FutureWrapper(new HashSet<>());
		}
		return execute(executor, Collections.singleton(process));
	}

	public static FutureWrapper execute(ExecutorService executor, Collection<Runnable> processes) {
		if (processes == null) {
			return new FutureWrapper(new HashSet<>());
		}
		Set<Future<?>> futures = new HashSet<>();
		for (Runnable process : processes) {
			futures.add(executor.submit(process));
		}
		return new FutureWrapper(futures);
	}

}
