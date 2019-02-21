package com.ixitask.ixitask.services;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.ixitask.ixitask.activities.HomeActivity;

import static com.ixitask.ixitask.utils.Constants.ACTION_PROCESS_UPDATES;
import static com.ixitask.ixitask.utils.Constants.KEY_FILTER_NAME_UPDATED;
import static com.ixitask.ixitask.utils.Constants.KEY_LOCATION_UPDATES_REQUESTED;

public class LocationRequestHelper {

    private static final String TAG = LocationRequestHelper.class.getSimpleName();
    public static final long LOCATION_UPDATE_INTERVAL = 20*60*1000; //15 minutes
    public static final long LOCATION_SINGLE_UPDATE_INTERVAL = 60*1000; //1 minute
    public static final int REQ_GPS_TRACKING = 11;
    public static final int REQ_GPS_SINGLE = 12;

    /**
     * create a new location request configuration
     * @param interval time for each location update
     * @return the locationrequest object created
     */
    public static LocationRequest getLocationRequest(long interval) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval(interval/2);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setMaxWaitTime(interval*2);
        return locationRequest;
    }

    /**
     * pending intent to LocationUpdatesIntentService to process live location tracking
     * @param context the context where its called
     * @return pendingintent object
     */
    public static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, LocationUpdatesIntentService.class);
        intent.setAction(ACTION_PROCESS_UPDATES);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

//        if device is Android "O" then enable broadcast receiver
//        Intent intent = new Intent(this, LocationUpdatesBroadcastReceiver.class);
//        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
//        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * set the app state of requesting location update
     * @param context the context where its called
     * @param value true if requesting
     */
    public static void setRequesting(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_LOCATION_UPDATES_REQUESTED, value)
                .apply();
    }

    /**
     * display a prompt window to enable gps instantly
     * (based on the priority of LocationRequest configuration object)
     * @param activity the activity where its called and its result to be processed (onActivityResult)
     * @param locationRequest configuration object
     * @param reqCode request code for onActivityResult
     */
    public static void promptEnableGps(Activity activity, LocationRequest locationRequest, int reqCode){
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(result1 -> {
            final Status status = result1.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    Log.i(TAG, "All location settings are satisfied.");
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    Log.i(TAG, "Location settings are not satisfied. " +
                            "Show the user a dialog to upgrade location settings ");
                    try {
                        status.startResolutionForResult(activity, reqCode);
                    } catch (IntentSender.SendIntentException e) {
                        Log.i(TAG, "PendingIntent unable to execute request.");
                        activity.onBackPressed();
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                    break;
            }
        });
    }

    /**
     * to get the state whether the app is requesting location update
     * @param context the context where its called
     * @return true if requesting
     */
    public static boolean getRequesting(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_LOCATION_UPDATES_REQUESTED, false);
    }
}
