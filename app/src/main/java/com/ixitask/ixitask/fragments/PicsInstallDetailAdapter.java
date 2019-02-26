package com.ixitask.ixitask.fragments;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ixitask.ixitask.R;
import com.ixitask.ixitask.fragments.InstallDetailFragment.OnListFragmentInteractionListener;
import com.ixitask.ixitask.models.ResponseInstallDetail;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PicsInstallDetailAdapter extends RecyclerView.Adapter<PicsInstallDetailAdapter.ViewHolder> {

    private final List<ResponseInstallDetail.Pic> pics;
    private final OnListFragmentInteractionListener mListener;
    private Context context;

    public PicsInstallDetailAdapter(List<ResponseInstallDetail.Pic> pics, OnListFragmentInteractionListener listener) {
        this.pics = pics;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_install_pics, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ResponseInstallDetail.Pic pic = pics.get(position);
        if (pic!=null && !TextUtils.isEmpty(pic.getPicture())) {
            Picasso.get()
                    .load(pic.getPicture())
                    .placeholder(context.getResources().getDrawable(R.drawable.picture_placeholder))
                    .error(context.getResources().getDrawable(R.drawable.picture_placeholder))
                    .into(holder.imageView);
            holder.itemView.setOnClickListener(v -> {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(pic.getPicture());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return pics!=null ? pics.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            imageView = (ImageView) view;
        }
    }
}
