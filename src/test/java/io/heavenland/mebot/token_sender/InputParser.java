package io.heavenland.mebot.token_sender;

import com.fasterxml.jackson.core.type.TypeReference;
import io.heavenland.mebot.utils.JsonUtils;
import lombok.Data;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class InputParser {

	public static void main(String[] args) throws IOException {
		String content = FileUtils.readFileToString(new File("mebot" + File.separator + "doggo" + File.separator + "greatgoats.json"), StandardCharsets.UTF_8);
		/*
		Map<String, Integer> map = JsonUtils.stringToObject(content, new TypeReference<>() {
		});

		List<String> lines = map.entrySet().stream().map(e -> e.getKey() + "," + e.getValue()).toList();
		 */
		Set<Owner> owners = JsonUtils.stringToObject(content, new TypeReference<>() {
		});
		List<String> lines = owners.stream().map(o -> o.getOwner()+","+o.getCount()).toList();

		FileUtils.writeLines(new File("mebot" + File.separator + "doggo" + File.separator + "greatgoats1.csv"), lines);
	}

	@Data
	private static class Owner {

		private String owner;
		private Set<String> mints;
		private int count;

	}

	@Data
	private static class Wallet {

		private Set<String> staked;
		private Set<String> unstaked;
		private String wallet;

		public int getStaked() {
			if (staked == null) {
				return 0;
			}
			return staked.size();
		}

	}

}
