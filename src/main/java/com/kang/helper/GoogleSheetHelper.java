package com.kang.helper;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import com.kang.common.AppException;
import com.kang.util.VmUtil;

import java.io.*;
import java.util.*;

/**
 * @author kang
 */
public class GoogleSheetHelper {

    private static final String APPLICATION_NAME = "Muslim Insight";
    private static final String CREDENTIALS_FILE_PATH = "config/credentials.json";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String VALUE_INPUT_OPTION = "USER_ENTERED";

    private static Credential authorize() {
        try (Reader reader = new InputStreamReader(new FileInputStream(new File(CREDENTIALS_FILE_PATH)))) {
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), reader);

            List<String> scopes = Collections.singletonList(SheetsScopes.SPREADSHEETS);

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), clientSecrets,
                    scopes).setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline").build();

            return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        } catch (Exception e) {
            throw new RuntimeException("Fail to authorize google account", e);
        }
    }

    public static Sheets sheetsService() {
        try {
            return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
                    authorize()).setApplicationName(APPLICATION_NAME).build();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static List<String> rowCaptions(Sheets sheets, String spreadSheetId, String sheetName) {
        String range = "'" + sheetName + "'!A:A";
        try {
            List<List<Object>> values = sheets.spreadsheets().values().get(spreadSheetId, range).execute().getValues();
            if (Objects.isNull(values)) {
                return null;
            }
            final List<String> result = new ArrayList<>(VmUtil.size(values));
            values.stream().map(t -> VmUtil.get(t, 0)).map(VmUtil::toString).forEach(result::add);
            return result;
        } catch (IOException e) {
            throw new AppException("Fail to read row caption!", e);
        }
    }

    public static List<String> columnHeaders(Sheets sheets, String spreadSheetId, String sheetName) {
        String range = "'" + sheetName + "'!1:1";
        try {
            List<List<Object>> values = sheets.spreadsheets().values().get(spreadSheetId, range).execute().getValues();
            if (VmUtil.empty(values)) {
                return null;
            }
            List<Object> columnValues = VmUtil.get(values, 0);
            if (VmUtil.empty(columnValues)) {
                return null;
            }
            final List<String> result = new ArrayList<>(VmUtil.size(columnValues));
            columnValues.stream().map(VmUtil::toString).forEach(result::add);
            return result;
        } catch (Exception e) {
            throw new AppException("Fail to read column header!", e);
        }
    }

    /**
     * insert one empty column
     *
     * @param sheets
     * @param spreadSheetId
     * @param sheetName
     * @param column
     * @return
     */
    public static AppendValuesResponse addEmptyColumn(Sheets sheets, String spreadSheetId, String sheetName, String column) {
        return appendValuesInColumn(sheets, spreadSheetId, sheetName, column, Collections.singletonList(""));
    }

    /**
     * append data from 1st not blank cell in the column
     *
     * @param sheets
     * @param spreadSheetId
     * @param sheetName
     * @param column
     * @param data
     * @return
     */
    public static AppendValuesResponse appendValuesInColumn(Sheets sheets, String spreadSheetId, String sheetName, String column, List<Object> data) {
        try {
            String range = "'" + sheetName + "'!" + column + 1;
            List<List<Object>> values = Collections.singletonList(blankWhenNull(data));
            ValueRange body = new ValueRange().setMajorDimension("COLUMNS").setValues(values);
            return sheets.spreadsheets().values().append(spreadSheetId, range, body)
                    .setValueInputOption(VALUE_INPUT_OPTION)
                    .setInsertDataOption("OVERWRITE")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
            throw new AppException(String.format("Fail to update sheets[%s]", sheetName));
        }
    }

    /**
     * update data from 1st row
     *
     * @param sheets
     * @param spreadSheetId
     * @param sheetName
     * @param column
     * @param data
     * @return
     */
    public static UpdateValuesResponse updateValuesInColumn(Sheets sheets, String spreadSheetId, String sheetName, String column, List<Object> data) {
        try {
            String range = "'" + sheetName + "'!" + column + 1;
            List<List<Object>> values = Collections.singletonList(blankWhenNull(data));
            ValueRange body = new ValueRange().setMajorDimension("COLUMNS").setValues(values);
            return sheets.spreadsheets().values().update(spreadSheetId, range, body)
                    .setValueInputOption(VALUE_INPUT_OPTION)
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
            throw new AppException(String.format("Fail to update sheets[%s]", sheetName));
        }
    }

    /**
     * batch update values
     *
     * @param sheets
     * @param spreadSheetId
     * @param sheetName
     * @param data          (k1, k2, value) -> column, rowIndex, value
     * @return
     */
    public static BatchUpdateValuesResponse batchUpdateValues(Sheets sheets, String spreadSheetId, String sheetName, Map<String, Map<Integer, Object>> data) {
        try {
            List<ValueRange> values = new LinkedList<>();
            data.forEach((col, rows) -> rows.forEach((row, val) -> values.add(new ValueRange().setRange("'" + sheetName + "'!" + col + row).setMajorDimension("COLUMNS").setValues(Collections.singletonList(Collections.singletonList(val))))));

            BatchUpdateValuesRequest body = new BatchUpdateValuesRequest();
            body.setValueInputOption(VALUE_INPUT_OPTION);
            body.setData(values);
            return sheets.spreadsheets().values().batchUpdate(spreadSheetId, body).execute();
        } catch (IOException e) {
            e.printStackTrace();
            throw new AppException(String.format("Fail to update sheets[%s]", sheetName));
        }
    }

    private static List<Object> blankWhenNull(List<Object> list) {
        for (int i = 0; i < list.size(); i++) {
            if (Objects.isNull(list.get(i))) {
                list.set(i, "");
            }
        }
        return list;
    }

}
