package io.heavenland.mebot.clients.magiceden;

import io.heavenland.mebot.domain.NftCollection;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class MagicEdenClientTest {

    @Test
    public void testListingsPage() {
        var loader = MagicEdenClient.instance();
        var result = loader.getListingsPage(NftCollection.HL_LOYALTY, 1, 0);

        System.out.println("Number of items: " + result.size());
        System.out.println(result.get(0).tokenMint + " at " + result.get(0).price + " SOL");
    }

    @Test
    //@Disabled
    public void testListings() {
        var loader = MagicEdenClient.instance();
        var result = loader.getListings(NftCollection.HL_LOYALTY);

        System.out.println("Number of items: " + result.size());
        System.out.println(result.get(0).tokenMint + " at " + result.get(0).price + " SOL");
    }

    @Test
    public void testWalletTokensPage() {
        var loader = MagicEdenClient.instance();
        var result = loader.getWalletTokensPage("eQw5ArmXWvqmQweTU2CLvV51fUmk8d7hRcSFXs5gu93", 1, 0);

        System.out.println("Number of items: " + result.size());
    }

}
