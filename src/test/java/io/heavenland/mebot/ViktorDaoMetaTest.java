package io.heavenland.mebot;

import com.fasterxml.jackson.core.type.TypeReference;
import io.heavenland.mebot.context.market_data.NftMetadataService;
import io.heavenland.mebot.domain.NftMetadata;
import io.heavenland.mebot.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ViktorDaoMetaTest {

	private final static NftMetadataService nftMetadataService = new NftMetadataService();

	@Test
	public void downloadMetaTest() throws IOException {
		List<String> nftMints = JsonUtils.stringToObject(
				FileUtils.readFileToString(
						new File("hash_list_8ch2JB8Vn9hHg4uRwqps3xxW4mJu2ios6m6ZJ6jqUmB5.json"), StandardCharsets.UTF_8),
				new TypeReference<>() {
				});
		if (nftMints == null) {
			log.error("empty nftMints");
			System.exit(1);
		}
		int counter = 0;
		for (String nftMint : nftMints) {
			log.info("processing [{}/{}] {}", counter++, nftMints.size(), nftMint);
			NftMetadata meta = nftMetadataService.getMetadata(nftMint);
			log.info("attributes: {}", meta.getAttributes());
		}

		List<String> lines = new ArrayList<>();
		for (String nftMint : nftMints) {
			lines.add(nftMint + "," + nftMetadataService.getMetadata(nftMint).getAttributes().get("Tier"));
		}
		FileUtils.writeLines(new File("viktor-dao-metas.csv"), lines);
	}

}
