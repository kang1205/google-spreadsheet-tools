package com.kang.model;

import java.util.List;
import java.util.Map;

/**
 * @author kang
 */
public class MyOnlineSheet {

    private List<String> headers;

    /**
     * row id -> row
     */
    private Map<String, Row> rows;

    public MyOnlineSheet(List<String> headers, Map<String, Row> rows) {
        this.headers = headers;
        this.rows = rows;
    }

    public Map<String, Row> getRows() {
        return rows;
    }

    public void setRows(Map<String, Row> rows) {
        this.rows = rows;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public static class Row {

        /**
         * row index on online sheet
         */
        private int index;

        /**
         * the data from configuration
         */
        private MyRow row;

        public static Row of(int index, MyRow row) {
            Row r = new Row();
            r.setIndex(index);
            r.setRow(row);
            return r;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public MyRow getRow() {
            return row;
        }

        public void setRow(MyRow row) {
            this.row = row;
        }
    }
}
