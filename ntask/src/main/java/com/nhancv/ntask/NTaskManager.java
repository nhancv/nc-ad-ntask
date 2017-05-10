package com.nhancv.ntask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Created by nhancao on 5/10/17.
 */

public class NTaskManager implements java.util.Iterator<NTask> {

    public Comparator<NTask> taskComparator = (o1, o2) -> {
        if (o1.isActive() && !o2.isActive()) return -1;
        if (o2.isActive() && !o1.isActive()) return 1;

        if (o1.getGroupPriority() < o2.getGroupPriority()) return -1;
        if (o2.getGroupPriority() < o1.getGroupPriority()) return 1;

        if (o1.getItemPriority() < o2.getItemPriority()) return -1;
        if (o2.getItemPriority() < o1.getItemPriority()) return 1;

        return 0;
    };

    private List<NTask> taskList = new ArrayList<>();
    private int lastGroupActive;

    public void genData() {
        taskList.clear();
        for (int i = 0; i < 3; i++) {
            String groupId = UUID.randomUUID().toString();
            for (int j = 0; j < 5; j++) {
                taskList.add(NTask.build(UUID.randomUUID().toString(),
                                         groupId, i == 0, i, j, "Item-" + j));
            }
        }

        Collections.sort(taskList, taskComparator);

        showList();

    }

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

    public synchronized void popTask(NTask task) {
        if (task == null) return;
        for (int i = 0; i < taskList.size(); i++) {
            NTask nTask = taskList.get(i);
            if (nTask.getId().equals(task.getId())) {
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
        }
    }

    public synchronized void refreshTaskList() {
        /*
         * Mark active group task
         * Find other group task which lower priority
         */

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

    @Override
    public synchronized boolean hasNext() {
        return taskList.size() > 0;
    }

    @Override
    public synchronized NTask next() {
        //Get active task
        List<NTask> activeTask = getActiveTask();
        if (activeTask.size() > 0) {
            return activeTask.get(0);
        }
        if (hasNext()) {
            return taskList.get(0);
        }
        return null;
    }

    @Override
    public synchronized void remove() {
        if (hasNext()) {
            taskList.remove(0);
        }
    }

    @Override
    public void forEachRemaining(Consumer<? super NTask> action) {

    }
}
