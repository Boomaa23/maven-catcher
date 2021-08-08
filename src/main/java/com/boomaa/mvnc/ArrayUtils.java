package com.boomaa.mvnc;

import java.lang.reflect.Array;

public class ArrayUtils {
    public static byte[] sliceArr(byte[] array, int start, int end) {
        try {
            byte[] out = new byte[end - start];
            System.arraycopy(array, start, out, 0, out.length);
            return out;
        } catch (ArrayIndexOutOfBoundsException | NegativeArraySizeException ignored) {
        }
        return new byte[0];
    }

    public static byte[] concat(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;

        byte[] c = (byte[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }
}
