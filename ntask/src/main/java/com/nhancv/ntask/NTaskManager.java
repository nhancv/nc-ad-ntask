package com.nhancv.ntask;

import android.content.Context;
import android.content.Intent;
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
    static boolean processing;
    public Comparator<RTask> taskComparator = (o1, o2) -> {
        if (o1.isActive() && !o2.isActive()) return -1;
        if (o2.isActive() && !o1.isActive()) return 1;

        if (o1.getGroupIndex() < o2.getGroupIndex()) return -1;
        if (o2.getGroupIndex() < o1.getGroupIndex()) return 1;

        if (o1.getItemIndex() < o2.getItemIndex()) return -1;
        if (o2.getItemIndex() < o1.getItemIndex()) return 1;

        return 0;
    };
    private WeakReference<Context> contextWeakReference;
    private List<RTask> taskList = new ArrayList<>();
    private int lastGroupActiveIndex;
    private String serviceClassName;

    public NTaskManager() {
        processing = false;
    }

    public static void init(Context context) {
        init(context, null);
    }

    public static void init(Context context, Class<?> serviceClassName) {
        //Build realm
        Realm.init(context);
        getInstance().setContextWeakReference(context);
        getInstance().initDataFromStorage();
        getInstance().retrieveClassServiceName(serviceClassName != null ? serviceClassName.getName() : null);
        notify(context);
    }

    public static NTaskManager getInstance() {
        return SingletonHelper.INSTANCE;
    }

    public synchronized static void completeTask(RTask task) {
        getInstance().popTask(task);
        getInstance().refreshTaskList();
    }

    public synchronized static boolean hasNext() {
        return getInstance().getTaskList().size() > 0;
    }

    public synchronized static RTask next() {
        //Get active task
        List<RTask> activeTask = getInstance().getActiveTask();
        if (activeTask.size() > 0) {
            return activeTask.get(0);
        }
        if (hasNext()) {
            return getInstance().getTaskList().get(0);
        }
        return null;
    }

    public synchronized static void remove() {
        if (hasNext()) {
            getInstance().getTaskList().remove(0);
        }
    }

    public static void notify(Context context) {
        if (!processing && getInstance().getServiceClassName() != null) {
            try {
                context.startService(new Intent(context, Class.forName(getInstance().getServiceClassName())));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void exportRealmFile(Context context) {
        RealmHelper.exportRealmFile(context);
    }

    public synchronized static void postTask(RTask rTask) {
        rTask.save();
        getInstance().getTaskList().add(rTask);
        getInstance().sortTasks();

        if (!getInstance().isNull()) {
            notify(getInstance().getContextWeakReference().get());
        } else {
            Log.e(TAG, "postTask: context is null, need to call init first");
        }
    }

    public WeakReference<Context> getContextWeakReference() {
        return contextWeakReference;
    }

    public void setContextWeakReference(Context context) {
        this.contextWeakReference = new WeakReference<>(context);
    }

    public boolean isNull() {
        return contextWeakReference == null || contextWeakReference.get() == null;
    }

    public void initDataFromStorage() {
        taskList.clear();
        taskList = getTaskList(true);
        sortTasks();
        showList();
    }

    private void sortTasks() {Collections.sort(taskList, taskComparator);}

    public synchronized void showList() {
        for (RTask rTask : taskList) {
            System.out.println(rTask);
        }
    }

    public synchronized List<RTask> getActiveTask() {
        List<RTask> res = new ArrayList<>();
        for (RTask task : taskList) {
            if (task.isActive()) {
                res.add(task);
                lastGroupActiveIndex = task.getGroupIndex();
            }
        }
        return res;
    }

    public synchronized void popTask(RTask task) {
        if (task == null) return;
        for (int i = 0; i < taskList.size(); i++) {
            RTask rTask = taskList.get(i);
            if (rTask.getId().equals(task.getId())) {
                rTask.delete();
                taskList.remove(i);
                break;
            }
        }
    }

    public synchronized void updateActiveGroup(int groupIndex) {
        lastGroupActiveIndex = groupIndex;
        for (RTask rTask : taskList) {
            if (rTask.getGroupIndex() == groupIndex) {
                rTask.setActive(true);
            } else {
                rTask.setActive(false);
            }
            rTask.save();
        }
    }

    public synchronized void updateActiveGroup(String groupId) {
        for (RTask rTask : taskList) {
            if (rTask.getGroupId().equals(groupId)) {
                rTask.setActive(true);
                lastGroupActiveIndex = rTask.getGroupIndex();
            } else {
                rTask.setActive(false);
            }
            rTask.save();
        }
    }

    public void retrieveClassServiceName(String serviceClassName) {
        if (serviceClassName != null) {
            this.serviceClassName = serviceClassName;
            RService.build(serviceClassName).save();
        } else {
            RService rService = RealmHelper
                    .query(realm -> realm.copyFromRealm(realm.where(RService.class).findFirst()));
            if (rService != null) {
                this.serviceClassName = rService.getClassName();
            }
        }
    }

    private synchronized void refreshTaskList() {
        /*
         * Mark active group task
         * Find other group task which lower priority
         */

        if (!hasNext()) {
            initDataFromStorage();
        }

        //Check is there any active group
        for (RTask task : taskList) {
            if (task.isActive()) {
                return;
            }
        }

        //Change active group
        if (hasNext()) {
            lastGroupActiveIndex = taskList.get(0).getGroupIndex();
        }

        //Update active with new group active
        updateActiveGroup(lastGroupActiveIndex);

    }

    public String getServiceClassName() {
        return serviceClassName;
    }

    public List<RTask> getTaskList() {
        return getTaskList(false);
    }

    public List<RTask> getTaskList(boolean fromRealm) {
        if (fromRealm) {
            return RealmHelper.query(realm -> {
                RealmResults<RTask> realmResults = realm.where(RTask.class).findAll();
                return realm.copyFromRealm(realmResults);
            });
        } else {
            return taskList;
        }
    }

    public int getLastGroupActiveIndex() {
        return lastGroupActiveIndex;
    }

    private static class SingletonHelper {
        private static final NTaskManager INSTANCE = new NTaskManager();
    }
}
