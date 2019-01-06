package com.kang.common;

import com.kang.util.LoggerUtil;
import com.kang.worker.AbstractWorker;

import java.util.LinkedList;

/**
 * task builder utility
 *
 * @author kang
 */
public class TaskBuilder {

    private String name;

    private LinkedList<AbstractWorker> workerChains = new LinkedList<>();

    private TaskBuilder(String name) {
        this.name = name;
    }

    public static TaskBuilder build(String name) {
        return new TaskBuilder(name);
    }

    public String getName() {
        return name;
    }

    public TaskBuilder then(AbstractWorker worker) {
        this.workerChains.add(worker);
        return this;
    }

    public void execute() {
        LoggerUtil.printInfo("The task[%s] is running!", this.name);
        long start = System.currentTimeMillis();
        boolean result = true;
        for (AbstractWorker worker : workerChains) {
            long innerStart = System.currentTimeMillis();
            result = worker.process();
            if (!result) {
                // if failed
                LoggerUtil.printWorkerError(this.name, worker.name(), "Fail!");
                break;
            } else {
                LoggerUtil.printWorkerInfo(this.name, worker.name(), "Done! Elapsed: %d milliseconds", System.currentTimeMillis() - innerStart);
            }
        }
        System.out.println();
        LoggerUtil.printInfo("The task[%s] has done! result: %s, total elapsed : %d milliseconds", this.name, result ? "SUCCESS" : "FAIL", System.currentTimeMillis() - start);
    }
}
