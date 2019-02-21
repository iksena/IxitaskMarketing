package com.ixitask.ixitask.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.ixitask.ixitask.activities.LoginActivity;
import com.ixitask.ixitask.models.ResponseUpdate;
import com.ixitask.ixitask.services.IxitaskService;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PermissionUtils {

    private static final String TAG = PermissionUtils.class.getSimpleName();

    public static String[] allPermissions = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    /**
     * check if some permissions is allowed by user
     * @param context context where the method is called
     * @param activity activity where the method is called
     * @param permissions the permissions to be checked
     * @return true if all permission are allowed
     */
    public static boolean isPermissionGranted(Context context, Activity activity, String... permissions){
        try {
            for (String s : permissions){
                if (ContextCompat.checkSelfPermission(context, s) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, permissions, Constants.REQ_PERMISSION);
                    return false;
                }
            }
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
