package com.kang.worker;

import com.alibaba.fastjson.JSONObject;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.kang.common.AppException;
import com.kang.common.GlobalVariables;
import com.kang.helper.GoogleSheetHelper;
import com.kang.model.MyOnlineSheet;
import com.kang.model.MyRow;
import com.kang.model.MySheet;
import com.kang.model.MySpreadSheet;
import com.kang.util.LoggerUtil;
import com.kang.util.SheetRadixUtil;
import com.kang.util.VmUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author kang
 */
public abstract class AbstractWorker {

    private static final Pattern SHEET_CELL_REF_PATTERN = Pattern.compile("\\{(\\w+)\\}");
    private static final Pattern SHEET_CELL_REF_FOREIGN_PATTERN = Pattern.compile("'([^']+)'!\\{(\\w+)\\}");

    private static final String DEFAULT_SHEET_HEADER_PATTERN = "dd/MM/yy EEE";
    protected static Map<String, MySpreadSheet> sheets;
    protected static Sheets sheetsService;
    protected String taskName;
    protected Date processDate;
    protected MySpreadSheet currentSpreadSheet;

    protected MySheet currentSheet;

    protected String currentHeader;
    /**
     * need to replace the function ids
     * It indicates that the system will fill all function cell when ids is null
     * It indicates that the system will not fill function cell when ids length is 0
     * <p>
     * Default : we don't fill function cell
     */
    protected List<String> functionRowIds = new ArrayList<>();
    private Set<String> scopes;
    private boolean initEnvFlag = false;

    public AbstractWorker(String taskName, Date processDate, String... scopes) {
        this.taskName = taskName;
        this.processDate = processDate;
        this.scopes = Arrays.stream(scopes).collect(Collectors.toSet());
    }

    public static void init(Map<String, MySpreadSheet> sheets, Sheets sheetsService) {
        AbstractWorker.sheets = sheets;
        AbstractWorker.sheetsService = sheetsService;
    }

    /**
     * work name
     *
     * @return
     */
    public String name() {
        return this.getClass().getSimpleName();
    }

    /**
     * the spread sheet name
     *
     * @return
     */
    public abstract String spreadSheet();

    /**
     * the sheet name
     *
     * @return
     */
    public abstract String sheetName();

    protected String headerPattern() {
        return DEFAULT_SHEET_HEADER_PATTERN;
    }

    /**
     * contain all if the scopes is empty, otherwise must be contained in scopes
     *
     * @param scope
     * @return
     */
    public final boolean containScope(String scope) {
        if (this.scopes.isEmpty()) {
            return true;
        }
        return this.scopes.contains(scope);
    }

    protected final void addFunctionRowIds(String... rowIds) {
        if (Objects.isNull(this.functionRowIds)) {
            this.functionRowIds = new LinkedList<>();
        }
        Collections.addAll(this.functionRowIds, rowIds);
    }


