package com.ixitask.ixitask.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.common.util.concurrent.ListenableFuture;
import com.ixitask.ixitask.R;
import com.ixitask.ixitask.models.ResponseUpdate;
import com.ixitask.ixitask.utils.Constants;
import com.ixitask.ixitask.utils.ViewUtils;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.ResolvableFuture;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import androidx.work.impl.utils.futures.SettableFuture;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.ixitask.ixitask.services.LocationRequestHelper.LOCATION_SINGLE_UPDATE_INTERVAL;
import static com.ixitask.ixitask.services.LocationRequestHelper.getLocationRequest;
import static com.ixitask.ixitask.utils.Constants.TAG_LOCATION_WORK;

public class LocationWorker extends ListenableWorker {

    public static final String TAG = LocationWorker.class.getSimpleName();

    private Context context;
    private String userId;
    private String userKey;
    private ResolvableFuture<Result> result;
    private FusedLocationProviderClient locationProvider;

    public LocationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        locationProvider = LocationServices.getFusedLocationProviderClient(context);
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        userId = sPrefs.getString(Constants.ARG_USER_ID, "");
        userKey = sPrefs.getString(Constants.ARG_USER_KEY, "");
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        result = ResolvableFuture.create();
        if (context == null) context = getApplicationContext();
        LocationCallback locationCallback = new LocationCallback(){
            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                if (!locationAvailability.isLocationAvailable()){
                    Toast.makeText(context, "Please enable GPS", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult!=null){
                    Location l = locationResult.getLastLocation();
                    double lat = l.getLatitude();
                    double lon = l.getLongitude();
                    LocationResultHelper locationResultHelper = new LocationResultHelper(context,
                            locationResult.getLocations());
                    locationResultHelper.saveResults();
                    updateServer(lat, lon);
                    locationResultHelper.showNotification();
                    locationProvider.removeLocationUpdates(this);
                } else {
                    Toast.makeText(context, "Please enable GPS", Toast.LENGTH_SHORT).show();
                    WorkManager.getInstance().cancelUniqueWork(TAG_LOCATION_WORK);
                    result.set(Result.failure());
                }
            }
        };
        LocationRequest locationRequest = getLocationRequest(LOCATION_SINGLE_UPDATE_INTERVAL);
        try {
            LocationServices.getFusedLocationProviderClient(context)
                    .requestLocationUpdates(locationRequest, locationCallback,null);
        } catch (SecurityException e){
            Toast.makeText(context, "Please give permission to get location", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            WorkManager.getInstance().cancelUniqueWork(TAG_LOCATION_WORK);
            result.set(Result.failure());
        }
        return result;
    }

    @Override
    public void onStopped() {
        super.onStopped();
    }

    public void updateServer(double lat, double lon) {
        if (context == null) context = getApplicationContext();
        IxitaskService.getApi().updateLocation(userId, userKey, lat, lon)
                .enqueue(new Callback<ResponseUpdate>() {
                    @Override
                    public void onResponse(Call<ResponseUpdate> call, Response<ResponseUpdate> response) {
                        ResponseUpdate res = response.body();
                        if (res != null){
                            Log.d(TAG, res.getStatus()+": "+res.getStatusMessage());
                            OneTimeWorkRequest.Builder locationBuilder =
                                    new OneTimeWorkRequest.Builder(LocationWorker.class)
                                            .setConstraints(new Constraints.Builder()
                                                    .setRequiredNetworkType(NetworkType.CONNECTED)
                                                    .build())
                                            .setInitialDelay(1, TimeUnit.HOURS);
                            OneTimeWorkRequest locationWork = locationBuilder.build();
                            WorkManager.getInstance().enqueueUniqueWork(TAG_LOCATION_WORK,
                                    ExistingWorkPolicy.REPLACE, locationWork);
                            result.set(Result.success());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseUpdate> call, Throwable t) {
                        t.printStackTrace();
                        WorkManager.getInstance().cancelUniqueWork(TAG_LOCATION_WORK);
                        result.set(Result.failure());
                    }
                });
    }


}
