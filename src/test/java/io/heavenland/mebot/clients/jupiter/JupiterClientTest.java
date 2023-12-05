package io.heavenland.mebot.clients.jupiter;

import io.heavenland.mebot.RpcClientFactory;
import io.heavenland.mebot.clients.jupiter.dto.JupiterRouteDTO;
import io.heavenland.mebot.clients.jupiter.dto.JupiterTransactionsDTO;
import io.heavenland.mebot.clients.magiceden.SuperTransaction;
import io.heavenland.mebot.domain.Account;
import io.heavenland.mebot.domain.Token;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.RpcSendTransactionConfig;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
public class JupiterClientTest {

	private final RpcClient solRpcClient = RpcClientFactory.create(Cluster.MAINNET);

	@Test
	public void testBasic() {
		List<JupiterRouteDTO> routes = JupiterClient.instance().getQuote(
				Token.WSOL, Token.HTO,
				BigDecimal.valueOf(0.01).multiply(BigDecimal.TEN.pow(Token.WSOL.getDecimals())).toBigInteger(),
				BigDecimal.valueOf(1.),
				false);
		log.info("routes: {}", routes);

		JupiterTransactionsDTO jupiterTx = JupiterClient.instance().swap(Account.TEST, routes.get(0), true);
		log.info("jupiterTx: {}", jupiterTx);

		Set<String> txs = new HashSet<>();
		if (jupiterTx.getSetupTransaction() != null) {
			txs.add(jupiterTx.getSetupTransaction());
		}
		txs.add(jupiterTx.getSwapTransaction());

		String txHash;
		try {
			for (String tx : txs) {
				byte[] serializedMessage = Base64.getDecoder().decode(tx);

				SuperTransaction superTx = SuperTransaction.from(serializedMessage);
				superTx.setRecentBlockHash(solRpcClient.getApi().getRecentBlockhash());
				superTx.setFeePayer(Account.TEST.getSolanaAccount().getPublicKey());

				superTx.sign(Account.TEST.getSolanaAccount(), true);

				byte[] txData = superTx.serialize();

				String base64Trx = Base64.getEncoder().encodeToString(txData);
				List<Object> params = new ArrayList<>();
				params.add(base64Trx);
				params.add(new RpcSendTransactionConfig());

				txHash = solRpcClient.call("sendTransaction", params, String.class);
				log.info("txHash: {}", txHash);
			}
		} catch (RpcException e) {
			log.error("exception when calling Solana RPC", e);
		}

	}

}
