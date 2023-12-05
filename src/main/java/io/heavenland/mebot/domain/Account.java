package io.heavenland.mebot.domain;

import io.heavenland.mebot.SecretProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Account {

    TEST,
    PG1,
    PG2,
    PG3,
    PG4,
    PG5,

    JL1,
    JL2,
    JL3,
    JL4,
    JL5,

    JL10,
    JL11,
    JL12,
    JL13,
    JL14,
    JL15,
    JL16,
    JL17,
    JL18,
    JL19;

    public org.p2p.solanaj.core.Account getSolanaAccount() {
        return SecretProvider.instance().getPrivateKey(this);
    }

    public String getAddress() {
        return getSolanaAccount().getPublicKey().toBase58();
    }

    // TODO add other methods ?
}
