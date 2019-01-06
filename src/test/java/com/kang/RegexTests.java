package com.kang;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTests {
    Pattern SHEET_IN_FUNCTION = Pattern.compile("'([^']+)'!");

    @Test
    public void testRegexMatcher() {
        String row = "xss3";
        Matcher matcher = SHEET_IN_FUNCTION.matcher(row);
        while (matcher.find()) {
            String s = matcher.group(1);
            System.out.println(s);
        }
    }

}
