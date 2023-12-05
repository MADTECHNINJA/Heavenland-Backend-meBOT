package io.heavenland.mebot.context.market_data;

import io.heavenland.mebot.domain.NftCollection;
import org.junit.jupiter.api.Test;

public class MarketDataServiceTest {

    @Test
    public void testSubscription() throws InterruptedException {
        var service = new MarketDataService();
        service.subscribeCollection(NftCollection.HL_LOYALTY);

        Thread.sleep(30_000);

        var listings = service.getListings(NftCollection.HL_LOYALTY);
        var size = listings.size();
        System.out.println(size);
    }
}
