package io.heavenland.mebot.paragon;

import com.fasterxml.jackson.databind.ser.Serializers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class TxCountTest {

	private final static String ENV = "mainnet-beta";
	private final static String RPC_URL = "https://weathered-proud-shape.solana-mainnet.quiknode.pro/75114e7c361deae9deac7f76ac11831fcfccd677/";

	private final static File txHashFile = new File("hto-txs.txt");

	@Test
	public void countTxs() throws IOException {
		execCmd("solana config set -u " + ENV);
		execCmd("solana config set -u " + RPC_URL);

		Set<String> txHashes = new HashSet<>();
		if (txHashFile.exists()) {
			txHashes.addAll(FileUtils.readLines(txHashFile, StandardCharsets.UTF_8));
		}
		String lastTxHash = null;
		while (true) {
			String cmd = "solana transaction-history htoHLBJV1err8xP5oxyQdV2PLQhtVjxLXpKB7FsgJQD --limit 1000";
			if (lastTxHash != null) {
				cmd += " --before "+lastTxHash;
			}
			String s = execCmd(cmd);

			int added = 0;
			for (String line : s.split(System.lineSeparator())) {
				try {
					Base58.decodeToBigInteger(line);
				} catch (AddressFormatException e) {
					continue;
				}
				if (!txHashes.contains(line)) {
					txHashes.add(line);
					lastTxHash = line;
					added++;
				}
			}

			if (added == 0) {
				break;
			}
			log.info("added: {}, total: {}", added, txHashes.size());
			FileUtils.writeLines(txHashFile, txHashes);
		}
	}

	public static String execCmd(String cmd) throws java.io.IOException {
		java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

}
