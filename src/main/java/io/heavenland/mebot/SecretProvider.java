package io.heavenland.mebot;

import io.heavenland.mebot.domain.Account;
import io.heavenland.mebot.utils.SolanaUtils;

// singleton
public class SecretProvider {

    private static SecretProvider INSTANCE;

    private final Secrets secrets = new Secrets();

    private SecretProvider() {
    }

    public static SecretProvider instance() {
        if (INSTANCE == null) {
            INSTANCE = new SecretProvider();
        }
        return INSTANCE;
    }

    public String getMagicEdenAPIKey() {
        return secrets.getMagicEdenAPIKey();
    }

    public org.p2p.solanaj.core.Account getPrivateKey(Account account) {
        var pkString = secrets.getPrivateKeys().get(account);

        if (pkString == null) {
            throw new RuntimeException("No private key for account " + account);
        }

        var ubytes = SolanaUtils.jsonArrayToIntArray(pkString);
        var pk = SolanaUtils.uBytesToBytes(ubytes);
        return new org.p2p.solanaj.core.Account(pk);
    }
}
