package com.nhancv.ntask;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.lang.ref.WeakReference;
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
    private Comparator<RTask> taskComparator = (o1, o2) -> {
        if (o1.isActive() && !o2.isActive()) return -1;
        if (o2.isActive() && !o1.isActive()) return 1;

        if (o1.getGroupIndex() < o2.getGroupIndex()) return -1;
        if (o2.getGroupIndex() < o1.getGroupIndex()) return 1;

        if (o1.getItemIndex() < o2.getItemIndex()) return -1;
        if (o2.getItemIndex() < o1.getItemIndex()) return 1;

        return 0;
    };
    private WeakReference<Context> contextWeakReference;
    private String lastGroupIdActive;
    private String serviceClassName;

    private NTaskManager() {
        processing = false;
    }

    public static void init(Context context) {
        init(context, (String) null);
    }

    public static void init(Context context, Class<?> serviceClassName) {
        init(context, serviceClassName != null ? serviceClassName.getName() : null);
    }

    public static void init(Context context, String serviceClassName) {
        //Build realm
        Realm.init(context);
        getInstance().setContextWeakReference(context);
        getInstance().retrieveClassServiceName(serviceClassName);
        notify(context);
    }

    public static NTaskManager getInstance() {
        return SingletonHelper.INSTANCE;
    }

    public synchronized static void completeTask(RTask task) {
        getInstance().popTask(task);
    }

    public synchronized static void markTaskFailed(RTask task) {
        task.setStatus(-1);
        task.save();
    }

    public synchronized static void refreshTask(RTask task) {
        task.setStatus(0);
        task.save();
    }

    public synchronized static boolean hasNext() {
        return getInstance().getCount() > 0;
    }

    public synchronized static RTask next() {
        //Get active task
        List<RTask> activeTask = getInstance().getActiveTask();
        if (activeTask.size() > 0) {
            return activeTask.get(0);
        }
        if (hasNext()) {
            RTask rTask = getInstance().getTaskList().get(0);
            if (!getInstance().getLastGroupIdActive().equals(rTask.getGroupId())) {
                getInstance().updateActiveGroup(rTask.getGroupId());
            }
            return rTask;
        }
        return null;
    }

    public static void exportRealmFile(Context context) {
        RealmHelper.exportRealmFile(context);
    }

    public synchronized static void postTask(RTask rTask) {
        rTask.save();

        if (!getInstance().isNull()) {
            notify(getInstance().getContextWeakReference().get());
        } else {
            Log.e(TAG, "postTask: context is null, need to call init first");
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

    public WeakReference<Context> getContextWeakReference() {
        return contextWeakReference;
    }

    public void setContextWeakReference(Context context) {
        this.contextWeakReference = new WeakReference<>(context);
    }

    public boolean isNull() {
        return contextWeakReference == null || contextWeakReference.get() == null;
    }

    public synchronized void updateActiveGroup(String groupId) {
        if (groupId == null) {
            List<RTask> taskList = getTaskList();
            if (taskList != null && taskList.size() > 0) {
                groupId = taskList.get(0).getGroupId();
            }
        }
        if (groupId != null)
            for (RTask rTask : getTaskList()) {
                if (rTask.getGroupId().equals(groupId)) {
                    rTask.setActive(true);
                    lastGroupIdActive = rTask.getGroupId();
                } else {
                    rTask.setActive(false);
                }
                rTask.save();
            }
    }

    public synchronized void showList() {
        for (RTask rTask : getTaskList()) {
            System.out.println(rTask);
        }
    }

    public synchronized void popTask(RTask task) {
        if (task == null) return;
        task.delete();
    }

    public synchronized long getCountByActiveStatus() {
        return getCountByActiveStatus(0);
    }

    public synchronized long getCountByActiveStatus(int status) {
        Long count = RealmHelper
                .query(realm -> realm.where(RTask.class).equalTo("isActive", true).equalTo("status", status).count());
        return count == null ? 0 : count;
    }

    public synchronized long getCount() {
        return getCountByStatus(0);
    }

    public String getServiceClassName() {
        return serviceClassName;
    }

    public synchronized List<RTask> getActiveTask() {
        return RealmHelper.query(realm -> {
            RealmResults<RTask> realmResults = realm.where(RTask.class).equalTo("isActive", true).equalTo("status", 0)
                                                    .findAll();
            List<RTask> tasks = realm.copyFromRealm(realmResults);
            Collections.sort(tasks, taskComparator);
            if (tasks.size() > 0) {
                lastGroupIdActive = tasks.get(0).getGroupId();
            }
            return tasks;
        });
    }

    public synchronized List<RTask> getTaskList() {
        return getTaskList(0);
    }

    public synchronized List<RTask> getTaskList(int status) {
        return RealmHelper.query(realm -> {
            RealmResults<RTask> realmResults = realm.where(RTask.class).equalTo("status", status).findAll();
            List<RTask> tasks = realm.copyFromRealm(realmResults);
            Collections.sort(tasks, taskComparator);
            return tasks;
        });
    }

    public synchronized RTask getTask(String taskId) {
        return RealmHelper.query(realm -> {
            RTask realmResult = realm.where(RTask.class).equalTo("id", taskId).findFirst();
            if (realmResult != null) return realm.copyFromRealm(realmResult);
            return null;
        });
    }

    public synchronized long getCountByErrorStatus() {
        return getCountByStatus(-1);
    }

    public synchronized long getCountByStatus(int status) {
        Long count = RealmHelper.query(realm -> realm.where(RTask.class).equalTo("status", status).count());
        return count == null ? 0 : count;
    }

    public synchronized void resetStatusQueue() {
        RealmHelper.transaction(realm -> {
            RealmResults<RTask> realmResults = realm.where(RTask.class).notEqualTo("status", 0).findAll();
            List<RTask> tasks = realm.copyFromRealm(realmResults);
            for (RTask task : tasks) {
                task.setStatus(0);
                task.setRetryTime(task.getRetryTime() + 1);
            }
            realm.insertOrUpdate(tasks);
        });
    }

    public String getLastGroupIdActive() {
        return lastGroupIdActive == null ? "" : lastGroupIdActive;
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

    private static class SingletonHelper {
        private static final NTaskManager INSTANCE = new NTaskManager();
    }
}
