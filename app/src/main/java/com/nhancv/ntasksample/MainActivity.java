package com.nhancv.ntasksample;

import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;

import com.nhancv.ntask.NTask;
import com.nhancv.ntask.NTaskManager;
import com.nhancv.ntask.NTaskService;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    private List<NTask> sample;
    private int index;

    {
        NTaskService.setTaskProcess(nTask -> {
            System.out.println("Process: " + nTask.getId() + " - groupActive: " + nTask.getGroupPriority());
            SystemClock.sleep(1000);
            return true;
        });
    }

    @AfterViews
    protected void init() {
        NTaskManager.init(this);

        sample = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            String groupId = UUID.randomUUID().toString();
            for (int j = 0; j < 3; j++) {
                sample.add(NTask.build(UUID.randomUUID().toString(), groupId, i == 0, i, j, "Item-" + j));
            }
        }
    }

    @Click(R.id.activity_main_bt_post)
    protected void btPostClick() {
        NTaskManager.getInstance().postTask(sample.get((index++) % sample.size()));
    }
}
