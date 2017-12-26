package com.example.android.sensorsync.Sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.example.android.sensorsync.data.SensorContract;

import java.util.ArrayList;

/**
 * Created by sandeepchawan on 2017-10-26.
 */

public class SensorTask {

    synchronized public static void syncSensor(Context context, ArrayList<ContentValues> values) {
        Log.d("SensorTask", "&&&&&&&&&&&& Calling syncSensor &&&&&&&&&&&&");
        try {

            if (values != null && values.size() != 0) {
                /* Get a handle on the ContentResolver to insert data */
                ContentResolver sensorContentResolver = context.getContentResolver();

                /* Convert the arraylist to normal array */
                ContentValues[] cvalues = new ContentValues[values.size()];
                for (int i=0; i<values.size(); i++) cvalues[i] = values.get(i);

                /* Insert our new sensor data into Sensor's ContentProvider */
                sensorContentResolver.bulkInsert(
                        SensorContract.SensorEntry.CONTENT_URI,
                        cvalues);

            }

            Log.d("SensorTask", "&&&&&&&&&&&& Bulk Insert completed &&&&&&&&&&&&");
            /* If the code reaches this point, we have successfully performed our insert into localDB */

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}