package com.ixitask.ixitask.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ixitask.ixitask.R;
import com.ixitask.ixitask.models.ResponseSlot;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SlotAdapter extends RecyclerView.Adapter<SlotAdapter.SlotViewHolder> {

    private Context context;
    private List<ResponseSlot.Slot> slots;
    private OnSlotInteractionListener listener;

    public interface OnSlotInteractionListener {
        void onSlotClick(ResponseSlot.Slot slot);
    }

    public SlotAdapter(Context context, List<ResponseSlot.Slot> slots, OnSlotInteractionListener listener) {
        this.context = context;
        this.slots = slots;
        this.listener = listener;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_slot, parent, false);
        return new SlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        ResponseSlot.Slot slot = slots.get(position);

        if (slot!=null){
            //TO DO add text formatting
            holder.textTitle.setText(slot.slotname);
            holder.textTime.setText(slot.slotname);
            if (Integer.parseInt(slot.availableslot)>0){
                holder.itemView.setEnabled(true);
                holder.textStatus.setText(context.getString(R.string.slot_available,slot.availableslot));
                holder.textStatus.setTextColor(context.getResources().getColor(R.color.darkGreen));
            } else {
                holder.itemView.setEnabled(false);
                holder.textStatus.setText(context.getString(R.string.slot_full));
                holder.textStatus.setTextColor(context.getResources().getColor(R.color.redCancelled));
            }

            holder.itemView.setOnClickListener(v->listener.onSlotClick(slot));
        }

    }

    @Override
    public int getItemCount() {
        return slots!=null ? slots.size() : 0;
    }

    class SlotViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text_title)
        TextView textTitle;
        @BindView(R.id.text_time)
        TextView textTime;
        @BindView(R.id.text_status)
        TextView textStatus;

        public SlotViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
