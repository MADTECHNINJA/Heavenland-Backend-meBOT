package io.heavenland.mebot.metacity;

import com.fasterxml.jackson.core.type.TypeReference;
import io.heavenland.mebot.context.market_data.NftMetadataService;
import io.heavenland.mebot.domain.NftMetadata;
import io.heavenland.mebot.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Slf4j
public class MetaCityTest {

	private final static NftMetadataService nftMetadataService = new NftMetadataService();

	private static final Set<String> fileNames = Set.of(
			"metacity_9PimqSpUmsQGKEi7fatueaGeACz9dZm5XRroGrqiKY2t_2.json",
			"metacity_HbfZtN2kw1NuVn9jyu1ZQ71s8QGScLswkdxLgBhAeZTh_1.json"
	);

	@Test
	public void test() throws IOException, InterruptedException {
		List<String> mints = new ArrayList<>();
		for (String fileName : fileNames) {
			Set<String> content = JsonUtils.stringToObject(FileUtils.readFileToString(new File(fileName), StandardCharsets.UTF_8), new TypeReference<>() {
			});
			if (content != null) {
				mints.addAll(content);
			}
		}
		log.info("mints: {}", mints.size());

		List<String> lines = new ArrayList<>();
		lines.add("mint,cNFT");
		for (String mint : mints) {
			NftMetadata metaData = nftMetadataService.getMetadata(mint);
			lines.add(mint + "," + getCNft(metaData.getAttributes().get("Tiers")));
		}
		FileUtils.writeLines(new File("metacity.csv"), lines);
	}

	public int getCNft(String tiersValue) {
		switch (tiersValue) {
			case "1" -> {
				return 60;
			}
			case "2" -> {
				return 45;
			}
			case "3" -> {
				return 30;
			}
		}
		log.error("unhandled " + tiersValue);
		return 0;
	}

}
