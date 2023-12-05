package io.heavenland.mebot.wallets;

import io.heavenland.mebot.context.wallet.WalletFacade;
import io.heavenland.mebot.domain.LiquidityPool;
import io.heavenland.mebot.domain.Token;
import io.heavenland.mebot.domain.Wallet;
import io.heavenland.tools.solana.helpers.SPLTokenInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.p2p.solanaj.rpc.Cluster;

import java.math.BigDecimal;
import java.util.List;

public class WalletFacadeTest {


    @Test
    public void testGetBalance() {
        var facade = new WalletFacade();
        var balance = facade.getSolBalance("eQw5ArmXWvqmQweTU2CLvV51fUmk8d7hRcSFXs5gu93", Cluster.MAINNET); // private key in src/resources/keys - never use for real assets
        Assertions.assertEquals(0, balance);
    }

    @Test
    public void testUpdateBalances() {
        var facade = new WalletFacade();
        var wallet = new Wallet("eQw5ArmXWvqmQweTU2CLvV51fUmk8d7hRcSFXs5gu93");
        var wallets = List.of(wallet);

        Assertions.assertNull(wallet.getBalance());

        facade.updateBalances(wallets);

        Assertions.assertNotNull(wallet.getBalance());
        Assertions.assertEquals(BigDecimal.ZERO, wallet.getBalance());
    }

    @Test
    public void testTokenAccounts() {
        WalletFacade facade = new WalletFacade();
        Wallet wallet = new Wallet(LiquidityPool.RAY_V4_HTO_USDC.getAddress());

        List<SPLTokenInfo> tokenAccounts = facade.getTokenAccountsInfo(wallet.getAddress(), Token.HTO.getAddress(), Cluster.MAINNET);
        tokenAccounts.forEach(tokenInfo -> System.out.println(tokenInfo.getAmount()));

        Assertions.assertNotNull(tokenAccounts);
    }
}
