package com.nhancv.ntasksample;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;

import com.nhancv.ntask.NTask;
import com.nhancv.ntask.NTaskManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NTaskManager taskManager = new NTaskManager();
        taskManager.genData();
        new Thread(() -> {
            SystemClock.sleep(1000);
            taskManager.updateActiveGroup(1);
            System.out.println("Change ActiveGroup: " + taskManager.getLastGroupActive());

            SystemClock.sleep(2000);
            taskManager.updateActiveGroup(2);
            System.out.println("Change ActiveGroup: " + taskManager.getLastGroupActive());

        }).start();

        while (taskManager.hasNext()) {

            NTask nTask = taskManager.next();
            System.out.println("Process: " + nTask.getId() + " - groupActive: " + nTask.getGroupPriority());

            taskManager.popTask(nTask);
            taskManager.refreshTaskList();

            SystemClock.sleep(1000);

        }


    }
}
