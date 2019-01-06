package com.kang;

import com.alibaba.fastjson.JSONObject;
import com.google.api.services.sheets.v4.Sheets;
import com.kang.common.TaskBuilder;
import com.kang.helper.GoogleSheetHelper;
import com.kang.helper.ResourcesHelper;
import com.kang.model.MySpreadSheet;
import com.kang.util.LoggerUtil;
import com.kang.worker.AbstractWorker;
import com.kang.worker.template.TemplateJsonWorker;
import com.kang.worker.template.TemplateXmlWorker;

import java.util.*;

/**
 * @author kang
 */
public class TaskManager {

    public static final Set<String> ALLOWED_TASKS = new LinkedHashSet<>(Arrays.asList(
            "template-json", "template-xml"
    ));

    /**
     * the entrance of task
     *
     * @param tasks
     * @param processDate
     */
    public static void execute(List<String> tasks, Date processDate) {
        // 1. load resource
        Map<String, MySpreadSheet> sheets = ResourcesHelper.sheets();
        LoggerUtil.printDebug("All spreadsheet configuration files: %s", JSONObject.toJSONString(sheets));

        // 2. load google service
        Sheets sheetsService = GoogleSheetHelper.sheetsService();

        AbstractWorker.init(sheets, sheetsService);

        // 3. build tasks
        for (String task : tasks) {
            if (!ALLOWED_TASKS.contains(task)) {
                LoggerUtil.printError("Forbidden task name : %s", task);
                continue;
            }
            switch (task) {
                // 3.1. template task
                case "template-json":
                    TaskBuilder.build(task).then(new TemplateJsonWorker(task, processDate)).execute();
                    break;
                case "template-xml":
                    TaskBuilder.build(task).then(new TemplateXmlWorker(task, processDate)).execute();
                    break;
                default:
                    LoggerUtil.printError("Invalid task name : %s", task);
                    break;
            }
        }
    }


}
