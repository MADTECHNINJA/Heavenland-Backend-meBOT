package io.heavenland.mebot.context.engine;

import io.heavenland.mebot.*;
import io.heavenland.mebot.clients.jupiter.JupiterClient;
import io.heavenland.mebot.clients.jupiter.dto.JupiterRouteDTO;
import io.heavenland.mebot.clients.jupiter.dto.JupiterTransactionsDTO;
import io.heavenland.mebot.clients.magiceden.MagicEdenClient;
import io.heavenland.mebot.clients.magiceden.SimpleTx;
import io.heavenland.mebot.clients.magiceden.SuperTransaction;
import io.heavenland.mebot.clients.magiceden.dto.MagicEdenTxResponseDTO;
import io.heavenland.mebot.domain.Account;
import io.heavenland.mebot.domain.AccountListing;
import io.heavenland.mebot.domain.MarketListing;
import io.heavenland.mebot.domain.Token;
import io.heavenland.mebot.utils.SolanaUtils;
import io.heavenland.tools.solana.TokenAccountFinder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.bitcoinj.core.Base58;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.RpcSendTransactionConfig;
import org.p2p.solanaj.utils.ShortvecEncoding;
import org.p2p.solanaj.utils.TweetNaclFast;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.*;

@Slf4j
public class EngineService {

	private final RpcClient solRpcClient = RpcClientFactory.create(Cluster.MAINNET);
	private final MagicEdenClient meClient = MagicEdenClient.instance();
	private final JupiterClient jupiterClient = JupiterClient.instance();
	private final TokenAccountFinder tokenAccountFinder = new TokenAccountFinder(Constants.USE_MAINNET);

	public boolean swap(Account account, Token from, Token to, BigDecimal amount, BigDecimal slippage, boolean onlyDirectRoutes, boolean wrapUnwrapSol) {
		List<JupiterRouteDTO> routes = jupiterClient.getQuote(
				from, to, amount.multiply(BigDecimal.TEN.pow(from.getDecimals())).toBigInteger(), slippage, onlyDirectRoutes
		);
		if (CollectionUtils.isEmpty(routes)) {
			log.warn("empty routes for swapping {} {}->{} on {} with slippage {}, onlyDirectRoutes: {}",
					amount, from, to, account, slippage, onlyDirectRoutes);
			return false;
		}

		JupiterTransactionsDTO jupiterTx = jupiterClient.swap(account, routes.get(0), wrapUnwrapSol);
		if (jupiterTx == null || jupiterTx.getSwapTransaction() == null) {
			return false;
		}
		log.info("jupiterTx: {}", jupiterTx);
		List<String> txs = new ArrayList<>();
		if (jupiterTx.getSetupTransaction() != null) {
			txs.add(jupiterTx.getSetupTransaction());
		}
		txs.add(jupiterTx.getSwapTransaction());

		try {
			for (int i = 0; i < txs.size(); i++) {
				String tx = txs.get(i);
				byte[] serializedMessage = Base64.getDecoder().decode(tx);

				SuperTransaction superTx = SuperTransaction.from(serializedMessage);
				superTx.setRecentBlockHash(solRpcClient.getApi().getRecentBlockhash());
				superTx.setFeePayer(account.getSolanaAccount().getPublicKey());

				superTx.sign(account.getSolanaAccount(), true);

				byte[] txData = superTx.serialize();

				String base64Trx = Base64.getEncoder().encodeToString(txData);
				List<Object> params = new ArrayList<>();
				params.add(base64Trx);
				params.add(new RpcSendTransactionConfig());

				String txHash = solRpcClient.call("sendTransaction", params, String.class);
				log.info("txHash for swap {} {}->{} on {}: {}", amount, from, to, account, txHash);
				if (i < txs.size() - 1) {
					try {
						log.info("waiting 45 seconds to another transaction");
						Thread.sleep(45_000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
			return true;
		} catch (RpcException e) {
			log.error("exception when calling Solana RPC", e);
			return false;
		}
	}

	public boolean buyNow(Account account, MarketListing marketListing) {
		MagicEdenTxResponseDTO response = meClient.buyNow(
				account.getAddress(),
				marketListing.getSeller(),
				marketListing.getAuctionHouse(),
				marketListing.getTokenMint(),
				marketListing.getTokenAddress(),
				marketListing.getPrice());
		if (response == null || response.getTxSigned() == null || CollectionUtils.isEmpty(response.getTxSigned().getData())) {
			log.error("invalid response from ME: {}", response);
			return false;
		}
		List<Integer> txSignedByPubKey = response.getTxSigned().getData();
		String txHash;
		try {
			byte[] serializedMessage = SolanaUtils.uBytesToBytes(txSignedByPubKey.stream().mapToInt(i -> i).toArray());

			var superTx = SuperTransaction.from(serializedMessage);
			superTx.sign(account.getSolanaAccount(), true);

			byte[] txData = superTx.serialize();

			String base64Trx = Base64.getEncoder().encodeToString(txData);
			List<Object> params = new ArrayList<>();
			params.add(base64Trx);
			params.add(new RpcSendTransactionConfig());

			txHash = solRpcClient.call("sendTransaction", params, String.class);

			//Transaction tx = new SimpleTx(SolanaUtils.uBytesToBytes(txSignedByPubKey.stream().mapToInt(i -> i).toArray()));
			//txHash = solRpcClient.getApi().sendTransaction(tx, account.getSolanaAccount());
		} catch (RpcException e) {
			log.error("exception when calling Solana RPC", e);
			return false;
		}
		log.info("txHash: {} for {} on {}", txHash, marketListing, account);
		return true;
	}

	public boolean sell(Account account, AccountListing accountListing, String auctionHouseAddress, BigDecimal price) {
		String tokenAccount = findTokenAccount(account, accountListing);
		if (tokenAccount == null) {
			log.error("null tokenAccount for mintAddress:{}, on account:{}", accountListing.getMintAddress(), account.getAddress());
			return false;
		}
		MagicEdenTxResponseDTO response = meClient.sell(
				account.getAddress(),
				auctionHouseAddress,
				accountListing.getMintAddress(),
				tokenAccount,
				price);
		if (response == null || response.getTxSigned() == null || CollectionUtils.isEmpty(response.getTxSigned().getData())) {
			log.error("invalid response from ME: {}", response);
			return false;
		}
		List<Integer> txSignedByPubKey = response.getTxSigned().getData();

		String txHash;
		try {
			byte[] serializedMessage = SolanaUtils.uBytesToBytes(txSignedByPubKey.stream().mapToInt(i -> i).toArray());

			var superTx = SuperTransaction.from(serializedMessage);
			superTx.sign(account.getSolanaAccount(), true);

			byte[] txData = superTx.serialize();

			String base64Trx = Base64.getEncoder().encodeToString(txData);
			List<Object> params = new ArrayList();
			params.add(base64Trx);
			params.add(new RpcSendTransactionConfig());

			txHash = solRpcClient.call("sendTransaction", params, String.class);
		} catch (RpcException e) {
			log.error("exception when calling Solana RPC", e);
			return false;
		}
		log.info("txHash: {} for {} on {} @ {} SOL", txHash, accountListing, account, price);
		return true;
	}

	public String findTokenAccount(Account account, AccountListing accountListing) {
		try {
			return tokenAccountFinder.findTokenAccount(accountListing.getMintAddress(), account.getAddress());
		} catch (RuntimeException e) {
			return null;
		}
	}
}
