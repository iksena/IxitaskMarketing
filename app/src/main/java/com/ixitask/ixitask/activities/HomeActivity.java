package com.ixitask.ixitask.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ixitask.ixitask.R;
import com.ixitask.ixitask.fragments.ActivityFragment;
import com.ixitask.ixitask.fragments.HomeSummaryFragment;
import com.ixitask.ixitask.fragments.HomepassFragment;
import com.ixitask.ixitask.fragments.InstallDetailFragment;
import com.ixitask.ixitask.fragments.InstallationFragment;
import com.ixitask.ixitask.fragments.LogsFragment;
import com.ixitask.ixitask.fragments.SummaryFragment;
import com.ixitask.ixitask.models.ResponseHomepass;
import com.ixitask.ixitask.models.ResponseSummary;
import com.ixitask.ixitask.models.ResponseSummaryRes;
import com.ixitask.ixitask.models.ResponseUpdate;
import com.ixitask.ixitask.services.IxitaskService;
import com.ixitask.ixitask.services.LocationRequestHelper;
import com.ixitask.ixitask.utils.Constants;
import com.ixitask.ixitask.utils.PermissionUtils;
import com.ixitask.ixitask.utils.ViewUtils;

import java.util.Calendar;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.ixitask.ixitask.services.LocationRequestHelper.LOCATION_SINGLE_UPDATE_INTERVAL;
import static com.ixitask.ixitask.services.LocationRequestHelper.LOCATION_UPDATE_INTERVAL;
import static com.ixitask.ixitask.services.LocationRequestHelper.REQ_GPS_SINGLE;
import static com.ixitask.ixitask.services.LocationRequestHelper.REQ_GPS_TRACKING;
import static com.ixitask.ixitask.services.LocationRequestHelper.getLocationRequest;
import static com.ixitask.ixitask.services.LocationRequestHelper.getPendingIntent;
import static com.ixitask.ixitask.services.LocationRequestHelper.promptEnableGps;
import static com.ixitask.ixitask.utils.PermissionUtils.isPermissionGranted;

