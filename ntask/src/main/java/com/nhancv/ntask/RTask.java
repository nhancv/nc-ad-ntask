package com.nhancv.ntask;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by nhancao on 5/10/17.
 */

public class RTask extends RealmObject {
    private static final String TAG = RTask.class.getSimpleName();

    @PrimaryKey
    private String id;
    private String groupId;
    private boolean isActive;
    private int groupIndex;
    private int itemIndex;
    private String itemContent;
    private String updateTime;

    public static RTask build(String id, String groupId,
                              String itemContent) {
        boolean active = false;
        Integer groupIndex;
        RTask tmp = RealmHelper
                .query(realm -> {
                    RTask res = realm.where(RTask.class).equalTo("groupId", groupId).findFirst();
                    if (res != null) {
                        return realm.copyFromRealm(res);
                    }
                    return null;
                });
        if (tmp != null) {
            groupIndex = tmp.getGroupIndex();
            if (tmp.isActive) active = true;
        } else {
            groupIndex = RealmHelper
                    .query(realm -> {
                        Number number = realm.where(RTask.class).max("groupIndex");
                        return number == null ? 0 : (number.intValue() + 1);
                    });
            if (groupIndex != null && groupIndex == 0) {
                active = true;
            }
        }

        Integer itemIndex = RealmHelper
                .query(realm -> {
                    Number number = realm.where(RTask.class).equalTo("groupId", groupId).max("itemIndex");
                    return number == null ? 0 : (number.intValue() + 1);
                });

        return build(id, groupId, active, groupIndex == null ? 0 : groupIndex,
                     itemIndex == null ? 0 : itemIndex,
                     itemContent);
    }

    public static RTask build(String id, String groupId, boolean isActive, int groupIndex, int itemIndex,
                              String itemContent) {
        RTask task = new RTask();
        task.setId(id);
        task.setGroupId(groupId);
        task.setActive(isActive);
        task.setGroupIndex(groupIndex);
        task.setItemIndex(itemIndex);
        task.setItemContent(itemContent);
        task.setUpdateTime(new Date().toString());
        return task;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(int groupIndex) {
        this.groupIndex = groupIndex;
    }

    public int getItemIndex() {
        return itemIndex;
    }

    public void setItemIndex(int itemIndex) {
        this.itemIndex = itemIndex;
    }

    public String getItemContent() {
        return itemContent;
    }

    public void setItemContent(String itemContent) {
        this.itemContent = itemContent;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "NTask{" +
               "id='" + id + '\'' +
               ", groupId='" + groupId + '\'' +
               ", isActive=" + isActive +
               ", groupIndex=" + groupIndex +
               ", itemIndex=" + itemIndex +
               ", itemContent='" + itemContent + '\'' +
               ", updateTime='" + updateTime + '\'' +
               '}';
    }

    public RTask save() {
        RealmHelper.transaction(realm -> {
            realm.insertOrUpdate(this);
        });
        return this;
    }

    public void delete() {
        RealmHelper.transaction(realm -> {
            RTask rTask = realm.where(RTask.class).equalTo("id", getId()).findFirst();
            if (rTask != null) {
                rTask.deleteFromRealm();
            }
        });
    }
}
