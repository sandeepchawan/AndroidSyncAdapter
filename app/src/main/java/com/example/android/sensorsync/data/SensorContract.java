package com.example.android.sensorsync.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by sandeepchawan on 2017-10-26.
 */

public class SensorContract {

    public static final String AUTHORITY = "com.example.android.sensorsync";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_SENSOR = "sensor";

    /* Inner class that defines the table contents of the sensor table */
    public static final class SensorEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SENSOR).build();
        /* Used internally as the name of our sensor table. */
        public static final String TABLE_NAME = "sensor";

        /*
         * The date column will store the UTC date that correlates to the local date for which
         * each particular sensor row represents. For example, if you live in the Eastern
         * Standard Time (EST) time zone and you load sensor data at 9:00 PM on September 23, 2016,
         * the UTC time stamp for that particular time would be 1474678800000 in milliseconds.
         * However, due to time zone offsets, it would already be September 24th, 2016 in the GMT
         * time zone when it is 9:00 PM on the 23rd in the EST time zone. In this example, the date
         * column would hold the date representing September 23rd at midnight in GMT time.
         * (1474588800000)
         *
         * The reason we store GMT time and not local time is because it is best practice to have a
         * "normalized", or standard when storing the date and adjust as necessary when
         * displaying the date. Normalizing the date also allows us an easy way to convert to
         * local time at midnight, as all we have to do is add a particular time zone's GMT
         * offset to this date to get local time at midnight on the appropriate date.
         */
        public static final String COLUMN_DATE = "date";

        /* ACCELEROMETER_X, ACC_Y, ACC_Z (stored as floats in database) */
        public static final String COLUMN_ACC_X = "acc_x";
        public static final String COLUMN_ACC_Y = "acc_y";
        public static final String COLUMN_ACC_Z = "acc_z";

        /* GYRO_X, GYRO_Y, GYRO_Z (stored as floats in database) */
        public static final String COLUMN_GYRO_X = "gyro_x";
        public static final String COLUMN_GYRO_Y = "gyro_y";
        public static final String COLUMN_GYRO_Z = "gyro_z";

        /* MAGNETOMETER_X, MAGNETO_Y, MAGNETO_Z (stored as floats in database) */
        public static final String COLUMN_MAGNETO_X = "magneto_x";
        public static final String COLUMN_MAGNETO_Y = "magneto_y";
        public static final String COLUMN_MAGNETO_Z = "magneto_z";

        /* Sync Status (stored as int in database) */
        public static final String COLUMN_SYNC_STATUS = "sync_status";

        /* TODO: Unique identifier (stored as INT in the database)*/
        public static final String COLUMN_UID = "uid";


        /* GPS co-ordinates of the mobile device (stored as doubles in the database)*/
        public static final String COLUMN_GPS_LAT = "gps_lat";
        public static final String COLUMN_GPS_LONG = "gps_long";

        /* Classification (stored as int in database) */
        public static final String COLUMN_CLASSIFICATION = "classification";
    }
}
