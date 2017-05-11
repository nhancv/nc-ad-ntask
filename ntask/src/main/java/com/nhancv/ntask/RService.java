package com.nhancv.ntask;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by nhancao on 5/11/17.
 */

public class RService extends RealmObject {

    @PrimaryKey
    private String className;

    public static RService build(String className) {
        RService rService = new RService();
        rService.setClassName(className);
        return rService;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void save() {
        RealmHelper.transaction(realm -> {
            realm.insertOrUpdate(this);
        });
    }

    public void delete() {
        RealmHelper.transaction(realm -> {
            RTask rTask = realm.where(RTask.class).equalTo("className", getClassName()).findFirst();
            if (rTask != null) {
                rTask.deleteFromRealm();
            }
        });
    }

}
