package com.ixitask.ixitask.fragments;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.ixitask.ixitask.R;
import com.ixitask.ixitask.fragments.HomepassFragment.OnHomepassInteractionListener;
import com.ixitask.ixitask.models.ResponseHomepass.Homepass;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomepassAdapter extends RecyclerView.Adapter<HomepassAdapter.ViewHolder> implements Filterable{

    private static final String TAG = HomepassAdapter.class.getSimpleName();

    private List<Homepass> homepasses;
    private List<Homepass> homepassesFiltered;
    private final OnHomepassInteractionListener mListener;
    private final Context context;

    public HomepassAdapter(List<Homepass> homepasses,
                           HomepassFragment.OnHomepassInteractionListener listener,
                           Context context) {
        this.homepasses = homepasses;
        this.homepassesFiltered = homepasses;
        this.mListener = listener;
        this.context = context;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_homepass, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Homepass homepass = homepassesFiltered.get(position);
        if (homepass!=null){
            holder.textStreet.setText(homepass.getStreetName());
            boolean isOpen = Boolean.parseBoolean(homepass.getOpen());
            Log.d(TAG, homepass.getOpen());
            holder.imgOpen.setVisibility(isOpen ? View.VISIBLE : View.INVISIBLE);
            holder.textComplex.setText(homepass.getComplex());
            holder.textName.setText(homepass.getOwner());
            holder.textPhone.setText(homepass.getPhone());
            holder.textDate.setText(context.getString(R.string.homepass_last_contact_value,
                    homepass.getContactdate(),
                    homepass.getContactby())); //value terbalik
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
                List<Homepass> filteredList = new ArrayList<>();
                if (query.isEmpty()){
                    homepassesFiltered = homepasses;
                } else {
                    for (Homepass homepass : homepasses){
                        boolean isQueried = homepass.getStreetName().toLowerCase().contains(query)
                                || homepass.getOwner().toLowerCase().contains(query);
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
                homepassesFiltered = (List<Homepass>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
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


        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

//        @Override
//        public String toString() {
//            return super.toString() + " '" + mContentView.getText() + "'";
//        }
    }
}
