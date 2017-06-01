package com.nhancv.ntasksample;

import android.content.Intent;
import android.os.SystemClock;

import com.nhancv.ntask.AbstractTaskService;
import com.nhancv.ntask.NTaskManager;
import com.nhancv.ntask.RTask;

import java.util.Random;

/**
 * Created by nhancao on 5/11/17.
 */

public class TaskService extends AbstractTaskService {

    private Random random = new Random();

    @Override
    protected void doing(Intent intent) {
        doingLoop();
    }

    private void doingLoop() {
        while (NTaskManager.hasNext()) {
            RTask rTask = NTaskManager.next();
            if (rTask != null) {
                System.out.println("Process: " + rTask.getId() + " - groupActive: " + rTask.getGroupIndex()
                                   + " - status: " + rTask.getStatus() + " - retryTime: " + rTask.getRetryTime());
                SystemClock.sleep(500);
                if (random.nextBoolean()) {
                    NTaskManager.markTaskFailed(rTask);
                } else {
                    NTaskManager.completeTask(rTask);
                }
            }
        }
        int errorCount = (int) NTaskManager.getInstance().getCountByErrorStatus();
        if (errorCount > 0) {
            System.out.println("Failed task count: " + errorCount);
            NTaskManager.getInstance().resetStatusQueue();
            System.out.println("Wait 2s");
            SystemClock.sleep(2000);
            doingLoop();
        } else {
            System.out.println("Done");
        }
    }
}
