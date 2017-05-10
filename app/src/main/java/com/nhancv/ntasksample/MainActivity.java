package com.nhancv.ntasksample;

import android.os.Bundle;
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
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Change ActiveGroup: " + 1);
            taskManager.updateActiveGroup(1);
            taskManager.showList();

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Change ActiveGroup: " + 2);
            taskManager.updateActiveGroup(2);
            taskManager.showList();

        }).start();
        while (taskManager.hasNext()) {

            NTask nTask = taskManager.next();
            System.out.println("Process: " + nTask.getId());

            taskManager.popTask(nTask);
            taskManager.refreshTaskList();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


    }
}
