package com.kang;

import com.kang.util.SheetRadixUtil;
import org.junit.Assert;
import org.junit.Test;

public class SheetRadixUtilTests {

    @Test
    public void testToAlphabeticRadix() {
        Assert.assertEquals("A", SheetRadixUtil.toAlphabeticIndex(1));
        Assert.assertEquals("Z", SheetRadixUtil.toAlphabeticIndex(26));
        Assert.assertEquals("AA", SheetRadixUtil.toAlphabeticIndex(27));
        Assert.assertEquals("AB", SheetRadixUtil.toAlphabeticIndex(28));
        Assert.assertEquals("AZ", SheetRadixUtil.toAlphabeticIndex(52));
        Assert.assertEquals("BA", SheetRadixUtil.toAlphabeticIndex(53));
    }

    @Test
    public void testToNumericIndex() {
        Assert.assertEquals(1, SheetRadixUtil.toNumericIndex("A"));
        Assert.assertEquals(26, SheetRadixUtil.toNumericIndex("Z"));
        Assert.assertEquals(27, SheetRadixUtil.toNumericIndex("AA"));
        Assert.assertEquals(28, SheetRadixUtil.toNumericIndex("AB"));
        Assert.assertEquals(52, SheetRadixUtil.toNumericIndex("AZ"));
        Assert.assertEquals(53, SheetRadixUtil.toNumericIndex("BA"));
        Assert.assertEquals(702, SheetRadixUtil.toNumericIndex("ZZ"));
        Assert.assertEquals(703, SheetRadixUtil.toNumericIndex("AAA"));
    }


    @Test
    public void testNextAlphabeticIndex() {
        Assert.assertEquals("B", SheetRadixUtil.nextAlphabeticIndex("A"));
        Assert.assertEquals("Z", SheetRadixUtil.nextAlphabeticIndex("Y"));
        Assert.assertEquals("AA", SheetRadixUtil.nextAlphabeticIndex("Z"));
        Assert.assertEquals("BA", SheetRadixUtil.nextAlphabeticIndex("AZ"));
        Assert.assertEquals("AAA", SheetRadixUtil.nextAlphabeticIndex("ZZ"));
        Assert.assertEquals("AAAA", SheetRadixUtil.nextAlphabeticIndex("ZZZ"));
        Assert.assertEquals("ZZA", SheetRadixUtil.nextAlphabeticIndex("ZYZ"));
    }

    @Test
    public void testPreAlphabeticIndex() {
        Assert.assertEquals(null, SheetRadixUtil.preAlphabeticIndex("A"));
        Assert.assertEquals("A", SheetRadixUtil.preAlphabeticIndex("B"));
        Assert.assertEquals("Z", SheetRadixUtil.preAlphabeticIndex("AA"));
        Assert.assertEquals("AZ", SheetRadixUtil.preAlphabeticIndex("BA"));
        Assert.assertEquals("ZZ", SheetRadixUtil.preAlphabeticIndex("AAA"));
    }

}
