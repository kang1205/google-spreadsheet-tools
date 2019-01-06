package com.kang;

import com.alibaba.fastjson.JSONObject;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.kang.helper.GoogleSheetHelper;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class GoogleSheetHelperTests {

    private String spreadsheetId = "10hUlIP6-3Xj2ciNd0zdw7UmskqIxlo-hLBKQ0WYugJE";
    private String sheetName = "Sheet1";

    @Test
    public void testSheetService() throws IOException {
        Sheets service = GoogleSheetHelper.sheetsService();
        String range = "Sheet1!1:1";
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        System.out.println(JSONObject.toJSONString(values));

    }

    @Test
    public void testColumnHeader() {
        System.out.println(JSONObject.toJSONString(GoogleSheetHelper.columnHeaders(GoogleSheetHelper.sheetsService(), spreadsheetId, sheetName)));
    }

    @Test
    public void testRowCaption() {
        System.out.println(JSONObject.toJSONString(GoogleSheetHelper.rowCaptions(GoogleSheetHelper.sheetsService(), spreadsheetId, sheetName)));
    }

    @Test
    public void testAppendDataInColumn() {
        List<Object> list = new ArrayList<>();
        list.add("n21111111111ss");
        list.add("n3ssd");
        list.add(false);
        list.add("2019/01dd/01");
        list.add("");
        list.add("endvddd3");
        Sheets sheets = GoogleSheetHelper.sheetsService();
        AppendValuesResponse response = GoogleSheetHelper.appendValuesInColumn(sheets, spreadsheetId, sheetName, "AA", list);
        System.out.println(response);
    }

    @Test
    public void testUpdateDataInColumn() {
        List<Object> list = new ArrayList<>();
        list.add("n42211");
        list.add("n5211111");
        list.add(false);
        list.add("2019/011111/01");
        list.add(null);
        list.add("endvsss1115");
        Sheets sheets = GoogleSheetHelper.sheetsService();
        UpdateValuesResponse response = GoogleSheetHelper.updateValuesInColumn(sheets, spreadsheetId, sheetName, "I", list);
        System.out.println(response);
    }

    @Test
    public void testBatchUpdate() {

        Map<Integer, Object> columnI = new HashMap<>();
        columnI.put(2, "i-2");
        columnI.put(5, "");
        columnI.put(3, "i-3");
        columnI.put(10, "i-10");

        Map<Integer, Object> columnK = new HashMap<>();
        columnK.put(1, "k-1");
        columnK.put(11, "k-11");
        columnK.put(12, "k-12");

        Map<String, Map<Integer, Object>> data = new HashMap<>();
        data.put("I", columnI);
        data.put("K", columnK);

        Sheets sheets = GoogleSheetHelper.sheetsService();
        BatchUpdateValuesResponse response = GoogleSheetHelper.batchUpdateValues(sheets, spreadsheetId, sheetName, data);
        System.out.println(response);
    }

    @Test
    public void testAddColumn() {
        Sheets sheets = GoogleSheetHelper.sheetsService();
        AppendValuesResponse response = GoogleSheetHelper.addEmptyColumn(sheets, spreadsheetId, sheetName, "J");
        System.out.println(response);
    }
}
