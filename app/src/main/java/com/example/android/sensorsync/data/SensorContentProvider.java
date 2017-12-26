package com.example.android.sensorsync.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.example.android.sensorsync.data.SensorContract.SensorEntry.TABLE_NAME;

/**
 * Created by sandeepchawan on 2017-10-26.
 */

public class SensorContentProvider extends ContentProvider {

    private SensorDbHelper mSensorDbHelper;

    private static final int SENSOR = 100;

    private static final int SYNC_PENDING = 101;

    private static final int SENSOR_WITH_ID = 102;

    private static final int DELETE_SYNCED = 103;

    private static final int UPDATE_SYNC_STAT = 104;

    private static final int DELETE_ALL_DATA = 105;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final String TAG = SensorContentProvider.class.getSimpleName();

    private long lastSuccessfulSyncTime = 0;

    public static UriMatcher buildUriMatcher () {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        //Add matches with addUri(String authority, String path, int code)
        //directory
        uriMatcher.addURI(SensorContract.AUTHORITY, SensorContract.PATH_SENSOR, SENSOR);
        //unsynced items
        uriMatcher.addURI(SensorContract.AUTHORITY, SensorContract.PATH_SENSOR + "/sync_pending", SYNC_PENDING);
        //Sensor with ID
        uriMatcher.addURI(SensorContract.AUTHORITY, SensorContract.PATH_SENSOR + "/#", SENSOR_WITH_ID);

        uriMatcher.addURI(SensorContract.AUTHORITY, SensorContract.PATH_SENSOR + "/delete_synced", DELETE_SYNCED);

        uriMatcher.addURI(SensorContract.AUTHORITY, SensorContract.PATH_SENSOR + "/update_sync_stat", UPDATE_SYNC_STAT);

        uriMatcher.addURI(SensorContract.AUTHORITY, SensorContract.PATH_SENSOR + "/delete_all_data", DELETE_ALL_DATA);

        return uriMatcher;
    }
    /**
     * In onCreate, we initialize our content provider on startup. This method is called for all
     * registered content providers on the application main thread at application launch time.
     * It must not perform lengthy operations, or application startup will be delayed.
     *
     * Nontrivial initialization (such as opening, upgrading, and scanning
     * databases) should be deferred until the content provider is used (via {@link #query},
     * {@link #bulkInsert(Uri, ContentValues[])}, etc).
     *
     * Deferred initialization keeps application startup fast, avoids unnecessary work if the
     * provider turns out not to be needed, and stops database errors (such as a full disk) from
     * halting application launch.
     *
     * @return true if the provider was successfully loaded, false otherwise
     */
    @Override
    public boolean onCreate() {
        Context context = getContext();
        mSensorDbHelper = new SensorDbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArguments,
                        @Nullable String sortOrder) {
        Cursor cursor;
        final SQLiteDatabase db = mSensorDbHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);

