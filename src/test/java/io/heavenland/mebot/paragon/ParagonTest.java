package io.heavenland.mebot.paragon;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.heavenland.mebot.context.market_data.NftMetadataService;
import io.heavenland.mebot.context.wallet.WalletFacade;
import io.heavenland.mebot.domain.NftMetadata;
import io.heavenland.mebot.domain.Token;
import io.heavenland.mebot.paragon.dto.*;
import io.heavenland.mebot.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.p2p.solanaj.rpc.Cluster;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

@Slf4j
public class ParagonTest {

	private final static NftMetadataService nftMetadataService = new NftMetadataService();
	private final static WalletFacade walletFacade = new WalletFacade();

	// ============
	// Check variables from here
	// ============
	private final static boolean DO_BREEDING = true;
	private final static Map<Integer, Predicate<BreedStats>> breedPredicateMap = Map.of(
			0, stats -> stats.getIncreasePercent() > 4,
			1, stats -> stats.getIncreasePercent() > 7,
			2, stats -> stats.getIncreasePercent() > 10
	);

	private final static boolean DO_FUSING = false;
	private final static Set<Integer> fusibleTiers = Set.of(0, 1, 2, 3);

	private final static boolean DO_CLOSING = true;

	private static final double MAX_TIER0_AVG_ATT_VALUE = 13.;
	private final static long SLEEP_BETWEEN_OPERATION_CREATE = 0;

	// ============
	// to here (others are needed to set only once)
	// ============

	private final static String RECEIVE_SHITS_WALLET = "J1CHG5pAMT4GRprmLwuQ4JzTcjJxTuXD2nsZDqp7924x";
	private final static Predicate<Paragon> shitParagonPredicate = p ->
			(p.getTier() == 0 && p.getScore() < 80)
					|| (p.getTier() == 1 && p.getScore() < 194)
					|| (p.getTier() == 2 && p.getScore() < 294)
					|| (p.getTier() == 3 && p.getScore() < 394);

	private final static String RECEIVE_TOP_WALLET = "p6dwZZUSeU4zHu6zpwonBM6E5Q6W53nUaZV59Rij8UD";
	private final static Predicate<Paragon> topParagonPredicate = p -> p.getTier() >= 4;

	private static final int NUM_RETIRES = 50;
	private final static int MAX_CLOSE_BATCH_SIZE = 20;

	private final static String ENV = "mainnet-beta";
	private final static String RPC_URL = "https://weathered-proud-shape.solana-mainnet.quiknode.pro/75114e7c361deae9deac7f76ac11831fcfccd677/";

	private final static String KEYPAIR_BASE_PATH = "/Users/jlochman/Documents/Solana/keys/";
	private final static List<String> KEYPAIR_FILE_NAMES = List.of(
			//"vocas.json",
			"voser.json",
			"jesus.json"
	);

	private static String pubKey;
	private static String cliStart;
	private static String cliEnd;

	@Test
	public void getStuckOperations() throws IOException {
		cliStart = "yarn --cwd /Users/jlochman/Documents/Solana/Paragon-mainnet/Paragon-Program/paragon ts-node ";
		cliEnd = " -e " + ENV + " -r " + RPC_URL + " -k " + KEYPAIR_BASE_PATH + KEYPAIR_FILE_NAMES.get(0);

		String s;
		OperationsDTO operations = getAllOperations(5);

		List<String> stuckPDAs = new ArrayList<>();
		for (BreedingPDA breeding : operations.getBreeding()) {
			if (breeding.getStatus() == 1
					&& Instant.now().toEpochMilli() - breeding.getEnterTime() * 1000 > Duration.ofHours(4L).toMillis()) {
				stuckPDAs.add(breeding.getPda());
			}
		}

		for (FusingPDA fusing : operations.getFusion()) {
			if (fusing.getStatus() == 1
					&& Instant.now().toEpochMilli() - fusing.getEnterTime() * 1000 > Duration.ofHours(4L).toMillis()) {
				stuckPDAs.add(fusing.getPda());
			}
		}

		for (StampingPDA stamping : operations.getStamping()) {
			if (Instant.now().toEpochMilli() - stamping.getEnterTime() * 1000 > Duration.ofHours(4L).toMillis()) {
				stuckPDAs.add(stamping.getPda());
			}
		}
		System.out.println("Stuck Operations: " + stuckPDAs.size());
		stuckPDAs.forEach(System.out::println);
	}

