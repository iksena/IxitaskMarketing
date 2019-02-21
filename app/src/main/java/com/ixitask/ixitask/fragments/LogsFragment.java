package com.ixitask.ixitask.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ixitask.ixitask.R;
import com.ixitask.ixitask.activities.SettingsActivity;
import com.ixitask.ixitask.models.ResponseLogs;
import com.ixitask.ixitask.services.IxitaskService;
import com.ixitask.ixitask.utils.Constants;
import com.ixitask.ixitask.utils.PermissionUtils;
import com.ixitask.ixitask.utils.ViewUtils;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogsFragment extends Fragment {
    private final String TAG = LogsFragment.class.getSimpleName();

    private Context context;
    private OnLogsInteractionListener mListener;
    private AlertDialog dialog;
    private String userId;
    private String userKey;
    private String hpId;
    private String streetName;
    private String ownerName;
    private String phone;

    @BindView(R.id.text_street)
    TextView textStreet;
    @BindView(R.id.text_desc)
    TextView textDesc;
    @BindView(R.id.rv_logs)
    RecyclerView rvLogs;
    @BindView(R.id.text_empty)
    TextView textEmpty;
    @BindView(R.id.navigation)
    BottomNavigationView botNav;
    @BindView(R.id.btn_settings)
    ImageView btnSettings;
    @BindView(R.id.btn_drawer)
    ImageView btnDrawer;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;

    public LogsFragment() {
    }

    public static LogsFragment newInstance(String userId, String userKey, String hpId,
                                           String streetName, String ownerName, String phone) {
        LogsFragment fragment = new LogsFragment();
        Bundle args = new Bundle();
        args.putString(Constants.ARG_USER_ID, userId);
        args.putString(Constants.ARG_USER_KEY, userKey);
        args.putString(Constants.ARG_HP_ID, hpId);
        args.putString(Constants.ARG_HP_STREET, streetName);
        args.putString(Constants.ARG_HP_OWNER, ownerName);
        args.putString(Constants.ARG_HP_PHONE, phone);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof ActivityFragment.OnActivityInteractionListener) {
            mListener = (OnLogsInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnActivityInteractionListener");
        }
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logs_list, container, false);
        ButterKnife.bind(this, view);
        setView();
        swipe.setOnRefreshListener(this::setView);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mListener==null){
            mListener = (OnLogsInteractionListener) requireContext();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewUtils.hideKeyboard(context, getView());
    }

    /**
     * set the UI of the fragment
     */
    public void setView(){
        botNav.getMenu().getItem(2).setChecked(true);
        botNav.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId()!=R.id.navigation_logs)
                mListener.onChangeFragment(item.getItemId());
            return true;
        });
        textStreet.setText(streetName);
        textDesc.setText(context.getString(R.string.activity_bar_desc, ownerName, phone));
        rvLogs.setLayoutManager(new LinearLayoutManager(context));
        getLogs();
        btnDrawer.setOnClickListener(v->mListener.openSideNavigation(true));
        btnSettings.setOnClickListener(v->startActivity(new Intent(context, SettingsActivity.class)));
    }

    /**
     * load logs list from ixitask
     */
    private void getLogs(){
        onProgressLoading(true);
        IxitaskService.getApi().getLogs(userId, userKey, hpId).enqueue(new Callback<ResponseLogs>() {
            @Override
            public void onResponse(Call<ResponseLogs> call, Response<ResponseLogs> response) {
                onProgressLoading(false);
                displayEmptyView(true);
                ResponseLogs res = response.body();
                if (res!=null){
                    int status = Integer.parseInt(res.getStatus());
                    Log.d(TAG, res.getStatusMessage());
                    if (status==200){
                        displayEmptyView(false);
                        rvLogs.setAdapter(new LogsAdapter(ownerName, res.getData().getLogs()));
                    } else {
                        if (context!=null) {
                            if (dialog != null) dialog.dismiss();
                            dialog = ViewUtils.dialogError(context, res.getStatus(), res.getStatusMessage())
                                    .setPositiveButton(context.getString(R.string.btn_retry), (d, w) -> call.clone().enqueue(this))
                                    .create();
                            dialog.show();
                        }
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
                }
            }

            @Override
            public void onFailure(Call<ResponseLogs> call, Throwable t) {
                onProgressLoading(false);
                displayEmptyView(true);
                String message = context.getString(R.string.error_failed_logs);
                Log.d(TAG, message);
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

    /**
     * Change recyclerview to textview if the list is empty
     * @param isEmpty true if empty
     */
    private void displayEmptyView(boolean isEmpty){
        if (isEmpty){
            rvLogs.setVisibility(View.INVISIBLE);
            textEmpty.setVisibility(View.VISIBLE);
        } else {
            rvLogs.setVisibility(View.VISIBLE);
            textEmpty.setVisibility(View.INVISIBLE);
        }
    }

    public interface OnLogsInteractionListener{
        /**
         * a listener when a progress is loading
         * @param isLoading is it loading?
         */
        void onProgressLoading(boolean isLoading);
        boolean onChangeFragment(int fragmentId);
        void openSideNavigation(boolean wantOpen);
    }

    private void onProgressLoading(boolean isLoading) {
        swipe.post(()->swipe.setRefreshing(isLoading));
//        if (isLoading) ViewUtils.hideKeyboard(this, getCurrentFocus());
    }
}
