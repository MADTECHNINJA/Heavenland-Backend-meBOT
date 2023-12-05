package io.heavenland.mebot.context.market_data;

import com.fasterxml.jackson.core.type.TypeReference;
import io.heavenland.mebot.context.collection.MetadataProvider;
import io.heavenland.mebot.domain.NftMetadata;
import io.heavenland.mebot.utils.JsonUtils;
import io.heavenland.tools.metaplex.dto.Metadata;
import io.heavenland.tools.metaplex.dto.OffChainMetadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class NftMetadataService {

	// key: nft mintAddress, value: nft meta
	private Map<String, NftMetadata> nftMap = new HashMap<>();

	private final MetadataProvider provider = MetadataProvider.instance(Cluster.MAINNET);
	private final static File STORAGE_FILE = new File("nfts_meta_cache.json");

	public NftMetadataService() {
		try {
			log.info("loading from " + STORAGE_FILE.getAbsolutePath());
			load();
		} catch (IOException e) {
			log.error("error loading file {}", STORAGE_FILE);
		}
	}

	public NftMetadata getMetadata(String mintAddress) {
		int numTries = 5;
		while (numTries > 0) {
			try {
				return getMetadataNoRetry(mintAddress);
			} catch (RpcException | NullPointerException e) {
				log.error(e.getMessage() + " when downloading meta for " + mintAddress + ". numTries: " + numTries);
				numTries--;
				if (numTries == 0) {
					log.error("error when downloading meta for " + mintAddress, e);
				}
			}
		}
		return null;
	}

	public NftMetadata getMetadataNoRetry(String mintAddress) throws RpcException {
		if (!nftMap.containsKey(mintAddress)) {
			OffChainMetadata offChainData = null;
			Metadata onChainData = null;
			try {
				offChainData = provider.getOffChainMetadata(mintAddress);
				onChainData = provider.getOnChainMetadata(mintAddress);
				log.info("downloaded meta for {}", mintAddress);
				Thread.sleep(50L);
			} catch (RuntimeException | InterruptedException e) {
				log.error("exception for {}", mintAddress, e);
			}
			if (offChainData == null || onChainData == null) {
				return null;
			}

			NftMetadata meta = new NftMetadata();
			meta.setMintAddress(mintAddress);
			meta.setUpdateAuthority(onChainData.getUpdateAuthority().toBase58());
			meta.setName(onChainData.getData().getName());
			for (OffChainMetadata.Attribute attribute : offChainData.getAttributes()) {
				meta.getAttributes().put(
						attribute.getTraitType(),
						attribute.getValue() == null ? null : attribute.getValue().toString()
				);
			}
			nftMap.put(mintAddress, meta);
			try {
				save();
			} catch (IOException e) {
				log.error("error saving file {}", STORAGE_FILE);
			}
		}
		return nftMap.get(mintAddress);
	}

	public void invalidate(String mintAddress) {
		nftMap.remove(mintAddress);
		try {
			save();
		} catch (IOException e) {
			log.error("error saving file {}", STORAGE_FILE);
		}
	}

	private void load() throws IOException {
		if (STORAGE_FILE.exists()) {
			nftMap = JsonUtils.stringToObject(FileUtils.readFileToString(STORAGE_FILE, StandardCharsets.UTF_8), new TypeReference<>() {
			});
			if (nftMap == null) {
				nftMap = new HashMap<>();
			}
		}
	}

	private void save() throws IOException {
		FileUtils.write(STORAGE_FILE, JsonUtils.objectToPrettyString(nftMap), StandardCharsets.UTF_8);
	}

}