    /**
     * the entrance of doing work
     *
     * @return
     */
    public boolean process() {

        if (!this.initEnvFlag) {
            this.currentSpreadSheet = sheets.get(spreadSheet());
            if (Objects.isNull(this.currentSpreadSheet)) {
                throw new RuntimeException(String.format("Not found spread sheet settings : %s", spreadSheet()));
            }
            this.currentSheet = this.currentSpreadSheet.getSheets().get(sheetName());
            if (Objects.isNull(this.currentSheet)) {
                throw new RuntimeException(String.format("Missing sheet[%s] settings in spreadsheet[%s]", sheetName(), spreadSheet()));
            }
            if (VmUtil.empty(this.currentSheet.getRows())) {
                throw new RuntimeException(String.format("Missing row settings in sheet[%s] of spreadsheet[%s]", sheetName(), spreadSheet()));
            }
            this.currentHeader = VmUtil.formatDate(this.processDate, this.headerPattern());
            if (StringUtils.isBlank(currentHeader)) {
                throw new RuntimeException(String.format("Invalid header pattern in worker[%s]", this.name()));
            }
            this.initEnvFlag = true;
        }

        try {
            // 0. get all sheets regarding of the work
            Map<String, MySheet> associatedSheets = this.getAssociatedSheets();
            LoggerUtil.printWorkerDebug(this.taskName, this.name(), "Associated sheets: %s", JSONObject.toJSONString(associatedSheets.keySet()));

            // 1. validate whether the configuration matches the sheet online and fill the row index
            Map<String, MyOnlineSheet> onlineSheets = this.validateAndFillSheetFormat(associatedSheets);
            LoggerUtil.printWorkerDebug(this.taskName, this.name(), "online sheet format: %s", JSONObject.toJSONString(onlineSheets));

            // 2. get or append column label
            List<String> headers = onlineSheets.get(sheetName()).getHeaders();
            String column;
            int index = headers.lastIndexOf(this.currentHeader);
            if (index == -1) {
                column = SheetRadixUtil.nextAlphabeticIndex(headers.size());
                GoogleSheetHelper.appendValuesInColumn(sheetsService, currentSpreadSheet.getSheetId(), sheetName(), column, Collections.singletonList(this.currentHeader));
            } else {
                column = SheetRadixUtil.toAlphabeticIndex(index + 1);
            }
            LoggerUtil.printWorkerInfo(this.taskName, this.name(), "The processing column: %s", column);

            final Map<String, Object> values = new LinkedHashMap<>();

            // 3. append statistic values
            this.assembleValues(values);

            // 4. append function values
            this.appendFunctionValues(column, onlineSheets, values);

            LoggerUtil.printWorkerDebug(this.taskName, this.name(), "origin values: %s", JSONObject.toJSONString(values, true));

            if (values.isEmpty()) {
                throw new AppException("There is no data to update");
            }

            // 5. convert values to google batch update request
            Map<String, Map<Integer, Object>> googleValues = this.convert2GoogleValues(column, values, onlineSheets.get(sheetName()));

            LoggerUtil.printWorkerDebug(this.taskName, this.name(), "cell values: %s", JSONObject.toJSONString(googleValues, true));

            if (GlobalVariables.DRY_RUN_MODE) {
                LoggerUtil.printWorkerDebug(this.taskName, this.name(), "Do not update sheet, only test!!");
                return true;
            }

            // 6. batch update value to sheet
            BatchUpdateValuesResponse response = GoogleSheetHelper.batchUpdateValues(sheetsService, this.currentSpreadSheet.getSheetId(), sheetName(), googleValues);

            LoggerUtil.printWorkerDebug(this.taskName, this.name(), "batch update response : %s", response);

            return true;
        } catch (AppException e) {
            LoggerUtil.printError("error message: %s", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * assemble values by url or database
     *
     * @param values
     * @return
     */
    protected abstract void assembleValues(final Map<String, Object> values);


    /**
     * get all associated sheets
     *
     * @return
     */
    private Map<String, MySheet> getAssociatedSheets() {
        // only the max depth is 2
        final Set<String> sheetNames = new HashSet<>(2);
        sheetNames.add(this.sheetName());
        this.iterateRowAndFindSheet(this.currentSheet, sheetNames);

        final Map<String, MySheet> result = new HashMap<>(sheetNames.size());
        sheetNames.forEach(s -> result.put(s, this.currentSpreadSheet.getSheets().get(s)));
        return result;
    }

    private void iterateRowAndFindSheet(final MySheet sheet, final Set<String> sheetNames) {
        for (MyRow row : sheet.getRows()) {
            if (StringUtils.isNotBlank(row.getValue())) {
                Matcher matcher = SHEET_CELL_REF_FOREIGN_PATTERN.matcher(row.getValue());
                while (matcher.find()) {
                    String s = matcher.group(1);
                    if (sheetNames.add(s)) {
                        iterateRowAndFindSheet(this.currentSpreadSheet.getSheets().get(s), sheetNames);
                    }
                }
            }
        }
    }

    /**
     * check whether the configuration matches the sheet online and fill the row index
     *
     * @param associatedSheets
     * @return
     */
    private Map<String, MyOnlineSheet> validateAndFillSheetFormat(final Map<String, MySheet> associatedSheets) {
        final Map<String, MyOnlineSheet> result = new HashMap<>(associatedSheets.size());
        List<String> captions, headers;
        Map<String, MyOnlineSheet.Row> onlineRows;
        int index;
        for (Map.Entry<String, MySheet> entry : associatedSheets.entrySet()) {
            captions = GoogleSheetHelper.rowCaptions(sheetsService, this.currentSpreadSheet.getSheetId(), entry.getKey());
            LoggerUtil.printWorkerDebug(this.taskName, this.name(), "online sheet[%s] captions: %s", entry.getKey(), JSONObject.toJSONString(captions));

            headers = GoogleSheetHelper.columnHeaders(sheetsService, this.currentSpreadSheet.getSheetId(), entry.getKey());
            LoggerUtil.printWorkerDebug(this.taskName, this.name(), "online sheet[%s] headers: %s", entry.getKey(), JSONObject.toJSONString(headers));

            if (VmUtil.empty(captions) || VmUtil.empty(headers)) {
                throw new AppException(String.format("Fail to get sheet format: %s in %s", entry.getKey(), spreadSheet()));
            }
            onlineRows = new HashMap<>(entry.getValue().getRows().size());
            for (MyRow row : entry.getValue().getRows()) {
                index = captions.indexOf(row.getCaption());
                if (index == -1) {
                    throw new AppException(String.format("There is no the caption[%s] in [%s] of [%s]", row.getCaption(), entry.getKey(), spreadSheet()));
                }
                onlineRows.put(row.getId(), MyOnlineSheet.Row.of(index + 1, row));
            }
            result.put(entry.getKey(), new MyOnlineSheet(headers, onlineRows));
        }
        return result;
    }

    /**
     * append function cell to values collection, replace macro with real value
     *
     * @param column
     * @param sheets
     * @param values
     */
    private void appendFunctionValues(String column, final Map<String, MyOnlineSheet> sheets, final Map<String, Object> values) {
        List<String> rowIds = this.functionRowIds;
        if (Objects.isNull(rowIds)) {
            rowIds = this.currentSheet.getRows().stream().filter(s -> StringUtils.isNotBlank(s.getValue())).map(MyRow::getId).collect(Collectors.toList());
        }

        String function;
        for (String rowId : rowIds) {
            function = this.replaceMacroInFunction(column, rowId, sheets);
            if (StringUtils.isNotBlank(function)) {
                values.put(rowId, function);
            }
        }
    }

    private String replaceMacroInFunction(final String column, final String rowId, final Map<String, MyOnlineSheet> sheets) {
        MyOnlineSheet currentSheet = sheets.get(this.sheetName());
        MyOnlineSheet.Row currentRow = currentSheet.getRows().get(rowId);
        if (StringUtils.isBlank(currentRow.getRow().getValue())) {
            return null;
        }

        String function = currentRow.getRow().getValue().trim();
        Matcher matcher = SHEET_CELL_REF_FOREIGN_PATTERN.matcher(function);
        int index;
        String tColumn;
        MyOnlineSheet tSheet;
        MyOnlineSheet.Row tRow;
        boolean replaceFlag = false;
        while (matcher.find()) {
            tSheet = sheets.get(matcher.group(1));
            tRow = tSheet.getRows().get(matcher.group(2));
            if (Objects.isNull(tRow)) {
                throw new AppException(String.format("There is no rowId[%s] in sheet[%s] of [%s]", matcher.group(2), matcher.group(1), spreadSheet()));
            }
            index = tSheet.getHeaders().lastIndexOf(this.currentHeader);
            if (index == -1) {
                throw new AppException(String.format("There is no column[%s] in sheet[%s] of [%s]", column, matcher.group(1), spreadSheet()));
            } else {
                tColumn = SheetRadixUtil.toAlphabeticIndex(index + 1);
            }
            function = function.replace(matcher.group(0), "'" + matcher.group(1) + "'!" + tColumn + tRow.getIndex());
            replaceFlag = true;
        }
        matcher = SHEET_CELL_REF_PATTERN.matcher(function);
        while (matcher.find()) {
            tRow = currentSheet.getRows().get(matcher.group(1));
            if (Objects.isNull(tRow)) {
                throw new AppException(String.format("There is no rowId[%s] in sheet[%s] of [%s]", matcher.group(1), this.sheetName(), spreadSheet()));
            }
            function = function.replace(matcher.group(0), column + tRow.getIndex());
            replaceFlag = true;
        }
        return replaceFlag ? "=" + function : function;
    }

    /**
     * convert values to google batch update request
     *
     * @param column
     * @param values
     * @param sheet
     * @return
     */
    private Map<String, Map<Integer, Object>> convert2GoogleValues(String column, Map<String, Object> values, MyOnlineSheet sheet) {
        Map<Integer, Object> vals = new HashMap<>(values.size());

        MyOnlineSheet.Row row;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            row = sheet.getRows().get(entry.getKey());
            if (Objects.nonNull(row)) {
                vals.put(row.getIndex(), entry.getValue());
            }
        }
        Map<String, Map<Integer, Object>> result = new HashMap<>(1);
        result.put(column, vals);
        return result;
    }

}