	@Test
	public void test() throws IOException, InterruptedException {
		for (String keypairFineName : KEYPAIR_FILE_NAMES) {
			pubKey = execCmd("solana address --keypair " + KEYPAIR_BASE_PATH + keypairFineName).trim();
			BigDecimal solInitBalance = BigDecimal.valueOf(walletFacade.getSolBalance(pubKey, Cluster.MAINNET), 9);
			BigDecimal htoInitBalance = walletFacade.getTokenBalance(pubKey, Token.HTO, Cluster.MAINNET);

			log.info("{}, SOL: {}, HTO: {}", keypairFineName, solInitBalance, htoInitBalance);
		}

		List<String> logs = new ArrayList<>();
		for (String keypairFineName : KEYPAIR_FILE_NAMES) {
			log.info("processing {}", keypairFineName);

			pubKey = execCmd("solana address --keypair " + KEYPAIR_BASE_PATH + keypairFineName).trim();
			log.info("pubKey: {}", pubKey);

			cliStart = "yarn --cwd /Users/jlochman/Documents/Solana/Paragon-mainnet/Paragon-Program/paragon ts-node ";
			cliEnd = " -e " + ENV + " -r " + RPC_URL + " -k " + KEYPAIR_BASE_PATH + keypairFineName;

			execCmd("solana config set -u " + ENV);
			execCmd("solana config set -u " + RPC_URL);
			execCmd("solana config set --keypair " + KEYPAIR_BASE_PATH + keypairFineName);
			execCmd("solana config set --commitment processed");

			BigDecimal solInitBalance = BigDecimal.valueOf(walletFacade.getSolBalance(pubKey, Cluster.MAINNET), 9);
			BigDecimal htoInitBalance = walletFacade.getTokenBalance(pubKey, Token.HTO, Cluster.MAINNET);
			log.info("Initial SOL: {}, HTO: {}", solInitBalance, htoInitBalance);

			int claimings = 0, badlings = 0, toplings = 0, breedings = 0, fusings = 0, closings = 0;
			int newClaimings = 0, newBadlings = 0, newToplings = 0, newBreedings = 0, newFusings = 0, newClosings = 0;

			newClaimings = Integer.MAX_VALUE;
			while (newClaimings > 0) {
				log.info("claiming...");
				newClaimings = claimFinishedOperations();
				claimings += newClaimings;
				if (newClaimings > 0) {
					Thread.sleep(30_000);
				}
			}

			int operations = Integer.MAX_VALUE;
			while (operations > 0) {
				Set<Paragon> paragons = getOwnedParagons(NUM_RETIRES);
				log.info("paragons: {}", paragons.size());
				log.info("badlings...");
				newBadlings = transferBadParagon(paragons);

				log.info("toplings...");
				newToplings = transferTopParago(paragons);

				log.info("Tier0 avgScore: {}", paragons.stream().filter(p -> p.getTier() == 0).mapToInt(Paragon::getScore).average().orElse(0.));
				Set<Paragon.CoreAttribute> crippledAttributes = new HashSet<>();
				for (Paragon.CoreAttribute coreAttribute : Paragon.CoreAttribute.values()) {
					final Paragon.CoreAttribute fCoreAttribute = coreAttribute;
					double avgValue = paragons.stream().filter(p -> p.getTier() == 0)
							.mapToInt(p -> p.getAttValues().get(fCoreAttribute))
							.average().orElse(0.);
					if (avgValue > MAX_TIER0_AVG_ATT_VALUE) {
						crippledAttributes.add(coreAttribute);
					}
					log.info(" Tier0 avg{}: {}", fCoreAttribute, avgValue);
				}

				if (DO_BREEDING) {
					log.info("breeding...");
					newBreedings = breedParagons(paragons, crippledAttributes);
				}
				if (DO_FUSING) {
					log.info("fusing...");
					newFusings = fuseParagons(paragons, crippledAttributes);
				}
				operations = newBadlings + newBreedings + newFusings;
				badlings += newBadlings;
				toplings += newToplings;
				breedings += newBreedings;
				fusings += newFusings;
				if (operations > 0) {
					Thread.sleep(30_000);
				}
			}

			if (DO_CLOSING) {
				newClosings = Integer.MAX_VALUE;
				while (newClosings > 0) {
					log.info("closing useless...");
					newClosings = closeEmptyAccounts();
					closings += newClosings;
					if (newClosings > 0) {
						Thread.sleep(30_000);
					}
				}
			}

			BigDecimal solFinalBalance = BigDecimal.valueOf(walletFacade.getSolBalance(pubKey, Cluster.MAINNET), 9);
			BigDecimal htoFinalBalance = walletFacade.getTokenBalance(pubKey, Token.HTO, Cluster.MAINNET);
			logs.add(String.format("%s, Final SOL: %s [%s], HTO: %s [%s]",
					keypairFineName,
					solFinalBalance, solFinalBalance.subtract(solInitBalance),
					htoFinalBalance, htoFinalBalance.subtract(htoInitBalance)
			));
			logs.add(String.format("%s, claimings: %s, badlings: %s, toplings: %s, breedings: %s, fusings: %s, closings: %s",
					keypairFineName, claimings, badlings, toplings, breedings, fusings, closings
			));
		}

		logs.forEach(log::info);
	}

