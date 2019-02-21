package com.ixitask.ixitask.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ixitask.ixitask.R;
import com.ixitask.ixitask.activities.RegisterActivity;
import com.ixitask.ixitask.activities.SettingsActivity;
import com.ixitask.ixitask.models.ResponseCode;
import com.ixitask.ixitask.models.ResponseUpdate;
import com.ixitask.ixitask.services.IxitaskService;
import com.ixitask.ixitask.utils.Constants;
import com.ixitask.ixitask.utils.PermissionUtils;
import com.ixitask.ixitask.utils.ViewUtils;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.ixitask.ixitask.services.LocationRequestHelper.LOCATION_SINGLE_UPDATE_INTERVAL;
import static com.ixitask.ixitask.services.LocationRequestHelper.REQ_GPS_SINGLE;
import static com.ixitask.ixitask.services.LocationRequestHelper.getLocationRequest;
import static com.ixitask.ixitask.services.LocationRequestHelper.promptEnableGps;

public class ActivityFragment extends Fragment {

    private static final String TAG = ActivityFragment.class.getSimpleName();
    private Context context;
    private OnActivityInteractionListener mListener;
    private FusedLocationProviderClient locationProvider;
    private Calendar datePicked;
    private SimpleDateFormat sdf;
    private AlertDialog dialog;
    private String userId;
    private String userKey;
    private String hpId;
    private String streetName;
    private String ownerName;
    private String phone;
    private boolean isOpen;
    private double lat;
    private double lon;
    private Button btnChecked;

    @BindView(R.id.text_street)
    TextView textStreet;
    @BindView(R.id.text_desc)
    TextView textDesc;
    @BindView(R.id.edit_date)
    EditText editDate;
    @BindView(R.id.group_contact)
    Button btnContact;
    @BindView(R.id.group_appoinment)
    Button btnAppoinment;
    @BindView(R.id.group_transaction)
    Button btnPresentation;
    @BindView(R.id.edit_name)
    EditText editName;
    @BindView(R.id.edit_phone)
    EditText editPhone;
    @BindView(R.id.spinner_res_codes)
    SearchableSpinner spinResCodes;
    @BindView(R.id.img_rc)
    ImageView imgResCodes;
    @BindView(R.id.edit_note)
    EditText editNote;
    @BindView(R.id.img_note)
    ImageView imgNote;
    @BindView(R.id.switch_open)
    Switch switchOpen;
    @BindView(R.id.btn_call)
    ImageButton btnCall;
    @BindView(R.id.btn_register)
    Button btnRegister;
    @BindView(R.id.btn_submit)
    Button btnSubmit;
    @BindView(R.id.navigation)
    BottomNavigationView botNav;
    @BindColor(R.color.colorPrimary)
    int colorPrimary;
    @BindColor(R.color.white)
    int colorWhite;
    @BindColor(R.color.transparent)
    int colorTransparent;
    @BindColor(R.color.black)
    int colorBlack;
    @BindString(R.string.error_field_required)
    String strError;
    @BindView(R.id.btn_settings)
    ImageView btnSettings;
    @BindView(R.id.btn_drawer)
    ImageView btnDrawer;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;

    public ActivityFragment() {
    }

