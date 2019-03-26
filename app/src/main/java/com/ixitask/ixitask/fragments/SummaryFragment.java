package com.ixitask.ixitask.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ixitask.ixitask.R;
import com.ixitask.ixitask.activities.SettingsActivity;
import com.ixitask.ixitask.models.ResponseSummary;
import com.ixitask.ixitask.services.IxitaskService;
import com.ixitask.ixitask.utils.Constants;
import com.ixitask.ixitask.utils.PermissionUtils;
import com.ixitask.ixitask.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class SummaryFragment extends Fragment {

    private static final String TAG = SummaryFragment.class.getSimpleName();
    private OnSummaryInteractionListener mListener;
    private Context context;
    private AlertDialog dialog;
    private SummaryAdapter adapter;
    private String userId;
    private String userKey;
    private String username;

    @BindView(R.id.text_name)
    TextView textName;
    @BindView(R.id.rv_summaries)
    RecyclerView rvSummaries;
    @BindView(R.id.text_empty)
    TextView textEmpty;
    @BindView(R.id.btn_settings)
    ImageView btnSettings;
    @BindView(R.id.btn_drawer)
    ImageView btnDrawer;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;

    public SummaryFragment() {
    }

    public static SummaryFragment newInstance(String userId, String userKey, String username) {
        SummaryFragment fragment = new SummaryFragment();
        Bundle args = new Bundle();
        args.putString(Constants.ARG_USER_ID, userId);
        args.putString(Constants.ARG_USER_KEY, userKey);
        args.putString(Constants.ARG_USERNAME, username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            userId = getArguments().getString(Constants.ARG_USER_ID);
            userKey = getArguments().getString(Constants.ARG_USER_KEY);
            username = getArguments().getString(Constants.ARG_USERNAME);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mListener==null){
            mListener = (OnSummaryInteractionListener) requireContext();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewUtils.hideKeyboard(context, getView());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary, container, false);
        ButterKnife.bind(this, view);
        setView();
        swipe.setOnRefreshListener(this::setView);
        return view;
    }

    public void setView(){
        onProgressLoading(false);
        rvSummaries.setLayoutManager(new LinearLayoutManager(context));
        loadSummaries();
        textName.setText(username);
        btnDrawer.setOnClickListener(v->mListener.openSideNavigation(true));
        btnSettings.setOnClickListener(v->startActivity(new Intent(context, SettingsActivity.class)));
    }

    private void loadSummaries(){
        //TO DO A3 add gps disabled failure
        if (PermissionUtils.isLocationTrackingEnabled(context)) {
            onProgressLoading(true);
            textEmpty.setText(R.string.summaries_empty);
            IxitaskService.getApi().getSummaries(userId, userKey).enqueue(new Callback<ResponseSummary>() {
                @Override
                public void onResponse(Call<ResponseSummary> call, Response<ResponseSummary> response) {
                    onProgressLoading(false);
                    displayEmptyView(true);
                    ResponseSummary res = response.body();
                    if (res != null) {
                        int status = Integer.parseInt(res.status);
                        Log.d(TAG, res.statusMessage);
                        if (status == 200) {
                            displayEmptyView(false);
                            List<ResponseSummary.Summary> summaries = new ArrayList<>(res.data.summaries);
                            adapter = new SummaryAdapter(summaries, mListener);
                            rvSummaries.setAdapter(adapter);
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
                public void onFailure(Call<ResponseSummary> call, Throwable t) {
                    onProgressLoading(false);
                    displayEmptyView(true);
                    String message = context.getString(R.string.error_failed_summary);
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
            rvSummaries.setVisibility(View.INVISIBLE);
            textEmpty.setVisibility(View.VISIBLE);
        } else {
            rvSummaries.setVisibility(View.VISIBLE);
            textEmpty.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof OnSummaryInteractionListener) {
            mListener = (OnSummaryInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void onProgressLoading(boolean isLoading) {
        swipe.post(()->swipe.setRefreshing(isLoading));
//        if (isLoading) ViewUtils.hideKeyboard(this, getCurrentFocus());
    }

    public interface OnSummaryInteractionListener {
        /**
         * a listener when a homepass clicked
         * @param summary the summary object to be passed
         */
        void onSummaryClick(ResponseSummary.Summary summary);
        /**
         * a listener when a progress is loading
         * @param isLoading is it loading?
         */
        void onProgressLoading(boolean isLoading);
        void openSideNavigation(boolean wantOpen);
        boolean onChangeFragment(int fragmentId);
    }
}
