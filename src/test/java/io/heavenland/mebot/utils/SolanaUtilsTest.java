package io.heavenland.mebot.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.p2p.solanaj.core.Account;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Stream;

public class SolanaUtilsTest {

    @ParameterizedTest
    @MethodSource("values")
    public void testLamportsToSolana(
            BigDecimal expected,
            long lamports
    ) {
        var result = SolanaUtils.lamportsToSolana(lamports);
        Assertions.assertEquals(0, expected.compareTo(result));
    }

    static Stream<Arguments> values() {
        return Stream.of(
                Arguments.arguments(new BigDecimal("1"), 1000000000),
                Arguments.arguments(new BigDecimal("1.000000001"), 1000000001)

        );
    }

    @Test
    public void testJsonPKConversion() {
        String privateKeyString = "[162,171,8,4,68,74,208,183,109,242,140,149,205,120,36,131,0,232,64,118,202,93,18,249,87,174,210,90,141,56,222,2,9,149,140,220,208,17,205,139,199,179,230,5,179,130,17,115,133,245,233,59,201,111,106,135,186,199,127,153,73,69,236,58]";

        var uBytes = SolanaUtils.jsonArrayToIntArray(privateKeyString);
        var privateKey = SolanaUtils.uBytesToBytes(uBytes);
        Account solAccount = new Account(privateKey);

        Assertions.assertEquals("eQw5ArmXWvqmQweTU2CLvV51fUmk8d7hRcSFXs5gu93", solAccount.getPublicKey().toBase58());
    }
}
