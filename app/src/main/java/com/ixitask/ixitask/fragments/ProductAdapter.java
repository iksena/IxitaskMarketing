package com.ixitask.ixitask.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ixitask.ixitask.R;
import com.ixitask.ixitask.models.ResponseProduct;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<ResponseProduct.Product> products;
    private OnProductInteractionListener listener;

    public interface OnProductInteractionListener {
        void onProductDelete(ResponseProduct.Product product);
    }

    public ProductAdapter(Context context, List<ResponseProduct.Product> products,
                          OnProductInteractionListener listener) {
        this.context = context;
        this.products = products;
        this.listener = listener;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_products, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ResponseProduct.Product product = products.get(position);
        if (product!=null){
            holder.textTitle.setText(product.proName);
            holder.itemView.setTag(product.proPrice);
            holder.btnDelete.setOnClickListener(v->{
                listener.onProductDelete(product);
            });
        }
    }

    @Override
    public int getItemCount() {
        return products!=null ? products.size() : 0;
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_title)
        TextView textTitle;
        @BindView(R.id.btn_delete)
        ImageButton btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
