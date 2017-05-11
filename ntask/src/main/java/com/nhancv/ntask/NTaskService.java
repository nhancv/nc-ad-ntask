package com.nhancv.ntask;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;

/**
 * Created by nhancao on 5/10/17.
 */

public class NTaskService extends IntentService {
    private static final String TAG = NTaskService.class.getSimpleName();

    public NTaskService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (NTaskManager.getInstance().isNull()) {
            NTaskManager.init(getApplicationContext(), null);
        }

        NTaskManager.processing = true;
        while (NTaskManager.hasNext()) {
            RTask rTask = NTaskManager.next();
            System.out.println("Process: " + rTask.getId() + " - groupActive: " + rTask.getGroupPriority());
            SystemClock.sleep(1000);
            NTaskManager.completeTask(rTask);
        }
        NTaskManager.processing = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }

}
