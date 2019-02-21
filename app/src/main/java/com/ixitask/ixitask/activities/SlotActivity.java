package com.ixitask.ixitask.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ixitask.ixitask.R;
import com.ixitask.ixitask.fragments.SlotAdapter;
import com.ixitask.ixitask.models.ResponseCode;
import com.ixitask.ixitask.models.ResponseSlot;
import com.ixitask.ixitask.services.IxitaskService;
import com.ixitask.ixitask.utils.Constants;
import com.ixitask.ixitask.utils.PermissionUtils;
import com.ixitask.ixitask.utils.ViewUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.ixitask.ixitask.utils.Constants.ARG_DATE;
import static com.ixitask.ixitask.utils.Constants.ARG_SLOT_ID;
import static com.ixitask.ixitask.utils.Constants.ARG_SLOT_NAME;

public class SlotActivity extends AppCompatActivity implements SlotAdapter.OnSlotInteractionListener {

    private String TAG = SlotActivity.class.getSimpleName();
    private AlertDialog dialog;
    private String userId;
    private String userKey;
    private String dateChosen;

    @BindView(R.id.text_date)
    TextView textDate;
    @BindView(R.id.rv_slot)
    RecyclerView rvSlots;
    @BindView(R.id.text_empty)
    TextView textEmpty;
    @BindView(R.id.btn_back)
    Button btnBack;
    @BindView(R.id.btn_menu_back)
    ImageView btnMenuBack;
    @BindView(R.id.btn_settings)
    ImageView btnSettings;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slot);
        ButterKnife.bind(this);

        checkUserValidity();
        swipe.setOnRefreshListener(this::getSlots);
        setDateChosen(null);
        textDate.setOnClickListener(v->showDatePicker());
        btnBack.setOnClickListener(v->cancelChoosingSlot());
        btnMenuBack.setOnClickListener(v->cancelChoosingSlot());
        btnSettings.setOnClickListener(v->startActivity(
                new Intent(this, SettingsActivity.class)));
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
    public void onBackPressed() {
        cancelChoosingSlot();
    }

    private void cancelChoosingSlot(){
        Intent registerActivity = new Intent(this, RegisterActivity.class);
        setResult(RESULT_CANCELED, registerActivity);
        finish();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(Constants.ARG_USER_ID, userId);
        outState.putString(Constants.ARG_USER_KEY, userKey);
        outState.putString(Constants.ARG_DATE, dateChosen);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState!=null) {
            userId = savedInstanceState.getString(Constants.ARG_USER_ID);
            userKey = savedInstanceState.getString(Constants.ARG_USER_KEY);
            dateChosen = savedInstanceState.getString(Constants.ARG_DATE);
        }
    }

    private void setDateChosen(Calendar calendar){
        if (calendar == null) calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.format_date_server), Locale.getDefault());
        SimpleDateFormat sdfView = new SimpleDateFormat(getString(R.string.format_date), Locale.getDefault());
        dateChosen = sdf.format(calendar.getTime());
        textDate.setText(sdfView.format(calendar.getTime()));
        getSlots();
    }

    private void getSlots(){
        onProgressLoading(true);
        IxitaskService.getApi().getSlots(userId, userKey, dateChosen).enqueue(new Callback<ResponseSlot>() {
            @Override
            public void onResponse(Call<ResponseSlot> call, Response<ResponseSlot> response) {
                ResponseSlot res = response.body();
                onProgressLoading(false);
                displayEmptyView(true);
                if (res != null) {
                    int status = Integer.parseInt(res.status);
                    Log.d(TAG, res.statusMessage);
                    if (status == 200) {
                        List<ResponseSlot.Slot> slots = res.data.slots;
                        if (slots!=null && !slots.isEmpty()){
                            displayEmptyView(false);
                            rvSlots.setAdapter(new SlotAdapter(SlotActivity.this, slots,
                                    SlotActivity.this));
                        }
                    } else {
                        if (dialog!=null) dialog.dismiss();
                        dialog = ViewUtils.dialogError(SlotActivity.this, res.status, res.statusMessage)
                                .setPositiveButton(getString(R.string.btn_retry), (d, w) -> call.clone().enqueue(this))
                                .create();
                        dialog.show();
                    }
                } else {
                    String message = getString(R.string.error_no_response);
                    Log.d(TAG, message);
                    Log.d(TAG, call.request().toString());
                    if (dialog != null) dialog.dismiss();
                        dialog = ViewUtils.dialogError(SlotActivity.this, "Failed", message)
                                .setPositiveButton(getString(R.string.btn_retry), (d, w) -> call.clone().enqueue(this))
                                .create();
                    dialog.show();
                }
            }

            @Override
            public void onFailure(Call<ResponseSlot> call, Throwable t) {
                String message = getString(R.string.error_failed_slot);
                Log.d(TAG, call.request().toString());
                t.printStackTrace();
                onProgressLoading(false);
                displayEmptyView(true);
                if (dialog != null) dialog.dismiss();
                if (!PermissionUtils.isNetworkAvailable(SlotActivity.this))
                    dialog = ViewUtils.dialogError(SlotActivity.this, "Failed",
                            getString(R.string.error_no_internet))
                            .setPositiveButton(getString(R.string.btn_retry),
                                    (d, w) -> call.clone().enqueue(this))
                            .create();
                else
                    dialog = ViewUtils.dialogError(SlotActivity.this, "Failed", String.format(message, t.getMessage()))
                            .setPositiveButton(getString(R.string.btn_retry), (d, w) -> call.clone().enqueue(this))
                            .create();
                dialog.show();
            }
        });
    }

    private void showDatePicker(){
        Calendar date = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar datePicked = Calendar.getInstance();
                    datePicked.set(Calendar.YEAR, year);
                    datePicked.set(Calendar.MONTH, month);
                    datePicked.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    setDateChosen(datePicked);
                },
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH));
        datePicker.getDatePicker().setMinDate(date.getTimeInMillis());
        if (!datePicker.isShowing()) datePicker.show();
    }

    @Override
    public void onSlotClick(ResponseSlot.Slot slot) {
        Intent registerActivity = new Intent(this, RegisterActivity.class);
        registerActivity.putExtra(ARG_SLOT_ID, slot.slotid);
        registerActivity.putExtra(ARG_SLOT_NAME, slot.slotname);
        registerActivity.putExtra(ARG_DATE, dateChosen);
        setResult(RESULT_OK, registerActivity);
        finish();
    }

    private void displayEmptyView(boolean isEmpty){
        if (isEmpty){
            rvSlots.setVisibility(View.INVISIBLE);
            textEmpty.setVisibility(View.VISIBLE);
        } else {
            rvSlots.setVisibility(View.VISIBLE);
            textEmpty.setVisibility(View.INVISIBLE);
        }
    }

    private void onProgressLoading(boolean isLoading) {
        swipe.post(()->swipe.setRefreshing(isLoading));
//        if (isLoading) ViewUtils.hideKeyboard(this, getCurrentFocus());
    }
}
