package com.ixitask.ixitask.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import id.zelory.compressor.Compressor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.ixitask.ixitask.R;
import com.ixitask.ixitask.models.ResponseImage;
import com.ixitask.ixitask.models.ResponseUpdate;
import com.ixitask.ixitask.services.IxitaskService;
import com.ixitask.ixitask.utils.Constants;
import com.ixitask.ixitask.utils.FileUtils;
import com.ixitask.ixitask.utils.PermissionUtils;
import com.ixitask.ixitask.utils.ViewUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import static com.ixitask.ixitask.utils.Constants.ARG_HP_IMAGE;
import static com.ixitask.ixitask.utils.Constants.ARG_LATITUDE;
import static com.ixitask.ixitask.utils.Constants.ARG_LONGITUDE;

public class MarkActivity extends AppCompatActivity {

    public static final String TAG = MarkActivity.class.getSimpleName();
    private static final int REQ_CAMERA = 10;
    private static final int REQ_LOCATION = 11;
    private static final int REQ_GPLAY_SERVICES = 12;
    private Uri photoUri;
    private AlertDialog dialog;
    private String userId;
    private String userKey;
    private String hpId;
    private double lat;
    private double lon;

    @BindView(R.id.btn_menu_back)
    ImageView btnMenuBack;
    @BindView(R.id.btn_settings)
    ImageView btnSettings;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;
    @BindView(R.id.img_mark)
    ImageView imgMark;
    @BindView(R.id.btn_capture)
    Button btnCapture;
    @BindView(R.id.btn_locate)
    Button btnLocate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark);
        ButterKnife.bind(this);
        checkUserValidity();
        if (getIntent()!=null && getIntent().getExtras()!=null){
            hpId = getIntent().getStringExtra(Constants.ARG_HP_ID);
        }
        setView();
    }

    @Override
    public void onBackPressed() {
        cancelMarkLocation();
    }

    private void setView(){
        getImage();
        swipe.setOnRefreshListener(this::getImage);
        btnMenuBack.setOnClickListener(v->cancelMarkLocation());
        btnSettings.setOnClickListener(v->startActivity(
                new Intent(this, SettingsActivity.class)));
        btnCapture.setOnClickListener(v->startCamera());
        btnLocate.setOnClickListener(v->{
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            try {
                startActivityForResult(builder.build(this), REQ_LOCATION);
            } catch (GooglePlayServicesRepairableException e) {
                Toast.makeText(this, "Please update Google Play Services", Toast.LENGTH_SHORT).show();
                GoogleApiAvailability.getInstance().getErrorDialog(this,
                        e.getConnectionStatusCode(),
                        REQ_GPLAY_SERVICES).show();
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                Toast.makeText(this, "Google Play Services not available", Toast.LENGTH_SHORT).show();
                GoogleApiAvailability.getInstance().getErrorDialog(this,
                        e.errorCode,
                        REQ_GPLAY_SERVICES).show();
                e.printStackTrace();
            }
        });
    }

    private void getImage(){
        onProgressLoading(true);
        IxitaskService.getApi().getImage(userId,userKey,hpId).enqueue(new Callback<ResponseImage>() {
            @Override
            public void onResponse(Call<ResponseImage> call, Response<ResponseImage> response) {
                ResponseImage res = response.body();
                onProgressLoading(false);
                if (res != null) {
                    int status = Integer.parseInt(res.getStatus());
                    Log.d(TAG, res.getStatusMessage());
                    if (status == 200) {
                        List<ResponseImage.Data> data = res.getData();
                        if (data!=null && !data.isEmpty()){
                            String imageUrl = data.iterator().next().getPicture(); //first item
                            if (!TextUtils.isEmpty(imageUrl))
                                Picasso.get().load(imageUrl).into(imgMark);
                        } else {
                            //TODO error no pictures
                        }
                    } else {
                        if (dialog!=null) dialog.dismiss();
                        dialog = ViewUtils.dialogError(MarkActivity.this, res.getStatus(), res.getStatusMessage())
                                .setPositiveButton(getString(R.string.btn_retry), (d, w) -> call.clone().enqueue(this))
                                .create();
                        dialog.show();
                    }
                } else {
                    String message = getString(R.string.error_no_response);
                    Log.d(TAG, message);
                    Log.d(TAG, call.request().toString());
                    if (dialog != null) dialog.dismiss();
                    dialog = ViewUtils.dialogError(MarkActivity.this, "Failed", message)
                            .setPositiveButton(getString(R.string.btn_retry), (d, w) -> call.clone().enqueue(this))
                            .create();
                    dialog.show();
                }
            }

            @Override
            public void onFailure(Call<ResponseImage> call, Throwable t) {
                onProgressLoading(false);
                String message = getString(R.string.error_failed_picture);
                Log.d(TAG, call.request().toString());
                t.printStackTrace();
                if (dialog != null) dialog.dismiss();
                if (!PermissionUtils.isNetworkAvailable(MarkActivity.this))
                    dialog = ViewUtils.dialogError(MarkActivity.this, "Failed",
                            getString(R.string.error_no_internet))
                            .setPositiveButton(getString(R.string.btn_retry),
                                    (d, w) -> call.clone().enqueue(this))
                            .create();
                else
                    dialog = ViewUtils.dialogError(MarkActivity.this, "Failed", String.format(message, t.getMessage()))
                            .setPositiveButton(getString(R.string.btn_retry), (d, w) -> call.clone().enqueue(this))
                            .create();
                dialog.show();
            }
        });
    }

    private void uploadImage(){
        if (photoUri==null) return;
        Toast.makeText(this, "Uploading picture...", Toast.LENGTH_SHORT).show();
        String imageStr;
        try {
            File compressedFile = new Compressor(this)
                    .compressToFile(new File(FileUtils.getPath(this, photoUri)));
            imageStr = FileUtils.encodeFileToString(this, Uri.fromFile(compressedFile));
        } catch (Exception e) {
            e.printStackTrace();
            imageStr = FileUtils.encodeFileToString(this, photoUri);
        }
        onProgressLoading(true);
        IxitaskService.getApi().uploadImage(userId,userKey,hpId,imageStr).enqueue(new Callback<ResponseUpdate>() {
            @Override
            public void onResponse(Call<ResponseUpdate> call, Response<ResponseUpdate> response) {
                onProgressLoading(false);
                ResponseUpdate res = response.body();
                Log.d(TAG, call.request().toString());
                if (res != null) {
                    Log.d(TAG, res.getStatusMessage());
                    int status = Integer.parseInt(res.getStatus());
                    if (status == 200) {
                        getImage();
                        Toast.makeText(MarkActivity.this, "Upload success!", Toast.LENGTH_SHORT).show();
                    } else {
                        if (dialog != null) dialog.dismiss();
                        dialog = ViewUtils.dialogError(MarkActivity.this, res.getStatus(),
                                res.getStatusMessage())
                                .setPositiveButton(getString(R.string.btn_retry), (d, w) -> call.clone().enqueue(this))
                                .create();
                        dialog.show();
                    }
                } else {
                    String message = getString(R.string.error_no_response);
                    Log.d(TAG, message);
                    if (dialog != null) dialog.dismiss();
                    dialog = ViewUtils.dialogError(MarkActivity.this, "Failed", message)
                            .setPositiveButton(getString(R.string.btn_retry), (d, w) -> call.clone().enqueue(this))
                            .create();
                    dialog.show();
                }
            }

            @Override
            public void onFailure(Call<ResponseUpdate> call, Throwable t) {
                onProgressLoading(false);
                String message = getString(R.string.error_failed_upload);
                Log.d(TAG, call.request().toString());
                t.printStackTrace();
                if (dialog != null) dialog.dismiss();
                if (!PermissionUtils.isNetworkAvailable(MarkActivity.this))
                    dialog = ViewUtils.dialogError(MarkActivity.this, "Failed",
                            getString(R.string.error_no_internet))
                            .setPositiveButton(getString(R.string.btn_retry),
                                    (d, w) -> call.clone().enqueue(this))
                            .create();
                else
                    dialog = ViewUtils.dialogError(MarkActivity.this, "Failed", String.format(message, t.getMessage()))
                            .setPositiveButton(getString(R.string.btn_retry), (d, w) -> call.clone().enqueue(this))
                            .create();
                dialog.show();
            }
        });
//        Data input = new Data.Builder()
//                .putString(Constants.ARG_HP_IMAGE_URI, photoUri.toString()).build();
//        OneTimeWorkRequest encodeWork = new OneTimeWorkRequest.Builder(EncodeImageWorker.class)
//                .setInputData(input)
//                .build();
//        WorkManager.getInstance().enqueue(encodeWork);
//        try {
//            WorkInfo workInfo = WorkManager.getInstance().getWorkInfoById(encodeWork.getId()).get();
//            if (workInfo!=null){
//                String imageStr = workInfo.getOutputData().getString(ARG_HP_IMAGE);
//
//            }
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    private void cancelMarkLocation(){
        Intent registerActivity = new Intent(this, RegisterActivity.class);
        setResult(RESULT_CANCELED, registerActivity);
        finish();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(Constants.ARG_USER_ID, userId);
        outState.putString(Constants.ARG_USER_KEY, userKey);
        outState.putString(Constants.ARG_HP_ID, hpId);
        outState.putDouble(Constants.ARG_LATITUDE, lat);
        outState.putDouble(Constants.ARG_LONGITUDE, lon);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState!=null) {
            userId = savedInstanceState.getString(Constants.ARG_USER_ID);
            userKey = savedInstanceState.getString(Constants.ARG_USER_KEY);
            hpId = savedInstanceState.getString(Constants.ARG_HP_ID);
            lat = savedInstanceState.getDouble(Constants.ARG_LATITUDE);
            lon = savedInstanceState.getDouble(Constants.ARG_LONGITUDE);
        }
    }

    public void startCamera() {
        if (PermissionUtils.isPermissionGranted(this, this,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            photoUri = FileUtils.getUriNewFile(this, FileUtils.getNewFile(this));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, REQ_CAMERA);
        }
    }

    private boolean checkUserValidity(){
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        userId = sPrefs.getString(Constants.ARG_USER_ID, "");
        userKey = sPrefs.getString(Constants.ARG_USER_KEY, "");
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(userKey)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return false;
        }
        return true;
    }

    private void onProgressLoading(boolean isLoading) {
        swipe.post(()->swipe.setRefreshing(isLoading));
//        if (isLoading) ViewUtils.hideKeyboard(this, getCurrentFocus());
    }

    private void sendResultAndFinish(){
        Intent registerActivity = new Intent(this, RegisterActivity.class);
        registerActivity.putExtra(ARG_LATITUDE, lat);
        registerActivity.putExtra(ARG_LONGITUDE, lon);
        setResult(RESULT_OK, registerActivity);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_LOCATION) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Place place = PlacePicker.getPlace(this, data);
                    lat = place.getLatLng().latitude;
                    lon = place.getLatLng().longitude;
                    String toastMsg = String.format("Place: %s", place.getName());
                    Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                    sendResultAndFinish();
                } else
                    Toast.makeText(this, "Location data not found", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(this, "Location must be chosen", Toast.LENGTH_SHORT).show();
        }

        if (requestCode == REQ_CAMERA) {
            if (resultCode == RESULT_OK) {
                if (photoUri!=null){
                    uploadImage();
                    Picasso.get()
                        .load(FileUtils.getPath(this, photoUri))
                        .into(imgMark);
                }
            }
        }
    }
}