    /**
     * Fragment factory method
     * @param userId user id
     * @param userKey user key
     * @param hpId homepass id
     * @param streetName for displaying
     * @param ownerName for displaying
     * @param phone for displaying
     * @param isOpen for displaying
     * @return this fragment object
     */
    public static ActivityFragment newInstance(String userId, String userKey, String hpId, String streetName,
                                               String ownerName, String phone, boolean isOpen) {
        ActivityFragment fragment = new ActivityFragment();
        Bundle args = new Bundle();
        args.putString(Constants.ARG_USER_ID, userId);
        args.putString(Constants.ARG_USER_KEY, userKey);
        args.putString(Constants.ARG_HP_ID, hpId);
        args.putString(Constants.ARG_HP_STREET, streetName);
        args.putString(Constants.ARG_HP_OWNER, ownerName);
        args.putString(Constants.ARG_HP_PHONE, phone);
        args.putBoolean(Constants.ARG_HP_OPEN, isOpen);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(Constants.ARG_USER_ID);
            userKey = getArguments().getString(Constants.ARG_USER_KEY);
            hpId = getArguments().getString(Constants.ARG_HP_ID);
            streetName = getArguments().getString(Constants.ARG_HP_STREET);
            ownerName = getArguments().getString(Constants.ARG_HP_OWNER);
            phone = getArguments().getString(Constants.ARG_HP_PHONE);
            isOpen = getArguments().getBoolean(Constants.ARG_HP_OPEN);
        }
        datePicked = Calendar.getInstance();
        sdf = new SimpleDateFormat(context.getString(R.string.format_date), Locale.getDefault());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity, container, false);
        ButterKnife.bind(this, view);
        setView();
        swipe.setOnRefreshListener(() -> {

        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mListener==null){
            mListener = (OnActivityInteractionListener) requireContext();
        }
        if (!isLocationValid(lat, lon)) getCoordinates();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewUtils.hideKeyboard(context, getView());
    }

    /**
     * diplay UI with data
     */
    public void setView(){
        botNav.getMenu().getItem(1).setChecked(true);
        botNav.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId()!=R.id.navigation_activity)
                mListener.onChangeFragment(item.getItemId());
            return true;
        });
        btnSubmit.setEnabled(false);
        getResCodes();
        getCoordinates();
        editDate.setText(sdf.format(datePicked.getTime()));
        editDate.setOnClickListener(v->showDatePicker());
        editDate.setOnFocusChangeListener((v,hasFocus)->{if (hasFocus) showDatePicker();});
        textStreet.setText(streetName);
        textDesc.setText(context.getString(R.string.activity_bar_desc, ownerName, phone));
        editName.setText(ownerName);
        editPhone.setText(phone);
        switchOpen.setChecked(isOpen);
        setGroupChecked(btnContact, btnContact); //initial value
        btnContact.setOnClickListener(v -> setGroupChecked(btnChecked, (Button) v));
        btnAppoinment.setOnClickListener(v -> setGroupChecked(btnChecked, (Button) v));
        btnPresentation.setOnClickListener(v -> setGroupChecked(btnChecked, (Button) v));
        editNote.setOnFocusChangeListener((v,hF)-> imgNote.setImageResource(hF ?
                R.drawable.activity_note_black : R.drawable.activity_note));
        btnCall.setOnClickListener(v -> {
            Intent dial = new Intent();
            dial.setAction(Intent.ACTION_DIAL);
            dial.setData(Uri.parse("tel:" + phone));
            startActivity(dial);
        });
        btnSubmit.setOnClickListener(v->attemptSubmit(false));
        btnRegister.setOnClickListener(v->attemptSubmit(true));
        btnDrawer.setOnClickListener(v->mListener.openSideNavigation(true));
        btnSettings.setOnClickListener(v->startActivity(new Intent(context, SettingsActivity.class)));
    }

    /**
     * A method to change the state of group button
     * @param btnChecked button that is already checked/selected and will be unchecked in this method
     * @param btnToCheck button that hasn't checked/selected and will be checked in this method
     */
    private void setGroupChecked(Button btnChecked, Button btnToCheck) {
        btnChecked.setBackgroundColor(colorTransparent);
        btnChecked.setTextColor(colorBlack);
        btnChecked.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        btnToCheck.setBackgroundColor(colorPrimary);
        btnToCheck.setTextColor(colorWhite);
        btnToCheck.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        this.btnChecked = btnToCheck;
    }

    /**
     * method to get checked value of group button
     * @param btnChecked button that is currently checked
     * @return a value to send to server
     */
    private int getGroupCheckedValue(Button btnChecked) {
        if (btnChecked == null) return -1;
        if (btnChecked.equals(btnContact)) {
            return 1;
        } else if (btnChecked.equals(btnAppoinment)) {
            return 2;
        } else if (btnChecked.equals(btnPresentation)) {
            return 3;
        } else {
            return -1;
        }
    }

    /**
     * pop-up calendar to select a date at least today's date
     */
    private void showDatePicker(){
        Calendar date = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(context,
                (view, year, month, dayOfMonth) -> {
                    datePicked.set(Calendar.YEAR, year);
                    datePicked.set(Calendar.MONTH, month);
                    datePicked.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    editDate.setText(sdf.format(datePicked.getTime()));
                },
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH));
        datePicker.getDatePicker().setMinDate(date.getTimeInMillis());
        if (!datePicker.isShowing()) datePicker.show();
    }

    /**
     * calling from ixitask server to get response codes
     */
    private void getResCodes(){
        onProgressLoading(true);
        IxitaskService.getApi().getResCodes(userId, userKey).enqueue(new Callback<ResponseCode>() {
            @Override
            public void onResponse(Call<ResponseCode> call, Response<ResponseCode> response) {
                onProgressLoading(false);
                ResponseCode res = response.body();
                if (res != null) {
                    int status = Integer.parseInt(res.getStatus());
                    Log.d(TAG, res.getStatusMessage());
                    if (status == 200) {
                        List<ResponseCode.ResCode> resCodes = res.getData().getResCodes();
                        ArrayAdapter<ResponseCode.ResCode> adapter = new ArrayAdapter<>(context,
                                android.R.layout.simple_spinner_dropdown_item, resCodes);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinResCodes.setAdapter(adapter);
                        btnSubmit.setEnabled(true);
                    } else {
                        if (context!=null) {
                            if (dialog!=null) dialog.dismiss();
                            dialog = ViewUtils.dialogError(context, res.getStatus(), res.getStatusMessage())
                                    .setPositiveButton(context.getString(R.string.btn_retry), (d, w) -> call.clone().enqueue(this))
                                    .create();
                            dialog.show();
                        }
                        btnSubmit.setEnabled(false);
                    }
                } else {
                    String message = context.getString(R.string.error_no_response);
                    Log.d(TAG, message);
                    Log.d(TAG, call.request().toString());
                    if (context!=null) {
                        if (dialog != null) dialog.dismiss();
                        dialog = ViewUtils.dialogError(context, "Failed", message)
                                .setPositiveButton(context.getString(R.string.btn_retry), (d, w) -> call.clone().enqueue(this))
                                .create();
                        dialog.show();
                    }
                    btnSubmit.setEnabled(false);
                }
            }

            @Override
            public void onFailure(Call<ResponseCode> call, Throwable t) {
                onProgressLoading(false);
                String message = context.getString(R.string.error_failed_rescodes);
                Log.d(TAG, call.request().toString());
                t.printStackTrace();
                if (context!=null) {
                    if (dialog != null) dialog.dismiss();
                    if (!PermissionUtils.isNetworkAvailable(context))
                        dialog = ViewUtils.dialogError(context, "Failed",
                                context.getString(R.string.error_no_internet))
                                .setPositiveButton(context.getString(R.string.btn_retry),
                                        (d, w) -> call.clone().enqueue(this))
                                .create();
                    else
                        dialog = ViewUtils.dialogError(context, "Failed",
                                String.format(message, t.getMessage()))
                                .setPositiveButton(context.getString(R.string.btn_retry),
                                        (d, w) -> call.clone().enqueue(this))
                                .create();
                    dialog.show();
                }
                btnSubmit.setEnabled(false);
            }
        });
    }

    /**
     * a callback object to get a single location for activity form submission
     */
    private LocationCallback locationCallback = new LocationCallback(){
        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
            onProgressLoading(false);
            if (!locationAvailability.isLocationAvailable()){
                if (context!=null) {
                    if (dialog != null) dialog.dismiss();
                    dialog = ViewUtils.dialogError(context, "Failed",
                            context.getString(R.string.error_location_disabled))
                            .setPositiveButton(context.getString(R.string.btn_retry), (d, w) -> getCoordinates())
                            .create();
                    dialog.show();
                }
                btnSubmit.setEnabled(false);
            }
        }
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            onProgressLoading(false);
            if (locationResult!=null){
                Location l = locationResult.getLastLocation();
                lat = l.getLatitude();
                lon = l.getLongitude();
                btnSubmit.setEnabled(true);
                locationProvider.removeLocationUpdates(this); //stopping update
            } else {
                if (context!=null) {
                    if (dialog != null) dialog.dismiss();
                    dialog = ViewUtils.dialogError(context, "Failed",
                            context.getString(R.string.error_location_unknown))
                            .setPositiveButton(context.getString(R.string.btn_retry), (d, w) -> getCoordinates())
                            .create();
                    dialog.show();
                }
                btnSubmit.setEnabled(false);
            }
        }
    };

    /**
     * getting location and then pass the data to callback
     */
    @SuppressLint("MissingPermission")
    private void getCoordinates() {
        onProgressLoading(true);
        String[] permissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        LocationRequest locationRequest = getLocationRequest(LOCATION_SINGLE_UPDATE_INTERVAL);
        if (PermissionUtils.isPermissionGranted(context, requireActivity(), permissions)) {
            //this prompt window interaction is handled on HomeActivity
            if (getActivity()!=null) promptEnableGps(getActivity(), locationRequest, REQ_GPS_SINGLE);
            locationProvider.requestLocationUpdates(locationRequest, locationCallback,null);
        } else {
            if (context!=null)
            Toast.makeText(context, R.string.error_permission, Toast.LENGTH_SHORT).show();
            btnSubmit.setEnabled(false);
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

    /**
     * called on submit button pressed
     * checking the validity of the form submission
     */
    private void attemptSubmit(boolean registerAfter){
        onProgressLoading(false);
        editDate.setError(null);
        editName.setError(null);
        editNote.setError(null);
        String resCode = null;
        String message = null;

        String date = editDate.getText().toString();
        int contactType = getGroupCheckedValue(btnChecked);
        String name = editName.getText().toString();
        phone = editPhone.getText().toString();
        String note = editNote.getText().toString();
        boolean isOpen = switchOpen.isChecked();
        Log.d(TAG, String.valueOf(isOpen));
        ResponseCode.ResCode selectedCode = (ResponseCode.ResCode) spinResCodes.getSelectedItem();
        if (selectedCode!=null) resCode = selectedCode.getRid();

        boolean cancel = false;
        View focusView = null;

        if (resCode==null) {
            message = context.getString(R.string.error_unselected_rescodes);
            focusView = spinResCodes;
            cancel = true;
            getResCodes();
        }
        if (!isLocationValid(lat, lon)){
            message = context.getString(R.string.error_location_unknown);
            cancel = true;
            getCoordinates();
        }
        if (TextUtils.isEmpty(date)){
            editDate.setError(strError);
            focusView = editDate;
            cancel = true;
        }
        if (TextUtils.isEmpty(name)){
            editName.setError(strError);
            focusView = editName;
            cancel = true;
        }
        if (TextUtils.isEmpty(note)){
            editNote.setError(strError);
            focusView = editNote;
            cancel = true;
        }
        if (contactType < 1){
            contactType = getGroupCheckedValue(btnContact);
        }

        if (cancel){
            if (focusView!=null) focusView.requestFocus();
            if (message!=null) Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        } else {
            locationProvider.removeLocationUpdates(locationCallback);
            onProgressLoading(true);
            IxitaskService.getApi().submitUpdate(
                userId,
                userKey,
                hpId,
                date,
                contactType,
                name,
                phone,
                resCode,
                note,
                isOpen,
                lon, lat)
            .enqueue(new Callback<ResponseUpdate>() {
                @Override
                public void onResponse(Call<ResponseUpdate> call, Response<ResponseUpdate> response) {
                    onProgressLoading(false);
                    ResponseUpdate res = response.body();
                    if (res != null) {
                        Log.d(TAG, res.getStatusMessage());
                        int status = Integer.parseInt(res.getStatus());
                        if (status==200){
                            mListener.onActivitySubmit(hpId);
                            if (registerAfter){
                                Intent registerIntent = new Intent(context, RegisterActivity.class);
                                registerIntent.putExtra(Constants.ARG_USER_ID, userId);
                                registerIntent.putExtra(Constants.ARG_USER_KEY, userKey);
                                registerIntent.putExtra(Constants.ARG_HP_ID, hpId);
                                registerIntent.putExtra(Constants.ARG_HP_STREET, streetName);
                                registerIntent.putExtra(Constants.ARG_HP_OWNER, ownerName);
                                registerIntent.putExtra(Constants.ARG_HP_PHONE, phone);
                                startActivity(registerIntent);
                            }
                        } else {
                            if (context!=null) {
                                if (dialog != null) dialog.dismiss();
                                dialog = ViewUtils.dialogError(context, res.getStatus(),
                                        res.getStatusMessage())
                                        .setPositiveButton(context.getString(R.string.btn_retry), (d, w) -> call.clone().enqueue(this))
                                        .create();
                                dialog.show();
                            }
                        }
                    } else {
                        String message = context.getString(R.string.error_no_response);
                        Log.d(TAG, message);
                        if (context!=null) {
                            if (dialog != null) dialog.dismiss();
                            dialog = ViewUtils.dialogError(context, "Failed", message)
                                    .setPositiveButton(context.getString(R.string.btn_retry), (d, w) -> call.clone().enqueue(this))
                                    .create();
                            dialog.show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseUpdate> call, Throwable t) {
                    onProgressLoading(false);
                    String message = context.getString(R.string.error_failed_update_homepass);
                    Log.d(TAG, call.request().toString());
                    t.printStackTrace();
                    if (context!=null) {
                        if (dialog != null) dialog.dismiss();
                        if (!PermissionUtils.isNetworkAvailable(context))
                            dialog = ViewUtils.dialogError(context, "Failed",
                                    context.getString(R.string.error_no_internet))
                                    .setPositiveButton(context.getString(R.string.btn_retry),
                                            (d, w) -> call.clone().enqueue(this))
                                    .create();
                        else
                            dialog = ViewUtils.dialogError(context, "Failed", String.format(message, t.getMessage()))
                                    .setPositiveButton(context.getString(R.string.btn_retry), (d, w) -> call.clone().enqueue(this))
                                    .create();
                        dialog.show();
                    }
                }
            });
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        locationProvider = LocationServices.getFusedLocationProviderClient(context);
        if (context instanceof OnActivityInteractionListener) {
            mListener = (OnActivityInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnActivityInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        locationProvider.removeLocationUpdates(locationCallback);
    }

    private void onProgressLoading(boolean isLoading) {
        swipe.post(()->swipe.setRefreshing(isLoading));
        if (isLoading) {
            ViewUtils.hideKeyboard(context, editName);
            ViewUtils.hideKeyboard(context, editDate);
            ViewUtils.hideKeyboard(context, editNote);
        }
    }

    public interface OnActivityInteractionListener {
        void onActivitySubmit(String hpId);
        void onProgressLoading(boolean isLoading);
        void openSideNavigation(boolean wantOpen);
        boolean onChangeFragment(int fragmentId);
    }
}