	private static int transferBadParagon(Set<Paragon> paragons) throws IOException {
		int badlings = 0;
		Iterator<Paragon> it = paragons.iterator();
		while (it.hasNext()) {
			Paragon paragon = it.next();
			if (shitParagonPredicate.test(paragon)) {
				log.info("sending to {} {}", RECEIVE_SHITS_WALLET, paragon);
				String s = execCmd("spl-token transfer " + paragon.getMint() + " 1 " + RECEIVE_SHITS_WALLET + " --fund-recipient");
				System.out.println(s);
				it.remove();
				badlings++;
			}
		}
		return badlings;
	}

	private static int transferTopParago(Set<Paragon> paragons) throws IOException {
		int toplings = 0;
		Iterator<Paragon> it = paragons.iterator();
		while (it.hasNext()) {
			Paragon paragon = it.next();
			if (topParagonPredicate.test(paragon)) {
				log.info("sending to {} {}", RECEIVE_TOP_WALLET, paragon);
				String s = execCmd("spl-token transfer " + paragon.getMint() + " 1 " + RECEIVE_TOP_WALLET + " --fund-recipient");
				System.out.println(s);
				it.remove();
				toplings++;
			}
		}
		return toplings;
	}

	private static int breedParagons(Set<Paragon> paragons, Set<Paragon.CoreAttribute> crippledAttributes) throws IOException, InterruptedException {
		Set<BreedPair> pairsToBreed = new HashSet<>();
		for (int breedCount : breedPredicateMap.keySet()) {
			final int fBreedCount = breedCount;
			Set<Paragon> breedableParagons = paragons.stream()
					.filter(p -> p.getTier() == 0 && p.getBreedCount() == fBreedCount)
					.filter(p -> {
						for (Paragon.CoreAttribute crippledAttribute : crippledAttributes) {
							if (p.getAttValues().get(crippledAttribute) > MAX_TIER0_AVG_ATT_VALUE) {
								return false;
							}
						}
						return true;
					})
					.collect(Collectors.toSet());
			Table<Paragon, Paragon, BreedStats> tblBreedStats = HashBasedTable.create();
			for (Paragon par1 : breedableParagons) {
				for (Paragon par2 : breedableParagons) {
					if (par1.equals(par2)) {
						continue;
					}
					BreedStats breedStats = getBreedStats(par1, par2);
					tblBreedStats.put(par1, par2, breedStats);
					tblBreedStats.put(par2, par1, breedStats);
				}
			}

			while (tblBreedStats.size() > 1) {
				double bestScoreIncreasePercent = 0;
				BreedPair bestBreedPair = null;
				for (Paragon par1 : tblBreedStats.columnKeySet()) {
					for (Paragon par2 : tblBreedStats.rowKeySet()) {
						if (par1.equals(par2)) {
							continue;
						}
						BreedStats breedStats = tblBreedStats.get(par1, par2);
						if (breedStats != null && breedStats.getIncreasePercent() > bestScoreIncreasePercent) {
							bestScoreIncreasePercent = breedStats.getIncreasePercent();
							bestBreedPair = new BreedPair(par1, par2);
							bestBreedPair.setBreedStats(breedStats);
						}
					}
				}
				if (bestBreedPair != null) {
					pairsToBreed.add(bestBreedPair);
					tblBreedStats.row(bestBreedPair.getPar1()).clear();
					tblBreedStats.row(bestBreedPair.getPar2()).clear();
					tblBreedStats.column(bestBreedPair.getPar1()).clear();
					tblBreedStats.column(bestBreedPair.getPar2()).clear();
				} else {
					break;
				}
			}
		}

		int breedings = 0;
		for (BreedPair breedPair : pairsToBreed.stream()
				.sorted(Comparator.comparingDouble(bp -> -bp.getBreedStats().getOutputExpectedScore()))
				.toList()) {
			if (!breedPredicateMap.get(breedPair.getBreedCount()).test(breedPair.getBreedStats())) {
				continue;
			}
			log.info("creating breeding {}", breedPair);
			String s = execCmd(cliStart + "breed_init" +
					" -nft1 " + breedPair.getPar1().getMint() +
					" -nft2 " + breedPair.getPar2().getMint() +
					" -p " + breedPair.getCostHto() + cliEnd);
			paragons.remove(breedPair.getPar1());
			paragons.remove(breedPair.getPar2());
			System.out.println(s);
			Thread.sleep(SLEEP_BETWEEN_OPERATION_CREATE);
			breedings++;
		}
		return breedings;
	}

