package com.nhancv.ntask;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

/**
 * Created by nhancao on 5/10/17.
 */

public class NTaskService extends IntentService {
    private static final String TAG = NTaskService.class.getSimpleName();
    private static boolean processing;
    private static NTaskProcess nTaskProcess;

    public NTaskService() {
        super(TAG);
        processing = false;
    }

    public static void notify(Context context) {
        if (!processing) {
            try {
                context.startService(new Intent(context, Class.forName(NTaskService.class.getName())));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setTaskProcess(NTaskProcess nTaskProcess) {
        NTaskService.nTaskProcess = nTaskProcess;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (NTaskManager.getInstance().isNull()) {
            NTaskManager.init(getApplicationContext());
        }

        processing = true;
        while (NTaskManager.hasNext()) {
            NTask nTask = NTaskManager.next();
            if (nTaskProcess != null && nTaskProcess.doing(nTask)) {
                NTaskManager.completeTask(nTask);
            }
        }
        processing = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }

    public boolean isProcessing() {
        return processing;
    }

}
