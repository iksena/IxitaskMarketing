package com.ixitask.ixitask.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
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

import com.ixitask.ixitask.R;
import com.ixitask.ixitask.activities.SettingsActivity;
import com.ixitask.ixitask.models.ResponseHomepass;
import com.ixitask.ixitask.models.ResponseSummaryRes;
import com.ixitask.ixitask.services.IxitaskService;
import com.ixitask.ixitask.utils.Constants;
import com.ixitask.ixitask.utils.PermissionUtils;
import com.ixitask.ixitask.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class HomeSummaryFragment extends Fragment {

    private static final String TAG = HomeSummaryFragment.class.getSimpleName();
    private HomepassFragment.OnHomepassInteractionListener mListener;
    private Context context;
    private AlertDialog dialog;
    private HomeSummaryAdapter adapter;
    private String userId;
    private String userKey;
    private int hpscId;
    private String summaryTitle;

    @BindView(R.id.edit_search)
    EditText editSearch;
    @BindView(R.id.btn_clear)
    ImageButton btnClear;
    @BindView(R.id.text_count)
    TextView textCount;
    @BindView(R.id.rv_homepasses)
    RecyclerView rvHomepasses;
    @BindView(R.id.text_empty)
    TextView textEmpty;
    @BindView(R.id.text_summary)
    TextView textSummary;
    @BindView(R.id.btn_settings)
    ImageView btnSettings;
    @BindView(R.id.btn_drawer)
    ImageView btnDrawer;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;

    public HomeSummaryFragment() {
    }

    public static HomeSummaryFragment newInstance(String userId, String userKey, int hpscId,
                                                  String summaryTitle) {
        HomeSummaryFragment fragment = new HomeSummaryFragment();
        Bundle args = new Bundle();
        args.putString(Constants.ARG_USER_ID, userId);
        args.putString(Constants.ARG_USER_KEY, userKey);
        args.putInt(Constants.ARG_HPSC_ID, hpscId);
        args.putString(Constants.ARG_HPSC_NAME, summaryTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            userId = getArguments().getString(Constants.ARG_USER_ID);
            userKey = getArguments().getString(Constants.ARG_USER_KEY);
            hpscId = getArguments().getInt(Constants.ARG_HPSC_ID);
            summaryTitle = getArguments().getString(Constants.ARG_HPSC_NAME);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mListener==null){
            mListener = (HomepassFragment.OnHomepassInteractionListener) requireContext();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewUtils.hideKeyboard(context, getView());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_homesummary, container, false);
        ButterKnife.bind(this, view);
        setView();
        swipe.setOnRefreshListener(this::setView);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof HomepassFragment.OnHomepassInteractionListener) {
            mListener = (HomepassFragment.OnHomepassInteractionListener) context;
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

    public void setView(){
        textSummary.setText(summaryTitle);
        rvHomepasses.setLayoutManager(new LinearLayoutManager(context));
        editSearch.getText().clear();
        editSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        loadHomepasses();
        editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == EditorInfo.IME_NULL){
                searchHomepass(((EditText) v).getText().toString());
            }
            return false;
        });
        btnClear.setOnClickListener(v->{
            if (!TextUtils.isEmpty(editSearch.getText().toString()))
                editSearch.getText().clear();
            searchHomepass("");
        });
        btnDrawer.setOnClickListener(v->mListener.openSideNavigation(true));
        btnSettings.setOnClickListener(v->startActivity(new Intent(context, SettingsActivity.class)));
    }

    private void loadHomepasses() {
        onProgressLoading(true);
        IxitaskService.getApi().getSummaryResponses(userId, userKey, hpscId).enqueue(new Callback<ResponseSummaryRes>() {
            @Override
            public void onResponse(Call<ResponseSummaryRes> call, Response<ResponseSummaryRes> response) {
                onProgressLoading(false);
                displayEmptyView(true);
                ResponseSummaryRes res = response.body();
                if (res != null){
                    int status = Integer.parseInt(res.status);
                    Log.d(TAG, res.statusMessage);
                    if (status==200){
                        displayEmptyView(false);
                        List<ResponseSummaryRes.SummaryRes> homepasses = new ArrayList<>(res.data.summaryResponses);
                        adapter = new HomeSummaryAdapter(homepasses, mListener, context);
                        rvHomepasses.setAdapter(adapter);
                        textCount.setText(context.getString(R.string.homepass_count, adapter.getItemCount()));
                    } else {
                        if (context!=null) {
                            if (dialog!=null) dialog.dismiss();
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
                    if (context!=null) {
                        if (dialog!=null) dialog.dismiss();
                        dialog = ViewUtils.dialogError(context, "Failed", message)
                                .setPositiveButton(context.getString(R.string.btn_retry), (d, w) -> call.clone().enqueue(this))
                                .create();
                        dialog.show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseSummaryRes> call, Throwable t) {
                onProgressLoading(false);
                displayEmptyView(true);
                String message = context.getString(R.string.error_failed_homesummary);
                Log.d(TAG, call.request().toString());
                t.printStackTrace();
                if (context!=null) {
                    if (dialog!=null) dialog.dismiss();
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
    }

    private void displayEmptyView(boolean isEmpty){
        if (isEmpty){
            rvHomepasses.setVisibility(View.INVISIBLE);
            textEmpty.setVisibility(View.VISIBLE);
        } else {
            rvHomepasses.setVisibility(View.VISIBLE);
            textEmpty.setVisibility(View.INVISIBLE);
        }
    }

    private void searchHomepass(String query){
        onProgressLoading(true);
        Log.d(TAG, "Filtering list");
        adapter.getFilter().filter(query, count -> { //count = homepasses found
            onProgressLoading(false);
            Log.d(TAG, "Filtering completed");
            textCount.setText(context.getString(R.string.homepass_count, adapter.getItemCount()));
        });
    }

    private void onProgressLoading(boolean isLoading) {
        swipe.post(()->swipe.setRefreshing(isLoading));
        if (isLoading) ViewUtils.hideKeyboard(context, editSearch);
    }
}
