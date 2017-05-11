package com.nhancv.ntasksample;

import android.content.Intent;
import android.os.SystemClock;

import com.nhancv.ntask.AbstractTaskService;
import com.nhancv.ntask.NTaskManager;
import com.nhancv.ntask.RTask;

/**
 * Created by nhancao on 5/11/17.
 */

public class TaskService extends AbstractTaskService {

    @Override
    protected void doing(Intent intent) {
        while (NTaskManager.hasNext()) {
            RTask rTask = NTaskManager.next();
            if (rTask != null) {
                System.out.println("Process: " + rTask.getId() + " - groupActive: " + rTask.getGroupIndex());
                SystemClock.sleep(1000);
                NTaskManager.completeTask(rTask);
            }
        }
    }
}
