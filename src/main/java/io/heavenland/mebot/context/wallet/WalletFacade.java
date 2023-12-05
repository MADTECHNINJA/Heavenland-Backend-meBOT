package io.heavenland.mebot.context.wallet;

import io.heavenland.mebot.Constants;
import io.heavenland.mebot.RpcClientFactory;
import io.heavenland.mebot.domain.Token;
import io.heavenland.mebot.utils.SolanaUtils;
import io.heavenland.mebot.domain.Wallet;
import io.heavenland.tools.solana.helpers.SPLTokenInfo;
import jakarta.ejb.Stateless;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Stateless
@Slf4j
public class WalletFacade {

	public long getSolBalance(String walletAddress, Cluster cluster) {
		var client = RpcClientFactory.create(cluster);
		try {
			return client.getApi().getBalance(new PublicKey(walletAddress));
		} catch (RpcException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot load balance for wallet " + walletAddress);
		}
	}

	public void updateBalances(Collection<Wallet> wallets) {
		wallets.forEach(wallet -> {
			long balance;
			try {
				balance = getSolBalance(wallet.getAddress(), Constants.CLUSTER);
			} catch (RuntimeException e) {
				log.warn("{} when reading balance at {}", e.getMessage(), wallet.getAddress());
				balance = 0;
			}
			wallet.setBalance(SolanaUtils.lamportsToSolana(balance));

			for (Token token : wallet.getTokens()) {
				wallet.setBalance(token, getTokenBalance(wallet.getAddress(), token, Constants.CLUSTER));
			}
		});
	}

	public BigDecimal getTokenBalance(String walletAddress, Token token, Cluster cluster) {
		List<SPLTokenInfo> tokenInfos = getTokenAccountsInfo(walletAddress, token.getAddress(), cluster);
		if (CollectionUtils.isEmpty(tokenInfos)) {
			return BigDecimal.ZERO;
		}
		BigDecimal balance = BigDecimal.ZERO;
		for (SPLTokenInfo tokenInfo : tokenInfos) {
			balance = balance.add(tokenInfo.getAmount());
		}
		return balance;
	}

	public List<SPLTokenInfo> getTokenAccountsInfo(String walletAddress, String tokenAddress, Cluster cluster) {
		var client = RpcClientFactory.create(cluster);
		var result = new ArrayList<SPLTokenInfo>();
		try {
			var accounts = client.getApi().getTokenAccountsByOwner(new PublicKey(walletAddress), new PublicKey(tokenAddress), null);
			accounts.getValue().forEach(value -> {
				var info = SPLTokenInfo.from(value);
				result.add(info);
			});
		} catch (RpcException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot load token accounts info for wallet " + walletAddress);
		}
		return result;
	}
}
