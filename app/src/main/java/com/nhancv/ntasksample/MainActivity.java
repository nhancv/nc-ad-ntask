package com.nhancv.ntasksample;

import android.support.v7.app.AppCompatActivity;

import com.nhancv.ntask.NTaskService;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    @AfterViews
    protected void init() {
        NTaskService.start(this);
    }

    @Click(R.id.activity_main_bt_post)
    protected void btPostClick() {
        NTaskService.notify(this);
    }
}
