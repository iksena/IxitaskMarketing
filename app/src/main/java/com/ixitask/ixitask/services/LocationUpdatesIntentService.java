package com.ixitask.ixitask.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.LocationResult;
import com.ixitask.ixitask.models.ResponseUpdate;
import com.ixitask.ixitask.utils.Constants;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.ixitask.ixitask.utils.Constants.ACTION_PROCESS_UPDATES;

public class LocationUpdatesIntentService extends IntentService {
    private static final String TAG = LocationUpdatesIntentService.class.getSimpleName();
    private String userId;
    private String userKey;

    public LocationUpdatesIntentService() {
        // Name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        userId = sPrefs.getString(Constants.ARG_USER_ID, "");
        userKey = sPrefs.getString(Constants.ARG_USER_KEY, "");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    List<Location> locations = result.getLocations();
                    LocationResultHelper locationResultHelper = new LocationResultHelper(this,
                            locations);
                    locationResultHelper.saveResults();
                    Location location = locations.get(0);
                    if (location != null) {
                        updateServer(location.getLatitude(), location.getLongitude());
                    }
                    locationResultHelper.showNotification();
                    Log.i(TAG, LocationResultHelper.getSavedLocationResult(this));
                }
            }
        }
    }

    /**
     * POST to ixitask server in background whenever a new location is acquired
     * @param lat latitude
     * @param lon longitude
     */
    public void updateServer(double lat, double lon) {
        IxitaskService.getApi().updateLocation(userId, userKey, lat, lon)
                .enqueue(new Callback<ResponseUpdate>() {
            @Override
            public void onResponse(Call<ResponseUpdate> call, Response<ResponseUpdate> response) {
                ResponseUpdate res = response.body();
                if (res != null){
                    Log.d(TAG, res.getStatus()+": "+res.getStatusMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseUpdate> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
