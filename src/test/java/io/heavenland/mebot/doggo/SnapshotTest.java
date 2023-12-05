package io.heavenland.mebot.doggo;

import com.fasterxml.jackson.core.type.TypeReference;
import io.heavenland.mebot.utils.JsonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class SnapshotTest {

	private final static String BASE_PATH = "/Users/jlochman/Documents/Solana/Paragon-mainnet/Paragon-Program/paragon/cli/";
	private final static File FIRST_FILE = new File(BASE_PATH+"Doggoyb1uHFJGFdHhJf8FKEBUMv58qo98CisWgeD7Ftk-1673890998503.json");
	private final static File SECOND_FILE = new File(BASE_PATH+"Doggoyb1uHFJGFdHhJf8FKEBUMv58qo98CisWgeD7Ftk-1674124503282.json");

	@Test
	public void test() throws IOException {
		Set<Holder> first = JsonUtils.stringToObject(FileUtils.readFileToString(FIRST_FILE, StandardCharsets.UTF_8), new TypeReference<>(){});
		Set<Holder> second = JsonUtils.stringToObject(FileUtils.readFileToString(SECOND_FILE, StandardCharsets.UTF_8), new TypeReference<>(){});

		Map<String, HolderChange> holderChanges = new HashMap<>();
		for (Holder holder : first) {
			HolderChange change = new HolderChange(holder);
			change.setAmountFirst(holder.getAmountDouble());
			holderChanges.put(holder.getOwner(), change);
		}
		for (Holder holder : second) {
			if (holderChanges.containsKey(holder.getOwner())) {
				holderChanges.get(holder.getOwner()).setAmountSecond(holder.getAmountDouble());
			} else {
				HolderChange change = new HolderChange(holder);
				change.setAmountSecond(holder.getAmountDouble());
				holderChanges.put(holder.getOwner(), change);
			}
		}

		System.out.println("biggest sellers: ");
		holderChanges.values().stream().sorted(Comparator.comparing(HolderChange::getChange)).limit(50L).forEach(c -> System.out.println(c.asString()));

		System.out.println("biggest buyers: ");
		holderChanges.values().stream().sorted(Comparator.comparing(HolderChange::getChange).reversed()).limit(50L).forEach(c -> System.out.println(c.asString()));

		System.out.println("biggest holders: ");
		holderChanges.values().stream().sorted(Comparator.comparing(HolderChange::getAmountSecond).reversed()).limit(50L).forEach(c -> System.out.println(c.asString()));

	}

	@Data
	private static class Holder {

		private String ata;
		private String owner;
		private long amount;

		public double getAmountDouble() {
			return BigDecimal.valueOf(amount, 5).doubleValue();
		}

	}

	@Data
	private static class HolderChange {

		private String ata;
		private String owner;
		private double amountFirst;
		private double amountSecond;

		public HolderChange(Holder holder) {
			this.ata = holder.getAta();
			this.owner = holder.getOwner();
		}

		public double getChange( ){
			return amountSecond - amountFirst;
		}

		public String asString() {
			return String.format("%s (%,.0f): %,.0f -> %,.0f ", owner, getChange(), amountFirst, amountSecond);
		}

	}

}
