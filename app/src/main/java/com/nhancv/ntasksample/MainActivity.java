package com.nhancv.ntasksample;

import android.support.v7.app.AppCompatActivity;

import com.nhancv.ntask.NTaskManager;
import com.nhancv.ntask.RTask;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    private List<RTask> sample;
    private int index;

    @AfterViews
    protected void init() {
        NTaskManager.init(this, TaskService.class);

        sample = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            String groupId = UUID.randomUUID().toString();
            for (int j = 0; j < 3; j++) {
                sample.add(RTask.build(UUID.randomUUID().toString(), groupId, i == 0, i, j, "Item-" + j));
            }
        }
    }

    @Click(R.id.activity_main_bt_post)
    protected void btPostClick() {
        NTaskManager.postTask(sample.get((index++) % sample.size()));

        //Backup for testing
        NTaskManager.exportRealmFile(this);
    }
}
