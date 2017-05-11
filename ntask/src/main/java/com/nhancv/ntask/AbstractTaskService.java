package com.nhancv.ntask;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by nhancao on 5/10/17.
 */

public abstract class AbstractTaskService extends IntentService {


    public AbstractTaskService() {
        this(AbstractTaskService.class.getName());
    }

    public AbstractTaskService(String name) {
        super(name);
    }

    protected abstract void doing(Intent intent);

    @Override
    protected void onHandleIntent(Intent intent) {
        if (NTaskManager.getInstance().isNull()) {
            NTaskManager.init(getApplicationContext());
        }

        NTaskManager.processing = true;
        doing(intent);
        NTaskManager.processing = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }

}
