package com.kang.model;

import java.util.Map;

/**
 * @author kang
 */
public class MySpreadSheet {

    private String sheetId;

    private Map<String, MySheet> sheets;

    public String getSheetId() {
        return sheetId;
    }

    public void setSheetId(String sheetId) {
        this.sheetId = sheetId;
    }

    public Map<String, MySheet> getSheets() {
        return sheets;
    }

    public void setSheets(Map<String, MySheet> sheets) {
        this.sheets = sheets;
    }
}
