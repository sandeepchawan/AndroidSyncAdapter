package com.example.android.sensorsync.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sandeepchawan on 2017-10-26.
 */

public class SensorDbHelper extends SQLiteOpenHelper {
    /*
     * This is the name of our database. Database names should be descriptive and end with the
     * .db extension.
     */
    public static final String DATABASE_NAME = "sensor.db";

    private static final int DATABASE_VERSION = 1;

    public SensorDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time. This is where the creation of
     * tables and the initial population of the tables should happen.
     *
     * @param sqLiteDatabase The database.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        /*
         * This String will contain a simple SQL statement that will create a table that will
         * cache our sensor data.
         */
        final String SQL_CREATE_SENSOR_TABLE =

                "CREATE TABLE " + SensorContract.SensorEntry.TABLE_NAME + " (" +

                /*
                 * SensorEntry did not explicitly declare a column called "_ID". However,
                 * SensorEntry implements the interface, "BaseColumns", which does have a field
                 * named "_ID". We use that here to designate our table's primary key.
                 */
                        SensorContract.SensorEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        SensorContract.SensorEntry.COLUMN_DATE       + " INTEGER NOT NULL, "                 +

                        SensorContract.SensorEntry.COLUMN_UID       + " INTEGER DEFAULT 0, "    +

                        SensorContract.SensorEntry.COLUMN_ACC_X + " FLOAT DEFAULT NULL, "                       +
                        SensorContract.SensorEntry.COLUMN_ACC_Y   + " FLOAT DEFAULT NULL, "                    +
                        SensorContract.SensorEntry.COLUMN_ACC_Z   + " FLOAT DEFAULT NULL, "                    +

                        SensorContract.SensorEntry.COLUMN_GYRO_X + " FLOAT DEFAULT NULL, "                       +
                        SensorContract.SensorEntry.COLUMN_GYRO_Y   + " FLOAT DEFAULT NULL, "                    +
                        SensorContract.SensorEntry.COLUMN_GYRO_Z   + " FLOAT DEFAULT NULL, "                    +

                        SensorContract.SensorEntry.COLUMN_MAGNETO_X + " FLOAT DEFAULT NULL, "                       +
                        SensorContract.SensorEntry.COLUMN_MAGNETO_Y   + " FLOAT DEFAULT NULL, "                    +
                        SensorContract.SensorEntry.COLUMN_MAGNETO_Z   + " FLOAT DEFAULT NULL, "                    +

                        SensorContract.SensorEntry.COLUMN_GPS_LAT       + " DOUBLE DEFAULT NULL, "    +
                        SensorContract.SensorEntry.COLUMN_GPS_LONG       + " DOUBLE DEFAULT NULL, "    +

                        SensorContract.SensorEntry.COLUMN_SYNC_STATUS       + " INTEGER NOT NULL DEFAULT 0, "    +

                        SensorContract.SensorEntry.COLUMN_CLASSIFICATION      + " INTEGER DEFAULT NULL);";

        sqLiteDatabase.execSQL(SQL_CREATE_SENSOR_TABLE);
    }

    /**
     * This database is only a cache for online data, so its upgrade policy is simply to discard
     * the data and call through to onCreate to recreate the table. Note that this only fires if
     * you change the version number for your database (in our case, DATABASE_VERSION). It does NOT
     * depend on the version number for your application found in your app/build.gradle file. If
     * you want to update the schema without wiping data, commenting out the current body of this
     * method should be your top priority before modifying this method.
     *
     * @param sqLiteDatabase Database that is being upgraded
     * @param oldVersion     The old database version
     * @param newVersion     The new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SensorContract.SensorEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
