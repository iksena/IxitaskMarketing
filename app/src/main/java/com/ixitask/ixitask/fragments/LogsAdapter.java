package com.ixitask.ixitask.fragments;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.vipulasri.timelineview.TimelineView;
import com.ixitask.ixitask.R;
import com.ixitask.ixitask.models.ResponseLogs;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.ViewHolder> {

    private final String ownerName;
    private final List<ResponseLogs.Log> logs;
    private Context context;

    public LogsAdapter(String ownerName, List<ResponseLogs.Log> logs) {
        this.ownerName = ownerName;
        this.logs = logs;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.fragment_log, parent, false);
        return new ViewHolder(view, getItemViewType(viewType));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ResponseLogs.Log log = logs.get(position);
        if (log!=null){
            holder.textName.setText(context.getString(R.string.log_name, ownerName));
            holder.textNote.setText(log.getActivityNote());
            holder.textDate.setText(context.getString(R.string.log_date,
                    log.getContactby(), log.getContactdate()));
        }
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.timeline)
        TimelineView timelineView;
        @BindView(R.id.text_name)
        TextView textName;
        @BindView(R.id.text_note)
        TextView textNote;
        @BindView(R.id.text_date)
        TextView textDate;

        public ViewHolder(View view, int viewType) {
            super(view);
            ButterKnife.bind(this, view);
            timelineView.initLine(viewType);
        }
    }
}
