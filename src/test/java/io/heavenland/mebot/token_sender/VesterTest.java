package io.heavenland.mebot.token_sender;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VesterTest {

	private final static Random rand = new Random();
	private final static double doggoPerVesting = 400e9;
	private final static int maxHoursVesting = 12;

	@Test
	public void test() throws IOException {
		List<String> lines = getLines(3e12, "GjE9WEWH6hmeDBAHDcaaGxSRkA5CiqufK6fwZ7p6ezGk");
		FileUtils.writeLines(new File("/Users/jlochman/Documents/Heavenland/StreamFlow-CLI/template/script.sh"), lines);
	}

	private List<String> getLines(double sum, String receiver) {
		Set<Double> amounts = getRandoms(sum);

		List<String> lines = new ArrayList<>();
		lines.add("#!/bin/bash");
		lines.add("");
		for (double amount : amounts) {
			lines.add(
					"yarn ts-node create_stream -re "
							+ receiver
							+ " -da "
							+ String.format("%.0f", amount)
							+ " -n 'fake' -s 1676458800 -h "
							+ rand.nextInt(maxHoursVesting)
							+ " -e mainnet-beta "
							+ "-r https://weathered-proud-shape.solana-mainnet.quiknode.pro/75114e7c361deae9deac7f76ac11831fcfccd677/ "
							+ "-k /Users/jlochman/Documents/Solana/keys-doggo/VestUyfwj3pGZX6ytcvBvkrzEfsMYghe9HyZehVVFrU.json"

			);
		}
		return lines;
	}

	private Set<Double> getRandoms(double sum) {
		Set<Double> values = IntStream.range(0, (int) (sum / doggoPerVesting)).mapToDouble(i -> 0.5 + 0.5 * Math.abs(rand.nextGaussian())).boxed().collect(Collectors.toSet());
		double randomSum = values.stream().mapToDouble(Double::doubleValue).sum();
		return values.stream().map(x -> x * sum / randomSum).collect(Collectors.toSet());
	}

}
