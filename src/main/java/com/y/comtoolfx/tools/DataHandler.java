package com.y.comtoolfx.tools;

import java.nio.charset.StandardCharsets;

/**
 * @author Y
 * @version 1.0
 * @date 2024/3/14 17:10
 */
public class DataHandler {
    public static byte[] getByte(String message, String deli) {
        if (message.isEmpty()) {
            return new byte[0];
        }
        byte[] de = getByte(deli);
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        if (de != null) {
            byte[] re = new byte[bytes.length + de.length];
            System.arraycopy(de, 0, re, 0, de.length);
            System.arraycopy(bytes, 0, re, de.length, bytes.length);
            return re;
        } else {
            return bytes;
        }
    }

    private static byte[] getByte(String deli) {
        if (deli.isEmpty()) {
            return null;
        }
        String[] split = deli.split(" ");
        byte[] bytes = new byte[split.length];
        for (int i = 0; i < bytes.length; i++) {
            try {
                bytes[i] = Byte.parseByte(split[i], 16);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return bytes;
    }
}