        switch(match) {
            case SENSOR:
                cursor = mSensorDbHelper.getReadableDatabase().query(
                        /* Table we are going to query */
                        SensorContract.SensorEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;
            case SENSOR_WITH_ID:
                //using selection and selection args
                // URI: content://<authority>/sensor/#
                String id = uri.getPathSegments().get(1);

                //Selection is the _ID column = ?, and the Selection args = the row ID from the URI
                String mSelection = "_id=?";
                String[] mSelectionArgs = new String[]{id};

                cursor = mSensorDbHelper.getReadableDatabase().query(
                        /* Table we are going to query */
                        SensorContract.SensorEntry.TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case SYNC_PENDING:
                //return rows which are pending sync
                String mSelection1 = "sync_status=? AND date>?";
                String[] mSelectionArgs1 = new String[]{"0", String.valueOf(lastSuccessfulSyncTime)};

                cursor = mSensorDbHelper.getReadableDatabase().query(
                        /* Table we are going to query */
                        SensorContract.SensorEntry.TABLE_NAME,
                        projection,
                        mSelection1,
                        mSelectionArgs1,
                        null,
                        null,
                        sortOrder);

                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("We are not implementing getType in SensorSync.");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mSensorDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);

        Uri returnUri;
        switch (match) {
            case SENSOR:
                long id = db.insert (SensorContract.SensorEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    //success
                    returnUri = ContentUris.withAppendedId(SensorContract.SensorEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int numRowsDeleted;

        /*
         * If we pass null as the selection to SQLiteDatabase#delete, our entire table will be
         * deleted. However, if we do pass null and delete all of the rows in the table, we won't
         * know how many rows were deleted. According to the documentation for SQLiteDatabase,
         * passing "1" for the selection will delete all rows and return the number of rows
         * deleted, which is what the caller of this method expects.
         */

        switch (sUriMatcher.match(uri)) {

            case DELETE_SYNCED:
                selection = "sync_status=?";
                selectionArgs = new String[]{"1"};
                numRowsDeleted = mSensorDbHelper.getWritableDatabase().delete(
                        SensorContract.SensorEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            case DELETE_ALL_DATA:
                selection = "1";
                numRowsDeleted = mSensorDbHelper.getWritableDatabase().delete(
                        SensorContract.SensorEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return numRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mSensorDbHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {

            case UPDATE_SYNC_STAT:
                db.beginTransaction();
                int rowsUpdated = 0;
                try {
                    //update code goes here
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsUpdated > 0) {
                    Log.i(TAG, "** Update sync stat completed in Content Provider: Rows count= " + rowsUpdated);

                }

                return rowsUpdated;

            default:
                return 0;
        }
    }


    /**
     * Handles requests to insert a set of new rows. In SensorSync, we are only going to be
     * inserting multiple rows of data at a time.
     *
     * @param uri    The content:// URI of the insertion request.
     * @param values An array of sets of column_name/value pairs to add to the database.
     *               This must not be {@code null}.
     *
     * @return The number of values that were inserted.
     */
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mSensorDbHelper.getWritableDatabase();
        //TODO: Delete the below line after testing HTTP POST
        db.execSQL("delete from "+ SensorContract.SensorEntry.TABLE_NAME);
        Log.i(TAG, "Starting Bulk Insert in Content Provider");
        switch (sUriMatcher.match(uri)) {

            case SENSOR:
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        if (value != null) {
                            long _id = db.insert(SensorContract.SensorEntry.TABLE_NAME, null, value);
                            if (_id != -1) {
                                rowsInserted++;
                            }
                        } else {
                            Log.e(TAG, "CONTENT VALUE IS NULL!!!! ");
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    Log.i(TAG, "** Bulk Insert completed in Content Provider: Rows count= " + rowsInserted);
                    getContext().getContentResolver().notifyChange(uri, null, true);
                }

                return rowsInserted;

            case UPDATE_SYNC_STAT:
                int rowsUpdated = bulkUpdate(values) ;
                return rowsUpdated;

            default:
                return super.bulkInsert(uri, values);
        }
    }

    public int bulkUpdate(ContentValues[] values) {
        Log.w(TAG, "Starting BULK Update !!");
        ArrayList<Integer> ids = new ArrayList<>();
        final SQLiteDatabase db = mSensorDbHelper.getWritableDatabase();

        String sqlQuery = "UPDATE " + SensorContract.SensorEntry.TABLE_NAME + " SET " + SensorContract.SensorEntry.COLUMN_SYNC_STATUS
                + " = 1 WHERE " + SensorContract.SensorEntry._ID + " =? " ;

        for (ContentValues value : values) {

            Set<Map.Entry<String, Object>> s=value.valueSet();
            Iterator itr = s.iterator();

            while(itr.hasNext())
            {
                Map.Entry me = (Map.Entry)itr.next();
                String key = me.getKey().toString();
                int id = Integer.parseInt(key);
                Object v =  me.getValue();
                int date = (int) v;
                lastSuccessfulSyncTime = date;
            }
        }

        db.beginTransaction();
        SQLiteStatement upd=db.compileStatement(sqlQuery);
        int i;
        for (i = 0; i < ids.size(); i++) {
            upd.bindLong(1, ids.get(i));
            upd.execute();
        }
        db.endTransaction();

        return i;
    }
}
