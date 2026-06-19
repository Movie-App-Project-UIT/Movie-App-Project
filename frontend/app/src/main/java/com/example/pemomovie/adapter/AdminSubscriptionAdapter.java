package com.example.pemomovie.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pemomovie.R;
import com.example.pemomovie.mockdata.SubscriptionMock;
import java.util.ArrayList;
import java.util.List;

public class AdminSubscriptionAdapter extends RecyclerView.Adapter<AdminSubscriptionAdapter.ViewHolder> {
    private List<SubscriptionMock> subscriptions = new ArrayList<>();
    private Context context;

    public AdminSubscriptionAdapter(Context context) {
        this.context = context;
    }

    public void setSubscriptions(List<SubscriptionMock> subscriptions) {
        this.subscriptions = subscriptions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_subscription, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubscriptionMock sub = subscriptions.get(position);
        holder.txtSubName.setText(sub.getName());
        holder.txtSubPrice.setText(sub.getPrice());
        
        try {
            holder.txtSubName.setTextColor(Color.parseColor(sub.getColorHex()));
        } catch (Exception e) {}

        if (sub.isPopular()) {
            holder.lblPopular.setVisibility(View.VISIBLE);
        } else {
            holder.lblPopular.setVisibility(View.GONE);
        }

        StringBuilder benefitsText = new StringBuilder();
        for (String b : sub.getBenefits()) {
            benefitsText.append("✓ ").append(b).append("\n");
        }
        holder.txtBenefits.setText(benefitsText.toString().trim());
    }

    @Override
    public int getItemCount() {
        return subscriptions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtSubName, txtSubPrice, lblPopular, txtBenefits;
        View btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSubName = itemView.findViewById(R.id.txtSubName);
            txtSubPrice = itemView.findViewById(R.id.txtSubPrice);
            lblPopular = itemView.findViewById(R.id.lblPopular);
            txtBenefits = itemView.findViewById(R.id.txtBenefits);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