	private static int fuseParagons(Set<Paragon> paragons, Set<Paragon.CoreAttribute> crippledAttributes) throws IOException, InterruptedException {
		Set<FuseSet> setsToFuse = new HashSet<>();
		for (int tier : fusibleTiers) {
			final int fTier = tier;
			Predicate<Paragon> predicate = p -> p.getTier() == fTier;
			if (fTier == 0) {
				predicate = predicate.and(p -> {
					if (p.getBreedCount() >= 2) {
						return true;
					} else if (p.getBreedCount() == 1) {
						for (Paragon.CoreAttribute crippledAttribute : crippledAttributes) {
							if (p.getAttValues().get(crippledAttribute) > 2 * MAX_TIER0_AVG_ATT_VALUE) {
								return true;
							}
						}
					}
					return false;
				});
			}

			List<Paragon> filteredParagons = paragons.stream()
					.filter(predicate).toList();
			if (CollectionUtils.isEmpty(filteredParagons)) {
				continue;
			}

			TreeMap<Integer, List<Paragon>> paragonMap = new TreeMap<>(Comparator.reverseOrder());
			for (Paragon paragon : filteredParagons) {
				paragonMap.putIfAbsent(paragon.getScore(), new ArrayList<>());
				paragonMap.get(paragon.getScore()).add(paragon);
			}
			int highestScore = paragonMap.firstKey();

			for (Paragon mainParagon : paragonMap.get(highestScore)) {
				for (Integer subScore : paragonMap.keySet().stream().sorted(Comparator.naturalOrder()).toList()) {
					if (subScore == highestScore) {
						continue;
					}
					if (paragonMap.get(subScore).size() >= 4) {
						List<Paragon> selectedSubParagons = new ArrayList<>();
						for (int i = 0; i < 4; i++) {
							selectedSubParagons.add(paragonMap.get(subScore).get(0));
							paragonMap.get(subScore).remove(0);
						}
						FuseSet fuseSet = new FuseSet(mainParagon, selectedSubParagons);
						fuseSet.setResultingSore(mainParagon.getScore() + getFusingScoreIncrease(fuseSet.getMainParagon(), fuseSet.getSubParagons()));
						setsToFuse.add(fuseSet);
						break;
					}
				}
			}

			if (setsToFuse.isEmpty()) {
				if (paragonMap.get(highestScore).size() >= 5 && paragonMap.values().stream().mapToInt(List::size).sum() * 0.8 <= paragonMap.get(highestScore).size()) {
					List<Paragon> mainParagons = paragonMap.get(highestScore);
					FuseSet fuseSet = new FuseSet(mainParagons.get(0), new ArrayList<>(mainParagons.subList(1, 5)));
					fuseSet.setResultingSore(mainParagons.get(0).getScore() + getFusingScoreIncrease(fuseSet.getMainParagon(), fuseSet.getSubParagons()));
					setsToFuse.add(fuseSet);
				}
			}
		}

		int fusings = 0;
		for (FuseSet fuseSet : setsToFuse.stream().sorted(Comparator.comparing(FuseSet::getResultingSore).reversed()).toList()) {
			log.info("creating fusing {}", fuseSet);
			String s = execCmd(cliStart + "fuse_init" +
					" -nftMain " + fuseSet.getMainParagon().getMint() +
					" -nftSub1 " + fuseSet.getSubParagons().get(0).getMint() +
					" -nftSub2 " + fuseSet.getSubParagons().get(1).getMint() +
					" -nftSub3 " + fuseSet.getSubParagons().get(2).getMint() +
					" -nftSub4 " + fuseSet.getSubParagons().get(3).getMint() +
					" -p " + fuseSet.getCostHto() + cliEnd);
			paragons.remove(fuseSet.getMainParagon());
			fuseSet.getSubParagons().forEach(paragons::remove);
			System.out.println(s);
			Thread.sleep(SLEEP_BETWEEN_OPERATION_CREATE);
			fusings++;
		}
		return fusings;
	}

