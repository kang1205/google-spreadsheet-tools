package com.kang.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author kang
 */
public class SheetRadixUtil {

    private static List<Character> ALPHABET_LIST = Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');

    /**
     * from 1
     *
     * @param index
     * @return
     */
    public static String toAlphabeticIndex(int index) {
        index--;
        if (index < 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        do {
            sb.append(ALPHABET_LIST.get(index % 26));
            index /= 26;
        } while (--index >= 0);
        return sb.reverse().toString();
    }

    /**
     * from 1
     *
     * @param alphabeticIndex
     * @return
     */
    public static int toNumericIndex(String alphabeticIndex) {
        if (StringUtils.isBlank(alphabeticIndex)) {
            return -1;
        }
        char[] chars = alphabeticIndex.toCharArray();
        int result = 0, t;
        for (int i = chars.length - 1, j = 0; i >= 0; i--, j++) {
            t = ALPHABET_LIST.indexOf(chars[i]);
            if (t < 0) {
                return -1;
            }
            if (j > 0) {
                t++;
            }
            result += t * Math.pow(26, j);
        }
        return result + 1;
    }

    public static String nextAlphabeticIndex(String current) {
        return nextAlphabeticIndex(toNumericIndex(current));
    }

    public static String nextAlphabeticIndex(int current) {
        return current >= 0 ? toAlphabeticIndex(current + 1) : null;
    }

    public static String preAlphabeticIndex(String current) {
        return preAlphabeticIndex(toNumericIndex(current));
    }

    public static String preAlphabeticIndex(int current) {
        return current > 0 ? toAlphabeticIndex(current - 1) : null;
    }

}
