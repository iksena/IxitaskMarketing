package com.ixitask.ixitask.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.ixitask.ixitask.activities.LoginActivity;
import com.ixitask.ixitask.activities.SettingsActivity;
import com.ixitask.ixitask.models.ResponseHomepass;
import com.ixitask.ixitask.models.ResponseSummaryRes;
import com.ixitask.ixitask.services.IxitaskService;
import com.ixitask.ixitask.utils.Constants;
import com.ixitask.ixitask.utils.PermissionUtils;
import com.ixitask.ixitask.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomepassFragment extends Fragment {

    private static final String TAG = HomepassFragment.class.getSimpleName();
    private OnHomepassInteractionListener mListener;
    private Context context;
    private AlertDialog dialog;
    private HomepassAdapter adapter;
    private String userId;
    private String userKey;

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
    @BindView(R.id.btn_settings)
    ImageView btnSettings;
    @BindView(R.id.btn_drawer)
    ImageView btnDrawer;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;

    public HomepassFragment() {
    }

    public static HomepassFragment newInstance(String userId, String userKey) {
        HomepassFragment fragment = new HomepassFragment();
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
    public void onResume() {
        super.onResume();
        if (mListener==null){
            mListener = (OnHomepassInteractionListener) requireContext();
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
        View view = inflater.inflate(R.layout.fragment_homepass_list, container, false);
        ButterKnife.bind(this, view);
        setView();
        swipe.setOnRefreshListener(this::setView);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof OnHomepassInteractionListener) {
            mListener = (OnHomepassInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnHomepassInteractionListener");
        }
    }

    /**
     * display UI with data
     */
    public void setView(){
        rvHomepasses.setLayoutManager(new LinearLayoutManager(context));
        editSearch.getText().clear();
        editSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        loadHomepasses();
        //TO DO implement live search if needed
//        editSearch.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {}
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (adapter!=null){
//                    new Handler().postDelayed(() -> {
//                        String query = s.toString();
//                        searchHomepass(query);
//                    }, 3000); //will search after 3 seconds
//                } else {
//                    String message = "No homepass loaded";
//                    Log.d(TAG, message);
//                    if (context!=null)
//                    ViewUtils.dialogError(context,"Failed", message)
//                            .create().show();
//                }
//            }
//        });
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

    /**
     * load homepass list
     */
    private void loadHomepasses(){
        onProgressLoading(true);
        IxitaskService.getApi().getHomepasses(userId, userKey).enqueue(new Callback<ResponseHomepass>() {
            @Override
            public void onResponse(Call<ResponseHomepass> call, Response<ResponseHomepass> response) {
                onProgressLoading(false);
                displayEmptyView(true);
                ResponseHomepass res = response.body();
                if (res != null){
                    int status = Integer.parseInt(res.getStatus());
                    Log.d(TAG, res.getStatusMessage());
                    if (status==200){
                        displayEmptyView(false);
                        List<ResponseHomepass.Homepass> homepasses = new ArrayList<>(res.getData().getHomepasses());
                        adapter = new HomepassAdapter(homepasses, mListener, context);
                        rvHomepasses.setAdapter(adapter);
                        textCount.setText(context.getString(R.string.homepass_count, adapter.getItemCount()));
                    } else {
                        if (context!=null) {
                            if (dialog!=null) dialog.dismiss();
                            dialog = ViewUtils
                                    .dialogError(context, res.getStatus(), res.getStatusMessage())
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
            public void onFailure(Call<ResponseHomepass> call, Throwable t) {
                onProgressLoading(false);
                displayEmptyView(true);
                String message = context.getString(R.string.error_failed_homepass);
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
                                    .setPositiveButton(context.getString(R.string.btn_retry),
                                            (d, w) -> call.clone().enqueue(this))
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
            rvHomepasses.setVisibility(View.INVISIBLE);
            textEmpty.setVisibility(View.VISIBLE);
        } else {
            rvHomepasses.setVisibility(View.VISIBLE);
            textEmpty.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * This function is to perform filtering that is implemented the adapter
     * @see com.ixitask.ixitask.fragments.HomepassAdapter
     * @param query the search query to filter the homepass list
     */
    private void searchHomepass(String query){
        onProgressLoading(true);
        Log.d(TAG, "Filtering list");
        adapter.getFilter().filter(query, count -> { //count = homepasses found
            onProgressLoading(false);
            Log.d(TAG, "Filtering completed");
            textCount.setText(context.getString(R.string.homepass_count, adapter.getItemCount()));
        });
    }

    /**
     * This interface is to be implemented on HomeActivity
     * @see com.ixitask.ixitask.activities.HomeActivity
     */
    public interface OnHomepassInteractionListener {
        /**
         * a listener when a homepass clicked
         * @param homepass the homepass object to be passed
         */
        void onHomepassClick(ResponseHomepass.Homepass homepass);
        void onHomepassClick(ResponseSummaryRes.SummaryRes homeSummary);
        void openSideNavigation(boolean wantOpen);
    }

    private void onProgressLoading(boolean isLoading) {
        swipe.post(()->swipe.setRefreshing(isLoading));
        if (isLoading) ViewUtils.hideKeyboard(context, editSearch);
    }
}
