package io.heavenland.mebot.context.account;

import io.heavenland.mebot.domain.Account;
import io.heavenland.mebot.domain.NftCollection;
import org.junit.jupiter.api.Test;

public class AccountServiceTest {

    @Test
    public void testSubscription() throws InterruptedException {
        var service = new AccountService();
        service.subscribeAccount(Account.PG1);

        Thread.sleep(10_000);

        var balance = service.getSolBalance(Account.PG1);
        var listings = service.getListings(Account.PG1, NftCollection.HL_LOYALTY);
        System.out.println("Balance: " + balance);
        System.out.println("Listings: " + listings);
    }
}
