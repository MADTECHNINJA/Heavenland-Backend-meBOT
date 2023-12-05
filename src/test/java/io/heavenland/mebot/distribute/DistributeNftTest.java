package io.heavenland.mebot.distribute;

import io.heavenland.mebot.utils.ExecutorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

@Slf4j
public class DistributeNftTest {

	private final static String ENV = "mainnet-beta";
	private final static String RPC_URL = "https://weathered-proud-shape.solana-mainnet.quiknode.pro/75114e7c361deae9deac7f76ac11831fcfccd677/";

	@Test
	public void distributeNFTs() throws IOException, ExecutionException, InterruptedException {
		List<String> lines = FileUtils.readLines(new File("distribute-nfts.csv"), StandardCharsets.UTF_8);

		execCmd("solana config set -u " + ENV);
		execCmd("solana config set -u " + RPC_URL);
		execCmd("solana config set --keypair /Users/jlochman/Documents/Solana/keys-new/excomsXY735YmH9Q6KxxXUuyJKAqohWegCNrGQ9b82p.json");
		execCmd("solana config set --commitment processed");

		System.out.println(execCmd("solana config get"));

		Set<Runnable> runnables = new HashSet<>();
		for (String line : lines) {
			String[] split = line.split(",");
			if (split.length != 2) {
				continue;
			}
			runnables.add(() -> {
				try {
					String s = execCmd("spl-token transfer " + split[0] + " 1 " + split[1] + " --fund-recipient --allow-unfunded-recipient");
					System.out.println(s);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		}
		ExecutorUtils.execute(Executors.newFixedThreadPool(8), runnables).blockUntilDone();
	}

	public static String execCmd(String cmd) throws IOException {
		Scanner s = new Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

}
