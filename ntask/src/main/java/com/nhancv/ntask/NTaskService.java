package com.nhancv.ntask;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by nhancao on 5/10/17.
 */

public class NTaskService extends IntentService {
    public static final String ACTION = "com.nhancv.ntask.NTaskService";
    private static final String TAG = NTaskService.class.getSimpleName();
    private static boolean processing;

    public NTaskService() {
        super(TAG);
        processing = false;
        NTaskManager.getInstance().genData();
    }

    public static void start(Context context) {
        context.startService(new Intent(context, NTaskService.class));
    }

    public static void notify(Context context) {
        Intent i = new Intent(NTaskService.ACTION);
        context.sendBroadcast(i);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e(TAG, "onHandleIntent: start");
        processing = true;
        while (NTaskManager.getInstance().hasNext()) {

            NTask nTask = NTaskManager.getInstance().next();
            System.out.println("Process: " + nTask.getId() + " - groupActive: " + nTask.getGroupPriority());
            NTaskManager.getInstance().completeTask(nTask);
            SystemClock.sleep(1000);

        }
        processing = false;
        Log.e(TAG, "onHandleIntent: end");
    }

    public boolean isProcessing() {
        return processing;
    }

    public static class NReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!processing) {
                context.startService(new Intent(context, NTaskService.class));
            }
        }

    }


}
