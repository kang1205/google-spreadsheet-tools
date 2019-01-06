package com.kang;

import com.alibaba.fastjson.JSONObject;
import com.kang.helper.ResourcesHelper;
import com.kang.model.MySpreadSheet;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class LoadSourceTests {

    @Test
    public void testLoadSettings() {

        Map<String, MySpreadSheet> sheetMap = ResourcesHelper.sheets();
        System.out.println(JSONObject.toJSONString(sheetMap, true));
    }

    @Test
    public void testFileSuffixRegex() {
        String FILE_SUFFIX_REGEX = ".*\\.(json|xml)$";
        Assert.assertTrue("template-json.json".matches(FILE_SUFFIX_REGEX));
        Assert.assertTrue("template-xml.xml".matches(FILE_SUFFIX_REGEX));
        Assert.assertFalse("template.xmlbak".matches(FILE_SUFFIX_REGEX));
    }
}
