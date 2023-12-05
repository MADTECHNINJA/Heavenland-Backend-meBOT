package io.heavenland.mebot.excom;

import com.fasterxml.jackson.core.type.TypeReference;
import io.heavenland.mebot.utils.ExecutorUtils;
import io.heavenland.mebot.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
public class ExcomRegisterTest {

	private final static String ENV = "mainnet-beta";
	private final static String RPC_URL = "https://weathered-proud-shape.solana-mainnet.quiknode.pro/75114e7c361deae9deac7f76ac11831fcfccd677/";

	@Test
	public void registerNFTs() throws IOException, ExecutionException, InterruptedException {
		Set<String> allMints = JsonUtils.stringToObject(
				FileUtils.readFileToString(
						new File("excoms-mint-list.json"),
						StandardCharsets.UTF_8
				), new TypeReference<>() {
				});
		if (allMints == null) {
			System.exit(1);
		}
		log.info("excom mints: {}", allMints.size());

		execCmd("solana config set -u " + ENV);
		execCmd("solana config set -u " + RPC_URL);
		String keypair = "/Users/jlochman/Desktop/keys-pg/excomkkWmibs1X8uEABcMMau14hVJ4CSzL6J2EeKbcL.json";
		execCmd("solana config set --keypair " + keypair);
		execCmd("solana config set --commitment processed");

		String pubKey = execCmd("solana address --keypair " + keypair).trim();
		log.info("pubKey: {}", pubKey);

		String cliStart = "yarn --cwd /Users/jlochman/Documents/Heavenland/Excoms-Staking-Program/staking ts-node ";
		String cliEnd = " -e " + ENV + " -r " + RPC_URL + " -k " + keypair;

		String cliOut = execCmd(cliStart + "get_all_registers" + cliEnd);

		Set<String> registeredMints = cliOut.lines()
				.filter(line -> line.contains("mint"))
				.map(line -> line.split("\"")[3])
				.collect(Collectors.toSet());
		log.info("registered mints: {}", registeredMints.size());

		Collection<String> unregisteredMints = CollectionUtils.subtract(allMints, registeredMints);
		log.info("unregistered mints: {}", unregisteredMints.size());

		Set<Runnable> runnables = new HashSet<>();
		for (String mint : unregisteredMints) {
			runnables.add(() -> {
				log.info("registering mint: {}", mint);
				try {
					String s = execCmd(cliStart + "register_nft -t 0 -mint " + mint + cliEnd);
					System.out.println(s);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		}
		ExecutorUtils.execute(Executors.newFixedThreadPool(5), runnables).blockUntilDone();

	}


	public static String execCmd(String cmd) throws java.io.IOException {
		java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

}
