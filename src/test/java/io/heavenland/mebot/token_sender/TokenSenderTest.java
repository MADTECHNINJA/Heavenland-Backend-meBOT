package io.heavenland.mebot.token_sender;

import com.fasterxml.jackson.core.type.TypeReference;
import io.heavenland.mebot.utils.ExecutorUtils;
import io.heavenland.mebot.utils.JsonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class TokenSenderTest {

	private final static String ENV = "mainnet-beta";
	private final static String RPC_URL = "https://weathered-proud-shape.solana-mainnet.quiknode.pro/75114e7c361deae9deac7f76ac11831fcfccd677/";
	//private final static String KEYPAIR = "/Users/jlochman/Documents/Solana/keys-doggo/7wAMQpYJJtGGg7cWGECZovbZKMLaB71qdDDTbH839AP6.json";
	private final static String KEYPAIR = "/Users/jlochman/Documents/Solana/keys/Vik6ZpwL2NoSfp7ypRQ2W4kSRqAmhGCHyUZD7ePGDYh.json";
	//private final static String KEYPAIR = "/Users/jlochman/Documents/Solana/keys-new/Give1i75LsJ4LeQbUs3daV4EE1SL4X47Nc1c6Gm3atnz.json";

	private final static String CLI_START = "yarn --cwd /Users/jlochman/Documents/Solana/Paragon-mainnet/Paragon-Program/paragon ts-node ";
	private final static String CLI_END = " -e " + ENV + " -r " + RPC_URL + " -k " + KEYPAIR;

	private final static int NUM_THREADS = 10;
	private final static int MAX_TRANSFERS_IN_TX = 1;

	private final static List<Transfer> toFix = new ArrayList<>();
	private final static AtomicInteger counter = new AtomicInteger();
	private final static AtomicInteger successCounter = new AtomicInteger();
	private final static AtomicInteger failCounter = new AtomicInteger();
	private static int total;

	@Test
	public void test() throws IOException, ExecutionException, InterruptedException {
		//Set<Runnable> runnables = getRunnables("hl.csv", 24.3e9, "Doggoyb1uHFJGFdHhJf8FKEBUMv58qo98CisWgeD7Ftk", 5);
		//Set<Runnable> runnables = getRunnables("sol.csv", 15.23);
		Set<Runnable> runnables = getRunnables("fix.csv", 0);
		//Set<Runnable> runnables = getRunnables("hto.csv", 0, "htoHLBJV1err8xP5oxyQdV2PLQhtVjxLXpKB7FsgJQD", 9);
		//Set<Runnable> runnables = getRunnables("cope_cets.csv", 6968 * 5e6, "Doggoyb1uHFJGFdHhJf8FKEBUMv58qo98CisWgeD7Ftk", 5);
		//Set<Runnable> runnables = getRunnables("fix.csv", 0, "Doggoyb1uHFJGFdHhJf8FKEBUMv58qo98CisWgeD7Ftk", 5);
		total = runnables.size();
		ExecutorUtils.execute(Executors.newFixedThreadPool(NUM_THREADS), runnables).blockUntilDone();
		if (!toFix.isEmpty()) {
			List<String> lines = toFix.stream().map(t -> t.getRecipient() + "," + t.getAmount()).toList();
			FileUtils.writeLines(new File("doggo" + File.separator + "fix.csv"), lines);

			lines = toFix.stream().map(t -> "spl-token transfer Doggoyb1uHFJGFdHhJf8FKEBUMv58qo98CisWgeD7Ftk "
					+ t.getAmount() + " " + t.getRecipient() + " --fund-recipient").toList();
			FileUtils.writeLines(new File("doggo" + File.separator + "fix-cli.txt"), lines);
		}
		log.info("success: {}, fail: {}", successCounter.get(), failCounter.get());
	}

	public Set<Runnable> getRunnables(String fileName, double totalAmount) throws IOException, ExecutionException, InterruptedException {
		return getRunnables(fileName, totalAmount, s -> true, null, null);
	}

	public Set<Runnable> getRunnables(String fileName, double totalAmount, String mint, int mintDecimals) throws IOException, ExecutionException, InterruptedException {
		return getRunnables(fileName, totalAmount, s -> true, mint, mintDecimals);
	}

	public Set<Runnable> getRunnables(String fileName, double totalAmount, Predicate<String> recipientPredicate, String mint, Integer mintDecimals) throws IOException, ExecutionException, InterruptedException {
		File f = new File("doggo" + File.separator + fileName);
		List<String> lines = new ArrayList<>();
		if (fileName.endsWith(".csv")) {
			lines = FileUtils.readLines(f, StandardCharsets.UTF_8);
		} else if (fileName.endsWith(".json")) {
			lines = JsonUtils.stringToObject(FileUtils.readFileToString(f, StandardCharsets.UTF_8), new TypeReference<>() {
			});
		}
		if (CollectionUtils.isEmpty(lines)) {
			log.error("empty lines");
			System.exit(1);
		}
		Map<String, Transfer> transferMap = new HashMap<>();
		for (String line : lines) {
			String[] s = line.split(",");
			String recipient = s[0];
			if (!recipientPredicate.evaluate(recipient)) {
				continue;
			}
			if (!transferMap.containsKey(recipient)) {
				Transfer transfer = new Transfer();
				transfer.setMint(mint);
				transfer.setMintDecimals(mintDecimals);
				transfer.setWeight(s.length == 1 ? 1 : Double.parseDouble(s[1]));
				transfer.setRecipient(s[0]);
				transferMap.put(recipient, transfer);
			} else {
				Transfer transfer = transferMap.get(recipient);
				transfer.setWeight(transfer.getWeight() + (s.length == 1 ? 1 : Double.parseDouble(s[1])));
			}
		}
		List<Transfer> transfers = new ArrayList<>(transferMap.values());
		if (totalAmount == 0) {
			transfers.forEach(t -> t.setAmount(t.getWeight()));
		} else {
			final double totalWeight = transfers.stream().mapToDouble(Transfer::getWeight).sum();
			transfers.forEach(t -> t.setAmount(totalAmount * t.getWeight() / totalWeight));
		}
		log.info("totalAmount: {}, transfers: {}", transfers.stream().mapToDouble(Transfer::getAmount).sum(), transfers.size());

		// cleanup
		Set<Runnable> runnables = new HashSet<>();
		if (mint != null) {
			AtomicInteger systemAccountsWithSol = new AtomicInteger();
			AtomicInteger systemAccountsWithoutSol = new AtomicInteger();
			AtomicInteger nonSystemAccounts = new AtomicInteger();
			AtomicInteger counter = new AtomicInteger();
			for (Transfer transfer : transfers) {
				runnables.add(() -> {
					try {
						String s = execCmd(CLI_START + "get_account_info -a " + transfer.getRecipient() + CLI_END);
						String[] commandLines = s.split(System.lineSeparator());
						boolean systemAccount = true;
						long lamports = 0;
						for (int i = 0; i < commandLines.length; i++) {
							if (commandLines[i].contains("owner: ") && !commandLines[i].contains("11111111111111111111111111111111")) {
								systemAccount = false;
								break;
							}
							if (commandLines[i].contains("balance: ")) {
								lamports = Long.parseLong(commandLines[i].split(" ")[1]);
							}
						}
						if (systemAccount) {
							if (lamports < 100_000_000) { // 0.1 SOL
								systemAccountsWithoutSol.incrementAndGet();
								transfer.setAmount(0);
							} else {
								systemAccountsWithSol.incrementAndGet();
							}
						} else {
							nonSystemAccounts.incrementAndGet();
							transfer.setAmount(0);
						}
						int count = counter.incrementAndGet();
						if (count % 100 == 0) {
							log.info("systemAccountsWithSol: {}, systemAccountsWithoutSol: {}, nonSystemAccounts: {}",
									systemAccountsWithSol.get(), systemAccountsWithoutSol, nonSystemAccounts.get());
						}
					} catch (IOException e) {
						log.error("error when getting account info");
						throw new RuntimeException(e);
					}
				});
			}
			ExecutorUtils.execute(Executors.newFixedThreadPool(50), runnables).blockUntilDone();
			log.info("filtered");
			log.info("systemAccountsWithSol: {}, systemAccountsWithoutSol: {}, nonSystemAccounts: {}",
					systemAccountsWithSol.get(), systemAccountsWithoutSol, nonSystemAccounts.get());
		}

		transfers = transfers.stream().filter(t -> t.getAmount() > 0).collect(Collectors.toList());
		log.info("totalAmount: {}, transfers: {}", transfers.stream().mapToDouble(Transfer::getAmount).sum(), transfers.size());

		runnables = new HashSet<>();
		int numberOfSplits = transfers.size() / MAX_TRANSFERS_IN_TX;
		for (int i = 0; i < numberOfSplits; i++) {
			runnables.add(createRunnable(transfers, i * MAX_TRANSFERS_IN_TX, (i + 1) * MAX_TRANSFERS_IN_TX));
		}
		if (transfers.size() % MAX_TRANSFERS_IN_TX > 0) {
			runnables.add(createRunnable(transfers, numberOfSplits * MAX_TRANSFERS_IN_TX, transfers.size()));
		}

		return runnables;
	}

	private static Runnable createRunnable(List<Transfer> transfers, int idxFrom, int idxTo) {
		return () -> {
			String arg = transfers.subList(idxFrom, idxTo).stream().map(Transfer::asString).collect(Collectors.joining(","));
			String command = "send_spl_tokens";
			if (transfers.stream().anyMatch(t -> t.getMint() == null)) {
				command = "send_sol";
			}
			try {
				String s = execCmd(CLI_START + command + " -input " + arg + CLI_END);
				System.out.println(s);
				boolean success = s.contains("Your transaction signature");
				if (success) {
					successCounter.incrementAndGet();
				} else {
					synchronized (toFix) {
						toFix.addAll(transfers.subList(idxFrom, idxTo));
					}
					failCounter.incrementAndGet();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				log.info("processed [{}/{}]", counter.incrementAndGet(), total);
			}
		};
	}

	public static String execCmd(String cmd) throws java.io.IOException {
		java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	@Data
	private static class Transfer {

		private String mint;
		private Integer mintDecimals;
		private double weight;
		private double amount;
		private String recipient;

		public String asString() {
			if (mint == null) {
				return String.format("%s,%.9f", recipient, amount);
			} else {
				return String.format("%s,%d,%s,%s", mint, mintDecimals, amount, recipient);
			}
		}

	}

}
