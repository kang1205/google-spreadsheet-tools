package com.kang.helper;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.kang.common.AppException;
import com.kang.model.MyRow;
import com.kang.model.MySheet;
import com.kang.model.MySpreadSheet;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author kang
 */
public class ResourcesHelper {

    private static final String SHEETS_DIR = "config/sheets";
    private static final Pattern FILE_SUFFIX_PATTERN = Pattern.compile("(.+)\\.(json|xml)$");
    private static final DocumentBuilderFactory DOCUMENT_FACTORY = DocumentBuilderFactory.newInstance();

    public static Map<String, MySpreadSheet> sheets() {
        try {
            List<Path> paths = Files.walk(Paths.get(SHEETS_DIR)).filter(Files::isRegularFile)
                    .filter(path -> FILE_SUFFIX_PATTERN.matcher(path.toFile().getName()).matches())
                    .collect(Collectors.toList());
            final Map<String, MySpreadSheet> sheets = new HashMap<>(paths.size());
            Matcher matcher;
            MySpreadSheet spreadSheet;
            for (Path path : paths) {
                try (InputStream sheetStream = new FileInputStream(path.toFile())) {
                    matcher = FILE_SUFFIX_PATTERN.matcher(path.toFile().getName());
                    if (!matcher.matches()) {
                        continue;
                    }
                    switch (matcher.group(2)) {
                        case "json":
                            spreadSheet = JSONObject.parseObject(sheetStream, Charsets.UTF_8, MySpreadSheet.class);
                            break;
                        case "xml":
                            spreadSheet = parseXml(matcher.group(1), sheetStream);
                            break;
                        default:
                            spreadSheet = null;
                            // no support now!
                            break;
                    }
                    if (Objects.nonNull(spreadSheet)) {
                        spreadSheet = sheets.put(matcher.group(1), spreadSheet);
                        if (Objects.nonNull(spreadSheet)) {
                            throw new AppException("There is a duplicate configuration for sheet[" + matcher.group(1) + "]");
                        }
                    }
                }
            }
            return sheets;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Fail to load sheets settings", e);
        }
    }

    private static MySpreadSheet parseXml(String fileName, InputStream sheetStream) throws ParserConfigurationException, IOException, SAXException {
        Element root = DOCUMENT_FACTORY.newDocumentBuilder().parse(sheetStream).getDocumentElement();

        MySpreadSheet spreadSheet = new MySpreadSheet();
        // 1. spreadsheet id
        String spreadsheetId = root.getAttribute("id");
        if (StringUtils.isBlank(spreadsheetId)) {
            throw new AppException(String.format("Missing spreadsheet id in %s file", fileName));
        }
        spreadSheet.setSheetId(spreadsheetId);

        // 2. sheets tag
        NodeList sheetsNodeList = root.getElementsByTagName("sheets");
        if (Objects.isNull(sheetsNodeList) || sheetsNodeList.getLength() == 0) {
            throw new AppException(String.format("Missing sheets tag in %s file", fileName));
        }
        NodeList sheetNodeList = ((Element) sheetsNodeList.item(0)).getElementsByTagName("sheet");
        if (Objects.isNull(sheetNodeList) || sheetNodeList.getLength() == 0) {
            throw new AppException(String.format("Missing sheet tag %s file", fileName));
        }
        Map<String, MySheet> sheets = new HashMap<>(sheetNodeList.getLength());
        spreadSheet.setSheets(sheets);

        Element sheetElement, rowsElement, rowElement;
        NodeList rowsList, rowList;
        MySheet sheet;
        MyRow row;
        List<MyRow> rows;
        String sheetName, rowId, rowCaption, rowValue;
        Set<String> rowIds;
        for (int i = 0; i < sheetNodeList.getLength(); i++) {
            // 3. one sheet
            sheetElement = (Element) sheetNodeList.item(i);
            sheetName = sheetElement.getAttribute("name");
            if (StringUtils.isBlank(sheetName)) {
                throw new AppException(String.format("Missing sheet name : NO[%s]", i + 1));
            }
            sheet = new MySheet();
            if (Objects.nonNull(sheets.put(sheetName, sheet))) {
                throw new AppException(String.format("Duplicate sheet: %s", sheetName));
            }
            rowsList = sheetElement.getElementsByTagName("rows");
            if (Objects.isNull(rowsList) || rowsList.getLength() == 0) {
                throw new AppException(String.format("Missing rows tag in sheet[%s] of spreadsheet[%s]", sheetName, fileName));
            }
            rowsElement = (Element) rowsList.item(0);
            rowList = rowsElement.getElementsByTagName("row");

            if (Objects.isNull(rowList) || rowList.getLength() == 0) {
                throw new AppException(String.format("Missing row tag in sheet[%s] of spreadsheet[%s]", sheetName, fileName));
            }

            rows = new ArrayList<>(rowList.getLength());
            sheet.setRows(rows);

            // it is used to check whether there is duplicate row id
            rowIds = new HashSet<>(rowList.getLength());

            for (int j = 0; j < rowList.getLength(); j++) {
                // 4. one row
                rowElement = (Element) rowList.item(j);

                row = new MyRow();
                rows.add(row);

                rowId = rowElement.getAttribute("id");
                rowCaption = rowElement.getAttribute("caption");
                rowValue = rowElement.getAttribute("value");
                if (StringUtils.isBlank(rowId)) {
                    throw new AppException(String.format("Missing row id on row[%d] in sheet[%s] of spreadsheet[%s]", j + 1, sheetName, fileName));
                }
                if (StringUtils.isBlank(rowCaption)) {
                    throw new AppException(String.format("Missing row caption on row[%d] in sheet[%s] of spreadsheet[%s]", j + 1, sheetName, fileName));
                }
                if (!rowIds.add(rowId)) {
                    throw new AppException(String.format("Duplicate row id[%s] on row[%d] in sheet[%s] of spreadsheet[%s]", rowId, j + 1, sheetName, fileName));
                }
                row.setCaption(rowCaption);
                row.setId(rowId);
                row.setValue(rowValue);
            }
        }
        return spreadSheet;
    }

}