public class HomeActivity extends AppCompatActivity implements
        HomepassFragment.OnHomepassInteractionListener,
        ActivityFragment.OnActivityInteractionListener,
        LogsFragment.OnLogsInteractionListener,
        InstallationFragment.OnInstallationInteractionListener,
        SummaryFragment.OnSummaryInteractionListener,
        NavigationView.OnNavigationItemSelectedListener,
        InstallDetailFragment.OnListFragmentInteractionListener {

    private static final String TAG = HomeActivity.class.getSimpleName();
    private String userId;
    private String userKey;
    private String username;
    private ResponseHomepass.Homepass homepass;
    private int hpscId;
    private String hpscName;
    private String serviceId;
    private FusedLocationProviderClient locationProvider;

    @BindView(R.id.drawer)
    DrawerLayout drawer;
    @BindView(R.id.nav_view)
    NavigationView navigation;
    @BindView(R.id.fragment_navigation)
    FrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        navigation.setNavigationItemSelectedListener(this);

        checkUserValidity();
        if (!isPermissionGranted(this, this,
                PermissionUtils.allPermissions)) {
            if (getIntent() == null) finish(); //if doesn't come from LoginActivity
        }
        ViewUtils.hideKeyboard(this, getCurrentFocus());

        //TO DO ask to enable GPS
        promptEnableGps(this, getLocationRequest(LOCATION_UPDATE_INTERVAL), REQ_GPS_TRACKING);
        locationProvider = LocationServices.getFusedLocationProviderClient(this);
        navigation.setCheckedItem(R.id.nav_homepass);
        onChangeFragment(R.id.nav_homepass);


    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager
            .getDefaultSharedPreferences(this)
            .edit()
            .putString(Constants.ARG_USER_ID, userId)
            .putString(Constants.ARG_USER_KEY, userKey)
            .putString(Constants.ARG_USERNAME, username)
            .apply();
        refreshFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserValidity();
        handleLocationUpdate();

        //TO DO expiration time bomb
//        Calendar expirationDate = Calendar.getInstance();
//        expirationDate.set(2019, Calendar.FEBRUARY , 28);
//        if (Calendar.getInstance().compareTo(expirationDate) > 0) {
//            Toast.makeText(this,
//                    "Your testing session has expired. Please contact the developer.",
//                    Toast.LENGTH_SHORT).show();
//            finish();
//        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_navigation);
            if (current instanceof HomepassFragment){
                moveTaskToBack(true);
            } else if (current instanceof ActivityFragment){
                onChangeFragment(R.id.nav_homepass);
                navigation.setCheckedItem(R.id.nav_homepass);
            } else if (current instanceof LogsFragment){
                onChangeFragment(R.id.nav_homepass);
                navigation.setCheckedItem(R.id.nav_homepass);
            } else if (current instanceof InstallationFragment){
                onChangeFragment(R.id.nav_homepass);
                navigation.setCheckedItem(R.id.nav_homepass);
            } else if (current instanceof SummaryFragment){
                onChangeFragment(R.id.nav_homepass);
                navigation.setCheckedItem(R.id.nav_homepass);
            } else if (current instanceof HomeSummaryFragment){
                onChangeFragment(R.id.nav_summary);
                navigation.setCheckedItem(R.id.nav_summary);
            } else if (current instanceof InstallDetailFragment){
                onChangeFragment(R.id.nav_installation);
                navigation.setCheckedItem(R.id.nav_installation);
            } else {
                onChangeFragment(R.id.nav_homepass);
                navigation.setCheckedItem(R.id.nav_homepass);
            }
//            if (navigation.getCheckedItem() != null) {
//                switch (navigation.getCheckedItem().getItemId()) {
//                    case R.id.nav_homepass:
//                        moveTaskToBack(true);
//                        break;
//                    case R.id.nav_installation:
//                        navigation.setCheckedItem(R.id.nav_homepass);
//                        break;
//                    case R.id.nav_summary:
//                        navigation.setCheckedItem(R.id.nav_homepass);
//                        break;
//                    default:
//                        moveTaskToBack(true);
//                        break;
//                }
//            }
        }
    }

    /**
     * validate user id and key stored in app
     * @return true if valid
     */
    private void checkUserValidity(){
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        userId = sPrefs.getString(Constants.ARG_USER_ID, "");
        userKey = sPrefs.getString(Constants.ARG_USER_KEY, "");
        username = sPrefs.getString(Constants.ARG_USERNAME, "");
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(userKey)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            String firebaseToken = sPrefs.getString(Constants.ARG_FIREBASE, "");
            if (TextUtils.isEmpty(firebaseToken)) {
                FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            String token = task.getResult().getToken();
                            Log.d(TAG, "token = " + token);
                            sPrefs.edit().putString(Constants.ARG_FIREBASE, token).apply();
                            IxitaskService.getApi().updateToken(userId, userKey, token).enqueue(new Callback<ResponseUpdate>() {
                                @Override
                                public void onResponse(Call<ResponseUpdate> call, Response<ResponseUpdate> response) {
                                    ResponseUpdate res = response.body();
                                    if (res != null) {
                                        Log.d(TAG, res.getStatus() + ": " + res.getStatusMessage());
                                    } else {
                                        ViewUtils.dialogError(HomeActivity.this, "Failed",
                                                "Cannot send device token to server")
                                                .setPositiveButton(getString(R.string.btn_retry),
                                                        (d, w) -> call.clone().enqueue(this))
                                                .create().show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseUpdate> call, Throwable t) {
                                    t.printStackTrace();
                                    if (!PermissionUtils.isNetworkAvailable(HomeActivity.this))
                                        ViewUtils.dialogError(HomeActivity.this, "Failed",
                                                getString(R.string.error_no_internet))
                                                .setPositiveButton(getString(R.string.btn_retry),
                                                        (d, w) -> call.clone().enqueue(this))
                                                .create().show();
                                    else
                                        ViewUtils.dialogError(HomeActivity.this, "Failed",
                                                "Cannot send device token to server")
                                                .setPositiveButton(getString(R.string.btn_retry),
                                                        (d, w) -> call.clone().enqueue(this))
                                                .create().show();
                                }
                            });
                        } else
                            Log.d(TAG, "Failed retrieving Firebase token", task.getException());
                    } else
                        Log.d(TAG, "Failed retrieving Firebase token", task.getException());
                });
            }
        }
    }

    /**
     * handling live location tracking based on user settings
     */
    private void handleLocationUpdate(){
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isTrackingLocation = sPrefs.getBoolean(getString(R.string.preference_key_location), true);
        if (isTrackingLocation) {
            requestLocationUpdate();
        } else {
            removeLocationUpdate();
        }
    }

    /**
     * run live location tracking on background
     */
    @SuppressLint("MissingPermission")
    private void requestLocationUpdate() {
        LocationRequestHelper.setRequesting(this, true);
        if (isPermissionGranted(this, this, PermissionUtils.allPermissions)){
            locationProvider.requestLocationUpdates(getLocationRequest(LOCATION_UPDATE_INTERVAL),
                    getPendingIntent(this));
//            OneTimeWorkRequest.Builder locationBuilder =
//                    new OneTimeWorkRequest.Builder(LocationWorker.class)
//                            .setConstraints(new Constraints.Builder()
//                                    .setRequiredNetworkType(NetworkType.CONNECTED)
//                                    .build());
//            OneTimeWorkRequest locationWork = locationBuilder.build();
//            WorkManager.getInstance().enqueueUniqueWork(TAG_LOCATION_WORK,
//                    ExistingWorkPolicy.REPLACE, locationWork);
        }
    }

    /**
     * stop live location tracking on background
     */
    private void removeLocationUpdate(){
        LocationRequestHelper.setRequesting(this, false);
        locationProvider.removeLocationUpdates(getPendingIntent(this));
//        WorkManager.getInstance().cancelUniqueWork(TAG_LOCATION_WORK);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        drawer.closeDrawer(GravityCompat.START);
        return onChangeFragment(item.getItemId());
//        Fragment fragment;
//        switch (item.getItemId()) {
//            case R.id.nav_homepass:
//                fragment = HomepassFragment.newInstance(userId, userKey);
//                break;
//            case R.id.nav_installation:
//                fragment = InstallationFragment.newInstance(userId,userKey);
//                break;
//            case R.id.nav_summary:
//                fragment = SummaryFragment.newInstance(userId,userKey);
//                break;
//            default:
//                fragment = HomepassFragment.newInstance(userId, userKey);
//                break;
//        }
//        if (fragment!=null) {
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_navigation, fragment)
//                    .commit();
//            drawer.closeDrawer(GravityCompat.START);
//            return true;
//        }
    }

    /**
     * refresh the current fragment displayed
     */
    private void refreshFragment(){
        onProgressLoading(true);
        String message = getString(R.string.error_no_response);
        //getting current fragment from the fragment container layout
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_navigation);
        if (current != null){
            if (current instanceof HomepassFragment){
                ((HomepassFragment) current).setView();
            } else if (current instanceof ActivityFragment){
                ((ActivityFragment) current).setView();
            } else if (current instanceof LogsFragment){
                ((LogsFragment) current).setView();
            } else if (current instanceof InstallationFragment){
                ((InstallationFragment) current).setView();
            } else if (current instanceof SummaryFragment){
                ((SummaryFragment) current).setView();
            } else if (current instanceof InstallDetailFragment){
                ((InstallDetailFragment) current).setView();
            } else if (current instanceof HomeSummaryFragment){
                ((HomeSummaryFragment) current).setView();
            } else {
                Log.d(TAG, message);
                ViewUtils.dialogError(this,"Failed", message).create().show();
                onProgressLoading(false);
            }
        } else {
            Log.d(TAG, message);
            ViewUtils.dialogError(this,"Failed", message).create().show();
            onProgressLoading(false);
        }

    }

    @Override
    public void onHomepassClick(ResponseHomepass.Homepass homepass) {
        this.homepass = homepass;
        onChangeFragment(R.id.navigation_activity);
    }

    @Override
    public void onHomepassClick(ResponseSummaryRes.SummaryRes homeSummary) {
        this.homepass = new ResponseHomepass.Homepass();
        homepass.setHpid(homeSummary.hpid);
        homepass.setComplex(homeSummary.streetName);
        homepass.setContactby(homeSummary.contactby);
        homepass.setContactdate(homeSummary.contactdate);
        homepass.setOpen("false");
        homepass.setOwner(homeSummary.owner);
        homepass.setStreetName(homeSummary.streetName);
        homepass.setPhone(homeSummary.phone);
        onChangeFragment(R.id.navigation_logs);
    }

    @Override
    public void openSideNavigation(boolean wantOpen) {
        if (wantOpen)
            drawer.openDrawer(GravityCompat.START);
        else
            drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onBackClick() {
        onBackPressed();
    }

    @Override
    public void onClick(String serviceId) {
        this.serviceId = serviceId;
        onChangeFragment(R.id.nav_install_detail);
    }

    @Override
    public void onActivitySubmit(String hpId) {
        if (Objects.equals(hpId, homepass.getHpid()))
            onChangeFragment(R.id.navigation_logs);
        else
            Toast.makeText(this, R.string.error_no_homepass, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSummaryClick(ResponseSummary.Summary summary) {
        hpscId = Integer.parseInt(summary.hpscid);
        hpscName = summary.hpscnote;
        onChangeFragment(R.id.nav_summary_homepass);
    }

    @Override
    public void onProgressLoading(boolean isLoading) {
//        swipeRefresh.post(()->swipeRefresh.setRefreshing(isLoading));
//        if (isLoading) ViewUtils.hideKeyboard(this, getCurrentFocus());
    }

    @Override
    public boolean onChangeFragment(int fragmentId) {
        Fragment fragment = null;
        switch (fragmentId) {
            case R.id.nav_homepass:
                fragment = HomepassFragment.newInstance(userId, userKey);
                break;
            case R.id.navigation_activity:
                if (!isPermissionGranted(this, this,
                        PermissionUtils.allPermissions)){
                    Toast.makeText(this, R.string.error_permission, Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (homepass!=null)
                    fragment = ActivityFragment.newInstance(userId, userKey,
                            homepass.getHpid(),
                            homepass.getStreetName(),
                            homepass.getOwner(),
                            homepass.getPhone(),
                            Boolean.parseBoolean(homepass.getOpen()));
                else
                    Toast.makeText(this, R.string.error_no_homepass, Toast.LENGTH_SHORT).show();
                break;
            case R.id.navigation_logs:
                if (homepass!=null)
                    fragment = LogsFragment.newInstance(userId, userKey,
                            homepass.getHpid(),
                            homepass.getStreetName(),
                            homepass.getOwner(),
                            homepass.getPhone());
                else
                    Toast.makeText(this, R.string.error_no_homepass, Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_installation:
                fragment = InstallationFragment.newInstance(userId,userKey);
                break;
            case R.id.nav_summary:
                fragment = SummaryFragment.newInstance(userId,userKey,username);
                break;
            case R.id.nav_summary_homepass:
                fragment = HomeSummaryFragment.newInstance(userId,userKey,hpscId,hpscName);
                break;
            case R.id.nav_install_detail:
                fragment = InstallDetailFragment.newInstance(userId,userKey,serviceId);
        }
        if (fragment!=null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_navigation, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == REQ_GPS_TRACKING){
//            if (resultCode == RESULT_OK){
//                handleLocationUpdate(); //live location tracking
//            } else {
//                promptEnableGps(this, getLocationRequest(LOCATION_UPDATE_INTERVAL),REQ_GPS_TRACKING);
//            }
//        } else
        if (requestCode == REQ_GPS_SINGLE){
            if (resultCode == RESULT_OK){
                refreshFragment(); //single location for activity form submission
            } else {
                promptEnableGps(this, getLocationRequest(LOCATION_SINGLE_UPDATE_INTERVAL), REQ_GPS_SINGLE);
            }
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onListFragmentInteraction(String imageUrl) {
        Intent photoIntent = new Intent(this, PhotoActivity.class);
        photoIntent.putExtra(Constants.ARG_IMAGE_URL, imageUrl);
        startActivity(photoIntent);
    }
}
