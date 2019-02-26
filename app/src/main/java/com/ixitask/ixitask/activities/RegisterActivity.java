package com.ixitask.ixitask.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.ixitask.ixitask.R;
import com.ixitask.ixitask.fragments.ProductAdapter;
import com.ixitask.ixitask.models.ResponseProduct;
import com.ixitask.ixitask.models.ResponseUpdate;
import com.ixitask.ixitask.services.IxitaskService;
import com.ixitask.ixitask.utils.Constants;
import com.ixitask.ixitask.utils.FileUtils;
import com.ixitask.ixitask.utils.PermissionUtils;
import com.ixitask.ixitask.utils.ViewUtils;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements ProductAdapter.OnProductInteractionListener {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    private static final int REQ_SLOT = 10;
    private static final int REQ_LOCATION = 11;
    private String userId;
    private String userKey;
    private String hpId;
    private String streetName;
    private String ownerName;
    private String phone;
    private String dateChosen;
    private String slotId;
    private String slotName;
    private double lat;
    private double lon;
    private boolean isLocation = false;
    private List<ResponseProduct.Product> productsAvailable;
    private List<ResponseProduct.Product> productsChosen;
    private AlertDialog dialog;
    private int monthlyFee = 0;
    private int installFee = 0;
    private SimpleDateFormat sdf;

    @BindView(R.id.text_street)
    TextView textStreet;
    @BindView(R.id.text_desc)
    TextView textDesc;
    @BindView(R.id.edit_date)
    EditText editDate;
    @BindView(R.id.edit_name)
    EditText editName;
    @BindView(R.id.btn_slot)
    ImageButton btnSlot;
    @BindView(R.id.spinner_products)
    SearchableSpinner spinProducts;
    @BindView(R.id.rv_products)
    RecyclerView rvProducts;
    @BindView(R.id.text_fee_monthly)
    TextView textMonthly;
    @BindView(R.id.text_fee_prorate)
    TextView textProrate;
    @BindView(R.id.text_fee_install)
    TextView textInstall;
    @BindView(R.id.btn_locate)
    Button btnLocate;
    @BindView(R.id.btn_submit)
    Button btnSubmit;
    @BindView(R.id.btn_back)
    ImageView btnBack;
    @BindView(R.id.btn_settings)
    ImageView btnSettings;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        checkUserValidity();
        if (getIntent() != null) {
            if (getIntent().getExtras()!=null) {
                hpId = getIntent().getStringExtra(Constants.ARG_HP_ID);
                streetName = getIntent().getStringExtra(Constants.ARG_HP_STREET);
                ownerName = getIntent().getStringExtra(Constants.ARG_HP_OWNER);
                phone = getIntent().getStringExtra(Constants.ARG_HP_PHONE);
            }
        }
        setView();
        sdf = new SimpleDateFormat(getString(R.string.format_date_server), Locale.getDefault());
        dateChosen = sdf.format(new Date());
        swipe.setOnRefreshListener(()->{
            new AlertDialog.Builder(RegisterActivity.this)
                    .setTitle("Refresh")
                    .setMessage("Apakah Anda yakin ingin memuat ulang dan mengisi ulang data?")
                    .setPositiveButton("Ya", (d,w)->setView())
                    .setNegativeButton("Tidak", (d,w)->d.dismiss())
                    .create().show();
        });
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
//        SharedPreferences.Editor edit = PreferenceManager
//                .getDefaultSharedPreferences(this)
//                .edit();
        outState.putString(Constants.ARG_HP_ID, hpId);
        outState.putString(Constants.ARG_HP_STREET, streetName);
        outState.putString(Constants.ARG_HP_OWNER, ownerName);
        outState.putString(Constants.ARG_HP_PHONE, phone);
        outState.putString(Constants.ARG_DATE, dateChosen);
        outState.putString(Constants.ARG_SLOT_ID, slotId);
        outState.putString(Constants.ARG_SLOT_NAME, slotName);
        outState.putDouble(Constants.ARG_LATITUDE,lat);
        outState.putDouble(Constants.ARG_LONGITUDE,lon);
//        edit.apply();
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        userId = sPrefs.getString(Constants.ARG_USER_ID,"");
        userKey = sPrefs.getString(Constants.ARG_USER_KEY,"");
        hpId = savedInstanceState.getString(Constants.ARG_HP_ID,"");
        streetName = savedInstanceState.getString(Constants.ARG_HP_STREET,"");
        ownerName = savedInstanceState.getString(Constants.ARG_HP_OWNER,"");
        phone = savedInstanceState.getString(Constants.ARG_HP_PHONE,"");
        dateChosen = savedInstanceState.getString(Constants.ARG_DATE,"");
        slotId = savedInstanceState.getString(Constants.ARG_SLOT_ID,"");
        slotName = savedInstanceState.getString(Constants.ARG_SLOT_NAME,"");
        lat = savedInstanceState.getDouble(Constants.ARG_LATITUDE);
        lon = savedInstanceState.getDouble(Constants.ARG_LONGITUDE);
    }

    @Override
    public void onBackPressed() {
        finishAndClearData();
    }

    private void finishAndClearData(){
        SharedPreferences.Editor edit = PreferenceManager
                .getDefaultSharedPreferences(this)
                .edit();
        edit.remove(Constants.ARG_HP_ID)
            .remove(Constants.ARG_HP_STREET)
            .remove(Constants.ARG_HP_OWNER)
            .remove(Constants.ARG_HP_PHONE)
            .remove(Constants.ARG_DATE)
            .remove(Constants.ARG_SLOT_ID)
            .remove(Constants.ARG_SLOT_NAME)
            .apply();
        Toast.makeText(this, "Register success!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void setView(){
        ViewUtils.hideKeyboard(this, editDate);
        ViewUtils.hideKeyboard(this, editName);
        textStreet.setText(streetName);
        textDesc.setText(getString(R.string.activity_bar_desc, ownerName, phone));
        editName.setText(ownerName);
        btnSubmit.setEnabled(false);
        spinProducts.setEnabled(false);
        getProductList(); //for spinner
        productsChosen = new ArrayList<>(); //for recyclerview
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(new ProductAdapter(this, productsChosen,this));
        spinProducts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (productsAvailable != null && !productsAvailable.isEmpty())
                    addChosenProduct(productsAvailable.get(position));
                else
                    getProductList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        editDate.setOnClickListener(v->
            startActivityForResult(new Intent(this, SlotActivity.class), REQ_SLOT));
        btnSlot.setOnClickListener(v->
                startActivityForResult(new Intent(this, SlotActivity.class), REQ_SLOT));
        btnLocate.setOnClickListener(v->{
            Intent markIntent = new Intent(this, MarkActivity.class);
            markIntent.putExtra(Constants.ARG_HP_ID, hpId);
            startActivityForResult(markIntent, REQ_LOCATION);
        });
        btnSubmit.setOnClickListener(v->{
            new AlertDialog.Builder(RegisterActivity.this)
                    .setTitle("Registrasi")
                    .setMessage("Apakah Anda yakin ingin melakukan registrasi?")
                    .setPositiveButton("Ya", (d,w)->attemptRegister())
                    .setNegativeButton("Tidak", (d,w)->d.dismiss())
                    .create().show();
        });
        btnBack.setOnClickListener(v->onBackPressed());
        btnSettings.setOnClickListener(v->startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void getProductList(){
        onProgressLoading(true);
        IxitaskService.getApi().getProducts(userId,userKey).enqueue(new Callback<ResponseProduct>() {
            @Override
            public void onResponse(Call<ResponseProduct> call, Response<ResponseProduct> response) {
                ResponseProduct res = response.body();
                onProgressLoading(false);
                if (res!=null){
                    int status = Integer.parseInt(res.status);
                    Log.d(TAG, res.statusMessage);
                    if (status == 200) {
                        productsAvailable = res.data.products;
                        ArrayAdapter<ResponseProduct.Product> adapter =
                                new ArrayAdapter<>(RegisterActivity.this,
                                android.R.layout.simple_spinner_dropdown_item, productsAvailable);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinProducts.setAdapter(adapter);
                        spinProducts.setEnabled(true);
                        btnSubmit.setEnabled(true);
                    } else {
                        if (dialog!=null) dialog.dismiss();
                        dialog = ViewUtils.dialogError(RegisterActivity.this, res.status, res.statusMessage)
                                .setPositiveButton(getString(R.string.btn_retry), (d, w) -> call.clone().enqueue(this))
                                .create();
                        dialog.show();
                        spinProducts.setEnabled(false);
                        btnSubmit.setEnabled(false);
                    }
                } else {
                    String message = getString(R.string.error_no_response);
                    Log.d(TAG, message);
                    if (dialog != null) dialog.dismiss();
                    dialog = ViewUtils.dialogError(RegisterActivity.this, "Failed", message)
                            .setPositiveButton(getString(R.string.btn_retry), (d, w) -> call.clone().enqueue(this))
                            .create();
                    dialog.show();
                    spinProducts.setEnabled(false);
                    btnSubmit.setEnabled(false);
                }
            }

            @Override
            public void onFailure(Call<ResponseProduct> call, Throwable t) {
                String message = getString(R.string.error_failed_product);
                Log.d(TAG, message);
                onProgressLoading(false);
                if (dialog != null) dialog.dismiss();
                if (!PermissionUtils.isNetworkAvailable(RegisterActivity.this))
                    dialog = ViewUtils.dialogError(RegisterActivity.this, "Failed",
                            getString(R.string.error_no_internet))
                            .setPositiveButton(getString(R.string.btn_retry),
                                    (d, w) -> call.clone().enqueue(this))
                            .create();
                else
                    dialog = ViewUtils.dialogError(RegisterActivity.this, "Failed", message)
                            .setPositiveButton(getString(R.string.btn_retry), (d, w) -> call.clone().enqueue(this))
                            .create();
                dialog.show();
                spinProducts.setEnabled(false);
                btnSubmit.setEnabled(false);
            }
        });
    }

    private void addChosenProduct(ResponseProduct.Product newProduct){
        if(productsChosen !=null && productsChosen.add(newProduct)) {
            rvProducts.setAdapter(new ProductAdapter(this, productsChosen, this));
            countFee();
        }
    }

    @Override
    public void onProductDelete(ResponseProduct.Product product) {
        if (productsChosen !=null && productsChosen.remove(product)){
            rvProducts.setAdapter(new ProductAdapter(this, productsChosen,this));
            countFee();
        }
    }

    private void countFee(){
        monthlyFee = 0; installFee = 0;
        for (ResponseProduct.Product product : productsChosen) {
            monthlyFee += product.proPrice;
            installFee += product.proInstall;
        }
        textMonthly.setText(getString(R.string.format_fee, monthlyFee));
        textInstall.setText(getString(R.string.format_fee,installFee));
        textProrate.setText(getString(R.string.format_fee, ViewUtils.countProrate(this, dateChosen, monthlyFee)));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_SLOT){
            if (resultCode == RESULT_OK) {
                if (data!=null && data.getExtras()!=null){
                    slotId = data.getStringExtra(Constants.ARG_SLOT_ID);
                    slotName = data.getStringExtra(Constants.ARG_SLOT_NAME);
                    dateChosen = data.getStringExtra(Constants.ARG_DATE);
                    editDate.setText(getString(R.string.slot_text, slotName, slotId));
                } else
                    Toast.makeText(this, "Slot must be chosen", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Slot must be chosen", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQ_LOCATION) {
            if (resultCode == RESULT_OK) {
                if (data != null && data.getExtras()!=null) {
                    isLocation = true;
                    lat = data.getDoubleExtra(Constants.ARG_LATITUDE,0.0);
                    lon = data.getDoubleExtra(Constants.ARG_LONGITUDE,0.0);
                } else
                    Toast.makeText(this, "Location data not found", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(this, "Location must be chosen", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * validate the coordinates
     * @param lat latitude
     * @param lon longitude
     * @return true if valid
     */
    private boolean isLocationValid(double lat, double lon) {
        return (lat > -90.0 && lat < 90.0) && (lon > -180.0 && lon < 180.0);
    }

    private void attemptRegister(){
        editDate.setError(null);
        editName.setError(null);
        String message = null;
        Map<String,Integer> products = new HashMap<>();
        Map<String,String> bundles = new HashMap<>();
        boolean cancel = false;
        View focusView = null;
        String custName = editName.getText().toString();

        if (TextUtils.isEmpty(userId) && TextUtils.isEmpty(userKey)){
            cancel = true;
            message = "You are not logged in";
            SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            userId = sPrefs.getString(Constants.ARG_USER_ID,"");
            userKey = sPrefs.getString(Constants.ARG_USER_KEY,"");
        }
        if (TextUtils.isEmpty(custName)){
            cancel = true;
            focusView = editName;
        }
        if (TextUtils.isEmpty(slotId)){
            message = "Please select Booking Schedule";
            cancel = true;
            focusView = editDate;
        }
        if (!isLocationValid(lat,lon) && !isLocation){
            message = "Please mark install location.";
            cancel = true;
        }
        if (!(productsChosen!=null && !productsChosen.isEmpty())){
            message = "Please choose at least one product";
            cancel = true;
            focusView = spinProducts;
            getProductList();
        } else {
            countFee();
            int i = 0;
            for (ResponseProduct.Product product : productsChosen){
                products.put(String.format(Locale.getDefault(),"pros[%d]",i), product.proId);
                bundles.put(String.format(Locale.getDefault(),"bund[%d]",i), product.proBundle);
                i++;
            }
        }

        if (cancel){
            if (focusView!=null) focusView.requestFocus();
            if (message!=null) Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else {
            onProgressLoading(true);
            IxitaskService.getApi().registerInstall(
                    userId,
                    userKey,
                    hpId,
                    slotId,
                    custName,
                    lon,
                    lat,
                    products,
                    bundles)
            .enqueue(new Callback<ResponseUpdate>() {
                @Override
                public void onResponse(Call<ResponseUpdate> call, Response<ResponseUpdate> response) {
                    onProgressLoading(false);
                    ResponseUpdate res = response.body();
                    Log.d(TAG, call.request().toString());
                    if (res != null) {
                        Log.d(TAG, res.getStatusMessage());
                        int status = Integer.parseInt(res.getStatus());
                        if (status == 200) {
                            finishAndClearData();
                        } else {
                            if (dialog != null) dialog.dismiss();
                            dialog = ViewUtils.dialogError(RegisterActivity.this, res.getStatus(),
                                    res.getStatusMessage())
                                    .setPositiveButton(getString(R.string.btn_retry), (d, w) -> call.clone().enqueue(this))
                                    .create();
                            dialog.show();
                        }
                    } else {
                        String message = getString(R.string.error_no_response);
                        Log.d(TAG, message);
                        if (dialog != null) dialog.dismiss();
                        dialog = ViewUtils.dialogError(RegisterActivity.this, "Failed", message)
                                .setPositiveButton(getString(R.string.btn_retry), (d, w) -> call.clone().enqueue(this))
                                .create();
                        dialog.show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseUpdate> call, Throwable t) {
                    onProgressLoading(false);
                    String message = getString(R.string.error_failed_register);
                    Log.d(TAG, call.request().toString());
                    t.printStackTrace();
                    if (dialog != null) dialog.dismiss();
                    if (!PermissionUtils.isNetworkAvailable(RegisterActivity.this))
                        dialog = ViewUtils.dialogError(RegisterActivity.this, "Failed",
                                getString(R.string.error_no_internet))
                                .setPositiveButton(getString(R.string.btn_retry),
                                        (d, w) -> call.clone().enqueue(this))
                                .create();
                    else
                        dialog = ViewUtils.dialogError(RegisterActivity.this, "Failed", String.format(message, t.getMessage()))
                                .setPositiveButton(getString(R.string.btn_retry), (d, w) -> call.clone().enqueue(this))
                                .create();
                    dialog.show();
                }
            });
        }

    }

    private void onProgressLoading(boolean isLoading) {
        swipe.post(()->swipe.setRefreshing(isLoading));
        if (isLoading) {
            ViewUtils.hideKeyboard(this, editName);
            ViewUtils.hideKeyboard(this, editDate);
        }
    }

}