	private static int claimFinishedOperations() throws IOException {
		String s;
		OperationsDTO operations = getAllOperations(5);

		log.info("breeding: {}, fusing: {}",
				operations.getBreeding().stream().collect(Collectors.groupingBy(
						BreedingPDA::getStatus,
						Collectors.counting())),
				operations.getFusion().stream().collect(Collectors.groupingBy(
						FusingPDA::getStatus,
						Collectors.counting()))
		);

		int claimed = 0;
		for (BreedingPDA breeding : operations.getBreeding()) {
			if ((breeding.getStatus() == 2)
					|| (breeding.getStatus() == 3 && breeding.getUnlockTime() < Instant.now().toEpochMilli() / 1000)) {
				log.info("claiming breeding {}", breeding);
				s = execCmd(cliStart + "breed_claim -pda " + breeding.getPda() + cliEnd);
				nftMetadataService.invalidate(breeding.getNftMint1());
				nftMetadataService.invalidate(breeding.getNftMint2());
				nftMetadataService.invalidate(breeding.getBornMint());
				System.out.println(s);
				claimed++;
			}
		}

		for (FusingPDA fusing : operations.getFusion()) {
			if ((fusing.getStatus() == 2)
					|| (fusing.getStatus() == 3 && fusing.getUnlockTime() < Instant.now().toEpochMilli() / 1000)) {
				log.info("claiming fusing {}", fusing);
				s = execCmd(cliStart + "fuse_claim -pda " + fusing.getPda() + cliEnd);
				nftMetadataService.invalidate(fusing.getNftMint());
				nftMetadataService.invalidate(fusing.getNftMint1());
				nftMetadataService.invalidate(fusing.getNftMint2());
				nftMetadataService.invalidate(fusing.getNftMint3());
				nftMetadataService.invalidate(fusing.getNftMint4());
				nftMetadataService.invalidate(fusing.getFusion());
				System.out.println(s);
				claimed++;
			}
		}
		return claimed;
	}

