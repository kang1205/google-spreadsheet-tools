package com.kang;

import com.alibaba.fastjson.JSONObject;
import com.kang.common.GlobalVariables;
import com.kang.util.LoggerUtil;
import com.kang.util.VmUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author kang
 */
public class AppRunner {

    private static final String INPUT_DATE_PATTERN = "yyyyMMdd";

    public static void main(String[] args) {
        for (String arg : args) {
            if ("-h".equalsIgnoreCase(arg) || "--help".equalsIgnoreCase(arg)) {
                showUsage(0);
            }
        }
        if (args.length < 2) {
            showUsage(-1);
        }
        if (args.length > 2) {
            for (int i = 2; i < args.length; i++) {
                if ("DEBUG".equalsIgnoreCase(args[i])) {
                    GlobalVariables.DEBUG_MODE = true;
                } else if ("DRY-RUN".equalsIgnoreCase(args[i])) {
                    GlobalVariables.DRY_RUN_MODE = true;
                    GlobalVariables.DEBUG_MODE = true;
                }
            }
        }
        LoggerUtil.printInfo("Debug Mode Enabled: %s", GlobalVariables.DEBUG_MODE);
        LoggerUtil.printInfo("Dry-run Mode Enabled: %s", GlobalVariables.DRY_RUN_MODE);

        LoggerUtil.printDebug("worker: %s", GlobalVariables.DRY_RUN_MODE);

        final List<String> tasks = Arrays.asList(args[0].split(","));
        LoggerUtil.printDebug("The task list: %s", JSONObject.toJSONString(tasks));

        LoggerUtil.printDebug("The process date from input : %s", args[1]);
        Date processDate = VmUtil.parseDate(args[1], INPUT_DATE_PATTERN);
        if (Objects.isNull(processDate)) {
            showUsage(-1);
        }
        LoggerUtil.printDebug("The process date after parsed: %s", processDate);

        TaskManager.execute(tasks, processDate);
    }

    private static void showUsage(int exitCode) {
        System.err.println("Usage: AppRunner task(,task2,task3...) date<format: YYYYMMDD, example: 20190101> [debug] [dry-run]");
        System.out.println("Allowed Task: " + JSONObject.toJSONString(TaskManager.ALLOWED_TASKS));

        System.exit(exitCode);
    }

}
