package com.ixitask.ixitask.fragments;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.ixitask.ixitask.R;
import com.ixitask.ixitask.models.ResponseInstall;
import com.ixitask.ixitask.models.ResponseInstall.Install;

import java.util.ArrayList;
import java.util.List;

public class InstallationAdapter extends RecyclerView.Adapter<InstallationAdapter.ViewHolder> implements Filterable {

    private Context context;
    private List<ResponseInstall.Install> installs;
    private List<ResponseInstall.Install> installsFiltered;

    public InstallationAdapter(Context context, List<Install> installs) {
        this.context = context;
        this.installs = installs;
        this.installsFiltered = installs;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_installation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.install = installsFiltered.get(position);
        if (holder.install!=null){
            holder.textStreet.setText(holder.install.address);
            holder.textComplex.setText(holder.install.address);
            holder.textName.setText(holder.install.custname);
            holder.textScheduled.setText(holder.install.tglservice);
            holder.textStatus.setText(holder.install.sstatus);
            GradientDrawable background = (GradientDrawable) holder.textStatus.getBackground();
            background.setColor(ContextCompat.getColor(context,getStatusColor(holder.install.sstatus)));
//            holder.textStatus.setBackgroundColor();
        }
    }

    private int getStatusColor(String status){
        switch (status.toLowerCase()){
            case "completed":
                return R.color.greenCompleted;
            case "cancelled":
                return R.color.redCancelled;
            case "reschedule":
                return R.color.yellowRescheduled;
            case "pending":
                return R.color.bluePending;
            default:
                return R.color.greenCompleted;
        }
    }

    @Override
    public int getItemCount() {
        return installsFiltered!=null ? installsFiltered.size() : 0;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint.toString().toLowerCase();
                List<ResponseInstall.Install> filteredList = new ArrayList<>();
                if (query.isEmpty()){
                    installsFiltered = installs;
                } else {
                    for (ResponseInstall.Install install : installs){
                        boolean isFound = install.address.toLowerCase().contains(query)
                                || install.custname.toLowerCase().contains(query);
                        if (isFound) filteredList.add(install);
                    }
                    installsFiltered = filteredList;
                }

                FilterResults results = new FilterResults();
                results.values = installsFiltered;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                installsFiltered = (List<Install>) results.values;
                notifyDataSetChanged();
            }
        };
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        @BindView(R.id.text_street)
        TextView textStreet;
        @BindView(R.id.text_complex)
        TextView textComplex;
        @BindView(R.id.text_name)
        TextView textName;
        @BindView(R.id.text_scheduled)
        TextView textScheduled;
        @BindView(R.id.text_status)
        TextView textStatus;
        public ResponseInstall.Install install;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            ButterKnife.bind(this, view);
        }
    }
}
