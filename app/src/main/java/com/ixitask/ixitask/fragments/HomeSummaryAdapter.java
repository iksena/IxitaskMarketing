package com.ixitask.ixitask.fragments;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.ixitask.ixitask.R;
import com.ixitask.ixitask.models.ResponseHomepass;
import com.ixitask.ixitask.models.ResponseSummaryRes;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeSummaryAdapter extends RecyclerView.Adapter<HomeSummaryAdapter.HomeSummaryViewHolder> implements Filterable {

    private static final String TAG = HomepassAdapter.class.getSimpleName();

    private List<ResponseSummaryRes.SummaryRes> homepasses;
    private List<ResponseSummaryRes.SummaryRes> homepassesFiltered;
    private final HomepassFragment.OnHomepassInteractionListener mListener;
    private final Context context;

    public HomeSummaryAdapter(List<ResponseSummaryRes.SummaryRes> homepasses,
                              HomepassFragment.OnHomepassInteractionListener mListener,
                              Context context) {
        this.homepasses = homepasses;
        this.homepassesFiltered = homepasses;
        this.mListener = mListener;
        this.context = context;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HomeSummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_homepass, parent, false);
        return new HomeSummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeSummaryViewHolder holder, int position) {
        ResponseSummaryRes.SummaryRes homepass = homepassesFiltered.get(position);
        if (homepass!=null){
            holder.textStreet.setText(homepass.streetName);
//            boolean isOpen = Boolean.parseBoolean(homepass);
//            Log.d(TAG, homepass.getOpen());
//            holder.imgOpen.setVisibility(isOpen ? View.VISIBLE : View.INVISIBLE);
            holder.textComplex.setText(homepass.streetName);
            holder.textName.setText(homepass.owner);
            holder.textPhone.setText(homepass.phone);
            holder.textDate.setText(context.getString(R.string.homepass_last_contact_value,
                    homepass.contactdate,
                    homepass.contactby)); //value terbalik
        }
        holder.itemView.setOnClickListener(v -> {
            if (null != mListener) {
                mListener.onHomepassClick(homepass);
            }
        });
    }

    @Override
    public int getItemCount() {
        return homepassesFiltered!=null ? homepassesFiltered.size() : 0;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint.toString().toLowerCase();
                List<ResponseSummaryRes.SummaryRes> filteredList = new ArrayList<>();
                if (query.isEmpty()){
                    homepassesFiltered = homepasses;
                } else {
                    for (ResponseSummaryRes.SummaryRes homepass : homepasses){
                        boolean isQueried = homepass.streetName.toLowerCase().contains(query)
                                || homepass.owner.toLowerCase().contains(query);
                        if (isQueried) {
                            filteredList.add(homepass);
                        }
                    }
                    homepassesFiltered = filteredList;
                }

                FilterResults results = new FilterResults();
                results.values = homepassesFiltered;
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                homepassesFiltered = (List<ResponseSummaryRes.SummaryRes>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    class HomeSummaryViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.text_street)
        TextView textStreet;
        @BindView(R.id.img_open)
        ImageView imgOpen;
        @BindView(R.id.text_complex)
        TextView textComplex;
        @BindView(R.id.text_owner_name)
        TextView textName;
        @BindView(R.id.text_phone)
        TextView textPhone;
        @BindView(R.id.text_date)
        TextView textDate;

        public HomeSummaryViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
