package com.kang.worker.template;

import com.kang.worker.AbstractWorker;

import java.util.Date;
import java.util.Map;

/**
 * @author kang
 */
public class TemplateJsonWorker extends AbstractWorker {

    public TemplateJsonWorker(String taskName, Date processDate, String... scopes) {
        super(taskName, processDate, scopes);
    }

    /**
     * the spread sheet name
     *
     * @return
     */
    @Override
    public String spreadSheet() {
        return "template-json";
    }

    /**
     * the sheet name
     *
     * @return
     */
    @Override
    public String sheetName() {
        return "sheet1-json";
    }

    /**
     * assemble values by url or database
     *
     * @param values
     * @return
     */
    @Override
    protected void assembleValues(Map<String, Object> values) {
        if (containScope("scope-1")) {
            /*
                1. add function rowId by method: 'this.addFunctionRowIds()'
            */
            this.addFunctionRowIds("r1", "r2");

            // 2. add cell value
            values.put("r6", "code-value-json");
            values.put("r7", 3.8);
        }
        /*
        other scope handler
        if (containScope("scope-2")){
            // ...
        }
        */
    }
}
