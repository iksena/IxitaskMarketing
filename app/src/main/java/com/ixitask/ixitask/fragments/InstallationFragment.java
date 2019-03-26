package com.ixitask.ixitask.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ixitask.ixitask.R;
import com.ixitask.ixitask.activities.SettingsActivity;
import com.ixitask.ixitask.models.ResponseInstall;
import com.ixitask.ixitask.services.IxitaskService;
import com.ixitask.ixitask.utils.Constants;
import com.ixitask.ixitask.utils.PermissionUtils;
import com.ixitask.ixitask.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class InstallationFragment extends Fragment {

    private static final String TAG = InstallationFragment.class.getSimpleName();
    private Context context;
    private OnInstallationInteractionListener mListener;
    private AlertDialog dialog;
    private InstallationAdapter adapter;
    private String userId;
    private String userKey;

    @BindView(R.id.edit_search)
    EditText editSearch;
    @BindView(R.id.btn_clear)
    ImageButton btnClear;
    @BindView(R.id.text_count)
    TextView textCount;
    @BindView(R.id.rv_installation)
    RecyclerView rvInstallation;
    @BindView(R.id.text_empty)
    TextView textEmpty;
    @BindView(R.id.btn_drawer)
    ImageView btnDrawer;
    @BindView(R.id.btn_settings)
    ImageView btnSettings;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;

    public InstallationFragment() {
    }

    public static InstallationFragment newInstance(String userId, String userKey) {
        InstallationFragment fragment = new InstallationFragment();
        Bundle args = new Bundle();
        args.putString(Constants.ARG_USER_ID, userId);
        args.putString(Constants.ARG_USER_KEY, userKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(Constants.ARG_USER_ID);
            userKey = getArguments().getString(Constants.ARG_USER_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_installation, container, false);
        ButterKnife.bind(this, view);
        setView();
        swipe.setOnRefreshListener(this::setView);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof OnInstallationInteractionListener) {
            mListener = (OnInstallationInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnActivityInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setView(){
        onProgressLoading(false);
        rvInstallation.setLayoutManager(new LinearLayoutManager(context));
        editSearch.getText().clear();
        editSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        loadInstallations();
        editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == EditorInfo.IME_NULL){
                searchInstall(((EditText) v).getText().toString());
            }
            return false;
        });
        btnClear.setOnClickListener(v->{
            if (!TextUtils.isEmpty(editSearch.getText().toString()))
                editSearch.getText().clear();
            searchInstall("");
        });
        btnDrawer.setOnClickListener(v->mListener.openSideNavigation(true));
        btnSettings.setOnClickListener(v->startActivity(new Intent(context, SettingsActivity.class)));
    }

    private void loadInstallations(){
        //TO DO A2 add gps disabled failure
        if (PermissionUtils.isLocationTrackingEnabled(context)) {
            onProgressLoading(true);
            textEmpty.setText(R.string.install_empty);
            IxitaskService.getApi().getInstallation(userId, userKey).enqueue(new Callback<ResponseInstall>() {
                @Override
                public void onResponse(Call<ResponseInstall> call, Response<ResponseInstall> response) {
                    ResponseInstall res = response.body();
                    displayEmptyView(true);
                    onProgressLoading(false);
                    if (res != null) {
                        int status = Integer.parseInt(res.status);
                        Log.d(TAG, res.statusMessage);
                        if (status == 200) {
                            displayEmptyView(false);
                            List<ResponseInstall.Install> installs = new ArrayList<>(res.data.installs);
                            adapter = new InstallationAdapter(context, installs, mListener);
                            rvInstallation.setAdapter(adapter);
                            textCount.setText(context.getString(R.string.install_count, adapter.getItemCount()));
                        } else {
                            if (context != null) {
                                if (dialog != null) dialog.dismiss();
                                dialog = ViewUtils
                                        .dialogError(context, res.status, res.statusMessage)
                                        .setPositiveButton(R.string.btn_retry,
                                                (d, w) -> call.clone().enqueue(this))
                                        .create();
                                dialog.show();
                            }
                        }
                    } else {
                        String message = context.getString(R.string.error_no_response);
                        Log.d(TAG, message);
                        Log.d(TAG, call.request().toString());
                        if (context != null) {
                            if (dialog != null) dialog.dismiss();
                            dialog = ViewUtils.dialogError(context, "Failed", message)
                                    .setPositiveButton(context.getString(R.string.btn_retry), (d, w) -> call.clone().enqueue(this))
                                    .create();
                            dialog.show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseInstall> call, Throwable t) {
                    displayEmptyView(true);
                    onProgressLoading(false);
                    String message = context.getString(R.string.error_failed_install);
                    Log.d(TAG, call.request().toString());
                    t.printStackTrace();
                    if (context != null) {
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
                                    .setPositiveButton(context.getString(R.string.btn_retry), (d, w) -> call.clone().enqueue(this))
                                    .create();
                        dialog.show();
                    }
                }
            });
        } else {
            Toast.makeText(context, "Aktifkan fitur location tracking dan GPS", Toast.LENGTH_SHORT).show();
            textEmpty.setText(R.string.empty_location_tracking);
            displayEmptyView(true);
            startActivity(new Intent(context, SettingsActivity.class));
        }
    }

    /**
     * Change recyclerview to textview if the list is empty
     * @param isEmpty true if empty
     */
    private void displayEmptyView(boolean isEmpty){
        if (isEmpty){
            rvInstallation.setVisibility(View.INVISIBLE);
            textEmpty.setVisibility(View.VISIBLE);
        } else {
            rvInstallation.setVisibility(View.VISIBLE);
            textEmpty.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * This function is to perform filtering that is implemented the adapter
     * @see com.ixitask.ixitask.fragments.InstallationAdapter
     * @param query the search query to filter the homepass list
     */
    private void searchInstall(String query){
        onProgressLoading(true);
        Log.d(TAG, "Filtering list");
        adapter.getFilter().filter(query, count -> { //count = homepasses found
            onProgressLoading(false);
            Log.d(TAG, "Filtering completed");
            textCount.setText(context.getString(R.string.install_count, adapter.getItemCount()));
        });
    }

    public interface OnInstallationInteractionListener{
        void openSideNavigation(boolean wantOpen);
        void onClick(String serviceId);
    }

    private void onProgressLoading(boolean isLoading) {
        swipe.post(()->swipe.setRefreshing(isLoading));
        if (isLoading) ViewUtils.hideKeyboard(context, editSearch);
    }
}
