package com.nhancv.ntask;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by nhancao on 5/11/17.
 */

class RealmHelper {
    private static final String TAG = RealmHelper.class.getSimpleName();
    private static final String databaseName = "ntask";

    private static final RealmConfiguration realmConfig = new RealmConfiguration.Builder()
            .name(databaseName)
            .deleteRealmIfMigrationNeeded()
            .build();

    static void transaction(RealmTransaction realmTransaction) {
        Realm realm = null;
        try {
            realm = Realm.getInstance(realmConfig);
            realm.beginTransaction();
            realmTransaction.execute(realm);
            realm.commitTransaction();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    static <T> T query(RealmQuery<T> realmDoing) {
        Realm realm = null;
        try {
            realm = Realm.getInstance(realmConfig);
            return realmDoing.query(realm);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
        return null;
    }

    /**
     * Export database to sdcard
     */
    static void exportRealmFile(Context context) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//" + context.getPackageName() + "//files//" + databaseName;
                String backupDBPath = databaseName + ".realm";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Log.d(TAG, "exportRealmFile: Db file has been backup on sdcard");
                } else {
                    Log.d(TAG, "exportRealmFile: current db not exists");
                }
            } else {
                Log.d(TAG, "exportRealmFile: Sdcard can not Write");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    interface RealmTransaction {
        void execute(Realm realm);
    }

    interface RealmQuery<T> {
        T query(Realm realm);
    }
}