	private int closeEmptyAccounts() throws IOException {
		String s;
		OperationsDTO operations = getAllOperations(5);

		Set<String> activeMints = new HashSet<>();
		for (BreedingPDA breeding : operations.getBreeding()) {
			activeMints.add(breeding.getNftMint1());
			activeMints.add(breeding.getNftMint2());
			activeMints.add(breeding.getBornMint());
		}
		for (FusingPDA fusing : operations.getFusion()) {
			activeMints.add(fusing.getNftMint());
			activeMints.add(fusing.getNftMint1());
			activeMints.add(fusing.getNftMint2());
			activeMints.add(fusing.getNftMint3());
			activeMints.add(fusing.getNftMint4());
			activeMints.add(fusing.getFusion());
		}
		for (StampingPDA stamping : operations.getStamping()) {
			activeMints.add(stamping.getParagon());
		}

		Set<String> emptyATAs = getEmptyTokenAccounts();
		int closings = 0;
		Set<String> closeBatch = new HashSet<>();
		for (String emptyATA : emptyATAs) {
			if (activeMints.contains(emptyATA)) {
				continue;
			}
			closeBatch.add(emptyATA);

			if (closeBatch.size() >= MAX_CLOSE_BATCH_SIZE) {
				String closeMints = String.join(",", closeBatch);
				log.info("closing {} ATAs", closeBatch.size());
				s = execCmd(cliStart + " close_ata -mint " + closeMints + cliEnd);
				System.out.println(s);
				closings += closeBatch.size();
				closeBatch.clear();
			}
		}

		if (!closeBatch.isEmpty()) {
			String closeMints = String.join(",", closeBatch);
			log.info("closing {}", closeMints);
			s = execCmd(cliStart + " close_ata -mint " + closeMints + cliEnd);
			System.out.println(s);
			closings += closeBatch.size();
			closeBatch.clear();
		}

		return closings;
	}

	private Set<String> getEmptyTokenAccounts() throws IOException {
		Set<String> emptyATAs = new HashSet<>();

		String s = execCmd("spl-token accounts --owner " + pubKey);
		List<String> lines = Arrays.stream(s.split(System.lineSeparator())).toList();
		for (String line : lines) {
			if (ObjectUtils.isEmpty(line)) {
				continue;
			}
			String[] split = line.split("\\s+");
			if (split.length == 2) {
				try {
					double balance = Integer.parseInt(split[1]);
					if (balance == 0) {
						emptyATAs.add(split[0]);
					}
				} catch (NumberFormatException e) {
					// do nothing
				}
			}
		}

		return emptyATAs;
	}

	private Set<Paragon> getOwnedParagons(int numRetires) throws IOException, InterruptedException {
		while (numRetires > 0) {
			try {
				return getOwnedParagons();
			} catch (IOException | NullPointerException e) {
				numRetires--;
				if (numRetires == 0) {
					throw e;
				}
				log.error("error when downloading paragons, {}, remaining tries: {}", e.getMessage(), numRetires);
				Thread.sleep(5_000);
			}
		}
		return new HashSet<>();
	}

	private Set<Paragon> getOwnedParagons() throws IOException {
		Set<Paragon> paragons = new HashSet<>();

		String s = execCmd("spl-token accounts --owner " + pubKey);
		List<String> lines = Arrays.stream(s.split(System.lineSeparator())).toList();

		for (String line : lines) {
			if (ObjectUtils.isEmpty(line)) {
				continue;
			}
			String[] split = line.split("\\s+");
			if (split.length == 2) {
				try {
					double balance = Integer.parseInt(split[1]);
					if (balance == 1) {
						String nftMint = split[0];
						NftMetadata metaData = nftMetadataService.getMetadata(nftMint);
						if (metaData.getName().startsWith("Paragon")
								&& metaData.getUpdateAuthority().equals("HLApLHj43LPLCUKXaapKmanMaHcFy47pXBPVg6hnoj9v")) {
							paragons.add(Paragon.fromMetadata(metaData));
						}
					}
				} catch (NumberFormatException e) {
					// do nothing
				}
			}
		}

		return paragons;
	}

	private static OperationsDTO getAllOperations(int numTries) {
		while (numTries > 0) {
			try {
				return getAllOperations();
			} catch (Exception e) {
				log.error("error when getting operations, numTries: " + numTries, e);
			}
			numTries--;
		}
		log.error("no operations obtained");
		System.exit(1);
		return null;
	}


