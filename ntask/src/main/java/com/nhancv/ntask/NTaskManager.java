package com.nhancv.ntask;

import android.content.Context;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by nhancao on 5/10/17.
 */

public class NTaskManager {

    private static final String TAG = NTaskManager.class.getSimpleName();

    public Comparator<NTask> taskComparator = (o1, o2) -> {
        if (o1.isActive() && !o2.isActive()) return -1;
        if (o2.isActive() && !o1.isActive()) return 1;

        if (o1.getGroupPriority() < o2.getGroupPriority()) return -1;
        if (o2.getGroupPriority() < o1.getGroupPriority()) return 1;

        if (o1.getItemPriority() < o2.getItemPriority()) return -1;
        if (o2.getItemPriority() < o1.getItemPriority()) return 1;

        return 0;
    };
    private WeakReference<Context> contextWeakReference;
    private List<NTask> taskList = new ArrayList<>();
    private int lastGroupActive;

    public static void init(Context context) {
        //Build realm
        Realm.init(context);
        getInstance().setContextWeakReference(context);
        getInstance().initDataFromStorage();
        NTaskService.notify(context);
    }

    public static NTaskManager getInstance() {
        return SingletonHelper.INSTANCE;
    }

    public synchronized static void completeTask(NTask task) {
        getInstance().popTask(task);
        getInstance().refreshTaskList();
    }

    public synchronized static boolean hasNext() {
        return getInstance().taskList.size() > 0;
    }

    public synchronized static NTask next() {
        //Get active task
        List<NTask> activeTask = getInstance().getActiveTask();
        if (activeTask.size() > 0) {
            return activeTask.get(0);
        }
        if (hasNext()) {
            return getInstance().taskList.get(0);
        }
        return null;
    }

    public synchronized static void remove() {
        if (hasNext()) {
            getInstance().taskList.remove(0);
        }
    }

    public boolean isNull() {
        return contextWeakReference == null;
    }

    public void setContextWeakReference(Context context) {
        this.contextWeakReference = new WeakReference<>(context);
    }

    public void initDataFromStorage() {
        taskList.clear();
        taskList = RealmHelper.query(realm -> {
            RealmResults<NTask> realmResults = realm.where(NTask.class).findAll();
            return realm.copyFromRealm(realmResults);
        });
        sortTasks();
        showList();
        //Backup for testing
        RealmHelper.exportDatabase(contextWeakReference.get());
    }

    private void sortTasks() {Collections.sort(taskList, taskComparator);}

    public synchronized void showList() {
        for (NTask nTask : taskList) {
            System.out.println(nTask);
        }
    }

    public synchronized List<NTask> getActiveTask() {
        List<NTask> res = new ArrayList<>();
        for (NTask task : taskList) {
            if (task.isActive()) {
                res.add(task);
                lastGroupActive = task.getGroupPriority();
            }
        }
        return res;
    }

    public void postTask(NTask nTask) {
        System.out.println("postTask: " + nTask.getId() + " - groupActive: " + nTask.getGroupPriority());

        nTask.save();
        taskList.add(nTask);
        sortTasks();

        if (contextWeakReference != null && contextWeakReference.get() != null) {
            NTaskService.notify(contextWeakReference.get());
        } else {
            Log.e(TAG, "postTask: context is null, need to call init first");
        }

    }

    public synchronized void popTask(NTask task) {
        if (task == null) return;
        for (int i = 0; i < taskList.size(); i++) {
            NTask nTask = taskList.get(i);
            if (nTask.getId().equals(task.getId())) {
                nTask.delete();
                taskList.remove(i);
                break;
            }
        }
    }

    public synchronized void updateActiveGroup(int activeGroup) {
        lastGroupActive = activeGroup;
        for (NTask nTask : taskList) {
            if (nTask.getGroupPriority() == activeGroup) {
                nTask.setActive(true);
            } else {
                nTask.setActive(false);
            }
            nTask.save();
        }
    }

    public synchronized void refreshTaskList() {
        /*
         * Mark active group task
         * Find other group task which lower priority
         */

        if (!hasNext()) {
            initDataFromStorage();
        }

        //Check is there any active group
        for (NTask task : taskList) {
            if (task.isActive()) {
                return;
            }
        }

        //Change active group
        if (hasNext()) {
            lastGroupActive = taskList.get(0).getGroupPriority();
        }

        //Update active with new group active
        updateActiveGroup(lastGroupActive);

    }

    public List<NTask> getTaskList() {
        return taskList;
    }

    public int getLastGroupActive() {
        return lastGroupActive;
    }

    private static class SingletonHelper {
        private static final NTaskManager INSTANCE = new NTaskManager();
    }
}
