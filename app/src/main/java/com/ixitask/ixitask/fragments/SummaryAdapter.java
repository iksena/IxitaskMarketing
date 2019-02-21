package com.ixitask.ixitask.fragments;

import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ixitask.ixitask.R;
import com.ixitask.ixitask.models.ResponseSummary;

import java.util.List;

public class SummaryAdapter extends RecyclerView.Adapter<SummaryAdapter.ViewHolder> {

    private final List<ResponseSummary.Summary> summaries;
    private final SummaryFragment.OnSummaryInteractionListener mListener;

    public SummaryAdapter(List<ResponseSummary.Summary> summaries, SummaryFragment.OnSummaryInteractionListener listener) {
        this.summaries = summaries;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_summary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.summary = summaries.get(position);
        if (holder.summary!=null){
            holder.textTitle.setText(holder.summary.hpscnote);
            holder.textCount.setText(holder.summary.hpsnum);
            holder.mView.setOnClickListener(v -> {
                if (null != mListener) {
                    mListener.onSummaryClick(holder.summary);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return null!=summaries ? summaries.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        @BindView(R.id.text_title)
        TextView textTitle;
        @BindView(R.id.text_count)
        TextView textCount;
        public ResponseSummary.Summary summary;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            ButterKnife.bind(this, view);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + textTitle.getText().toString() + "'";
        }
    }
}