	private static OperationsDTO getAllOperations() throws Exception {
		String s = execCmd(cliStart + "get_all_operations -a " + pubKey + cliEnd);
		if (s.contains("There is no match for this address and operation!")) {
			return new OperationsDTO();
		}
		int idxFrom = s.indexOf("{");
		int idxTo = s.lastIndexOf("}");
		s = s.substring(idxFrom, idxTo + 1);

		OperationsDTO operations = JsonUtils.stringToObject(s, OperationsDTO.class);
		if (operations == null) {
			throw new Exception("no operations obtained");
		}
		return operations;
	}


	private static BreedStats getBreedStats(Paragon par1, Paragon par2) {
		double meanScore = 0;
		for (Paragon.CoreAttribute coreAttribute : Paragon.CoreAttribute.values()) {
			meanScore += getBreedingMeanAttValue(par1.getAttValues().get(coreAttribute), par2.getAttValues().get(coreAttribute));
		}
		BreedStats breedStats = new BreedStats(Math.max(par1.getScore(), par2.getScore()), meanScore);
		for (Paragon.CoreAttribute coreAttribute : Paragon.CoreAttribute.values()) {
			breedStats.getInputMaxAttValue().put(coreAttribute,
					Math.max(
							par1.getAttValues().get(coreAttribute),
							par2.getAttValues().get(coreAttribute)
					)
			);
		}
		return breedStats;
	}

	private static double getBreedingMeanAttValue(double a1, double a2) {
		if (a1 > a2) {
			double b = a2;
			a2 = a1;
			a1 = b;
		}

		final double sigma = 2;
		final double sigmaR = sigma * Math.exp(1 + (a1 - a2) / 5);
		final double C = 1 - (a2 - a1) / 50;

		List<Double> probabilities = new ArrayList<>();
		for (int i = 0; i <= 50; i++) {
			double value = Math.exp(-0.5 * Math.pow((i - a1) / sigma, 2));
			if (i > a2) {
				value += C * Math.exp(-0.5 * Math.pow((i - a2) / sigmaR, 2));
			} else {
				value += C * Math.exp(-0.5 * Math.pow((i - a2) / sigma, 2));
			}
			probabilities.add(value);
		}
		double sum = probabilities.stream().mapToDouble(Double::doubleValue).sum();
		probabilities = probabilities.stream().map(x -> x / sum).collect(Collectors.toList());

		double meanValue = 0;
		for (int i = 0; i < probabilities.size(); i++) {
			meanValue += i * probabilities.get(i);
		}
		return meanValue;
	}

	private static int getFusingScoreIncrease(Paragon mainParagon, Collection<Paragon> subParagons) {
		int tier = mainParagon.getTier();
		double cAlpha = (double) (mainParagon.getScore() - getTierMinScore(tier)) / getTierScoreRange(tier);

		double cBeta = 0;
		for (Paragon subParagon : subParagons) {
			cBeta += 0.25 * (subParagon.getScore() - getTierMinScore(tier)) / getTierScoreRange(tier);
		}

		double cBetaSigma = 1. - getVariance(() -> subParagons.stream().mapToDouble(Paragon::getScore)) / getTierScoreRange(tier);
		cBetaSigma = Math.max(cBetaSigma, 0);

		int scoreIncreaseMin = getTierMinScore(tier + 1) - mainParagon.getScore();
		return (int) (scoreIncreaseMin + getTierScoreRange(tier + 1) * Math.max(Math.tanh(cAlpha + cBeta + cBetaSigma - 1), 0));
	}

	private static double getVariance(Supplier<DoubleStream> streamSupplier) {
		double avg = streamSupplier.get().summaryStatistics().getAverage();
		return streamSupplier.get().map(x -> Math.pow(x - avg, 2)).summaryStatistics().getAverage();
	}

	private static int getTierScoreRange(int tier) {
		return getTierMaxScore(tier) - getTierMinScore(tier);
	}

	private static int getTierMinScore(int tier) {
		return tier * 100;
	}

	private static int getTierMaxScore(int tier) {
		if (tier == 5) {
			return 700;
		}
		return 99 + 100 * tier;
	}

	public static String execCmd(String cmd) throws java.io.IOException {
		java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

}
