package com.ixitask.ixitask.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ixitask.ixitask.R;
import com.ixitask.ixitask.activities.SettingsActivity;
import com.ixitask.ixitask.models.ResponseInstallDetail;
import com.ixitask.ixitask.services.IxitaskService;
import com.ixitask.ixitask.utils.Constants;
import com.ixitask.ixitask.utils.PermissionUtils;
import com.ixitask.ixitask.utils.ViewUtils;

import java.util.List;

public class InstallDetailFragment extends Fragment {

    private static final String TAG = InstallDetailFragment.class.getSimpleName();
    private OnListFragmentInteractionListener mListener;
    private Context context;
    private AlertDialog dialog;
    private PicsInstallDetailAdapter adapter;
    private String userId;
    private String userKey;
    private String serviceId;

    @BindView(R.id.text_date)
    TextView textDate;
    @BindView(R.id.text_owner)
    TextView textOwner;
    @BindView(R.id.list_products)
    ListView listProducts;
    @BindView(R.id.text_fee_monthly)
    TextView textMonthly;
    @BindView(R.id.text_fee_prorate)
    TextView textProrate;
    @BindView(R.id.text_fee_install)
    TextView textInstall;
    @BindView(R.id.text_location)
    TextView textLocation;
    @BindView(R.id.rv_photos)
    RecyclerView rvPhotos;
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

    public InstallDetailFragment() {
    }

    public static InstallDetailFragment newInstance(String userId, String userKey, String serviceId) {
        InstallDetailFragment fragment = new InstallDetailFragment();
        Bundle args = new Bundle();
        args.putString(Constants.ARG_USER_ID, userId);
        args.putString(Constants.ARG_USER_KEY, userKey);
        args.putString(Constants.ARG_SERVICE_ID, serviceId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            userId = getArguments().getString(Constants.ARG_USER_ID);
            userKey = getArguments().getString(Constants.ARG_USER_KEY);
            serviceId = getArguments().getString(Constants.ARG_SERVICE_ID);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mListener==null){
            mListener = (OnListFragmentInteractionListener) requireContext();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        ViewUtils.hideKeyboard(context, getView());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_install_detail, container, false);
        ButterKnife.bind(this, view);
//            recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        setView();
        swipe.setOnRefreshListener(this::setView);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
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
        rvPhotos.setLayoutManager(new GridLayoutManager(context,2));
        btnMenuBack.setOnClickListener(v->mListener.onBackClick());
        btnBack.setOnClickListener(v->mListener.onBackClick());
        btnSettings.setOnClickListener(v->startActivity(new Intent(context, SettingsActivity.class)));

        onProgressLoading(true);
        IxitaskService.getApi().getInstallDetail(userId,userKey,serviceId).enqueue(new Callback<ResponseInstallDetail>() {
            @Override
            public void onResponse(Call<ResponseInstallDetail> call, Response<ResponseInstallDetail> response) {
                onProgressLoading(false);
                ResponseInstallDetail res = response.body();
                if (res!=null){
                    int status = Integer.parseInt(res.getStatus());
                    Log.d(TAG, res.getStatusMessage());
                    if (status==200){
                        if (res.getData().getRecords()!=null && !res.getData().getRecords().isEmpty()) {
                            ResponseInstallDetail.Record data = res.getData().getRecords().iterator().next();
                            textDate.setText(data.getSlot());
                            textOwner.setText(context.getString(R.string.install_detail_owner,data.getCustname(), data.getPhone(), data.getSonote()));
                            String[] products = data.getProductsStr();
                            if (products!=null && products.length > 0){
                                ArrayAdapter<String> productsAdapter = new ArrayAdapter<>(context,
                                        android.R.layout.simple_list_item_1, products);
                                listProducts.setAdapter(productsAdapter);
                            }
                            textMonthly.setText(context.getString(R.string.format_fee, data.getMonthlyfee()));
                            textInstall.setText(context.getString(R.string.format_fee, data.getInstallfee()));
                            textProrate.setText(context.getString(R.string.format_fee, data.getProrate()));
                            textLocation.setText(context.getString(R.string.install_detail_latlng,data.getLattitude(),data.getLongitude()));
                            List<ResponseInstallDetail.Pic> pics = data.getPics();
                            if (pics!=null && !pics.isEmpty()){
                                displayEmptyView(false);
                                adapter = new PicsInstallDetailAdapter(pics, mListener);
                                rvPhotos.setAdapter(adapter);
                            } else
                                displayEmptyView(true);
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
            }

            @Override
            public void onFailure(Call<ResponseInstallDetail> call, Throwable t) {
                onProgressLoading(false);
                displayEmptyView(true);
                String message = context.getString(R.string.failed_install_detail);
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

    private void displayEmptyView(boolean isEmpty){
        if (isEmpty){
            rvPhotos.setVisibility(View.INVISIBLE);
            textEmpty.setVisibility(View.VISIBLE);
        } else {
            rvPhotos.setVisibility(View.VISIBLE);
            textEmpty.setVisibility(View.INVISIBLE);
        }
    }

    private void onProgressLoading(boolean isLoading) {
        swipe.post(()->swipe.setRefreshing(isLoading));
//        if (isLoading) ViewUtils.hideKeyboard(context, editSearch);
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(String imageUrl);
        void openSideNavigation(boolean wantOpen);
        void onBackClick();
    }
}
