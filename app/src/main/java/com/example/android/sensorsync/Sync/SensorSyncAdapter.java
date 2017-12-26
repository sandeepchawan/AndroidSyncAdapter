package com.example.android.sensorsync.Sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.example.android.sensorsync.data.SensorContentProvider;
import com.example.android.sensorsync.data.SensorContract;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by sandeepchawan on 2017-10-29.
 */

public class SensorSyncAdapter extends AbstractThreadedSyncAdapter {

    //private final AccountManager mAccountManager;

    private static final String TAG = "SensorSyncAdapter";

    /**
     * Content resolver, for performing database operations.
     */
    private final ContentResolver mContentResolver;

    public SensorSyncAdapter (Context context, boolean autoInitialize) {
        super (context, autoInitialize);
        //mAccountManager = AccountManager.get(context);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SensorSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        //mAccountManager = AccountManager.get(context);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        //Log.d(TAG, "onPerformSync for account[" + account.name + "]\n");
        Log.e(TAG, "$$$$$$$$$$$ onPerformSync Starting $$$$$$$$$ \n");
        Cursor syncPendingEntriesCursor = getSyncPendingEntries();
        JSONArray jsonArray = convertCursorToJSON(syncPendingEntriesCursor);
        boolean result = SyncToCloud(jsonArray);
        if (result) {
            updateLocalDB(syncPendingEntriesCursor);
        }
    }

    private void updateLocalDB(Cursor cursor) {

        ArrayList<ContentValues> cvs = new ArrayList<>(); //Will contain all the id's which need to be updated
        ContentResolver sensorContentResolver = getContext().getContentResolver();
        Uri uri = SensorContract.SensorEntry.CONTENT_URI.buildUpon().appendPath("update_sync_stat").build();

        int cnt = 0;
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            ContentValues cv = new ContentValues();
            cv.put(String.valueOf(cursor.getColumnIndex(SensorContract.SensorEntry._ID)),
                    cursor.getColumnIndex(SensorContract.SensorEntry.COLUMN_DATE));
            cnt++;
            cvs.add(cv);
            cursor.moveToNext();
        }

        cursor.close();

        /* Convert the arraylist to normal array */
        ContentValues[] cv_array = new ContentValues[cvs.size()];
        for (int i=0; i<cvs.size(); i++) cv_array[i] = cvs.get(i);

        //Using bulkInsert function because it supports bulk transactions. Using hack for bulk updating.
        sensorContentResolver.bulkInsert(uri, cv_array);
    }

    private boolean SyncToCloud(JSONArray jsonArray) {
        Log.w(TAG, "** Sending POST to server **");
        String urlString = "http://10.0.2.2:3000/addToCloudDB";
        boolean result = false;
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setReadTimeout(10000); /* ms */
            conn.setConnectTimeout(7000); /* ms */
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));

            JSONObject params = new JSONObject();
            params.put("json_array", jsonArray);
            writer.write(params.toString());
            writer.flush();
            writer.close();
            os.close();

            int responseCode=conn.getResponseCode();

            /* 200 represents HTTP OK */
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                result = true;
            }

        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    private Cursor getSyncPendingEntries () {
        Log.e(TAG, "Get Pending Entries from local DB");
        ContentResolver sensorContentResolver = getContext().getContentResolver();
        Uri uri = SensorContract.SensorEntry.CONTENT_URI.buildUpon().appendPath("sync_pending").build();
        /* Get sensor data pending sync */
        Cursor cursor = sensorContentResolver.query(
                SensorContract.SensorEntry.CONTENT_URI,
                null, null, null, null);
        return cursor;
    }

    private JSONArray convertCursorToJSON(Cursor cursor) {
        Log.e(TAG, "Converting Cursor to JSON");
        JSONArray resultSet = new JSONArray();

        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {

            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();

            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {

                    try {

                        if (cursor.getString(i) != null) {
                            //Log.d("TAG_NAME", cursor.getString(i));
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                        } else {
                            rowObject.put(cursor.getColumnName(i), "");
                        }
                    } catch (Exception e) {
                        Log.d("TAG_NAME", e.getMessage());
                    }
                }

            }

            resultSet.put(rowObject);
            cursor.moveToNext();
        }

        //cursor.close();
        Log.d(TAG, resultSet.toString());
        return resultSet;
    }
}
