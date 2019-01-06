package com.kang.util;

import static com.kang.common.GlobalVariables.DEBUG_MODE;

/**
 * @author kang
 */
public class LoggerUtil {

    public static void printInfo(String format, Object... args) {
        System.out.println("[INFO] " + String.format(format, args));
    }

    public static void printDebug(String format, Object... args) {
        if (DEBUG_MODE) {
            System.out.println("[DEBUG] " + String.format(format, args));
        }
    }

    public static void printError(String format, Object... args) {
        System.err.println("[ERROR] " + String.format(format, args));
    }

    public static void printWorkerInfo(String task, String worker, String format, Object... args) {
        System.out.println(String.format("[INFO][%s->%s] %s", task, worker, String.format(format, args)));
    }

    public static void printWorkerDebug(String task, String worker, String format, Object... args) {
        if (DEBUG_MODE) {
            System.out.println(String.format("[DEBUG][%s->%s] %s", task, worker, String.format(format, args)));
        }
    }

    public static void printWorkerError(String task, String worker, String format, Object... args) {
        System.err.println(String.format("[ERROR][%s->%s] %s", task, worker, String.format(format, args)));
    }
}
