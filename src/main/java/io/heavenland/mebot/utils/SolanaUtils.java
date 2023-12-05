package io.heavenland.mebot.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

public class SolanaUtils {

    public static BigDecimal lamportsToSolana(long lamports) {
        var result = new BigDecimal(lamports);
        return result.divide(new BigDecimal("1000000000"), 9, RoundingMode.UNNECESSARY).stripTrailingZeros();
    }

    public static byte uByteToByte(int uByte) {
        if (uByte < 127) {
            return (byte) uByte;
        }
        return (byte) (uByte - 256);
    }

    public static byte[] uBytesToBytes(int[] uBytes) {
        byte[] bytes = new byte[uBytes.length];
        for (int i = 0; i < uBytes.length; i++) {
            bytes[i] = uByteToByte(uBytes[i]);
        }
        return bytes;
    }

    public static int[] jsonArrayToIntArray(String jsonByteArray) {
        var pkString = jsonByteArray.substring(1, jsonByteArray.length() - 1);
        return Arrays.stream(pkString.split(",")).mapToInt(Integer::parseInt).toArray();
    }
}
