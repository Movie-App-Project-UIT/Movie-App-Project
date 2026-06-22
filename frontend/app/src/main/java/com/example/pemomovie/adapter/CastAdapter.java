package com.example.pemomovie.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.pemomovie.R;
import com.example.pemomovie.dto.CreditDto;

import java.util.List;

public class CastAdapter extends RecyclerView.Adapter<CastAdapter.CastViewHolder> {

    private Context context;
    private List<CreditDto> castList;

    public CastAdapter(Context context, List<CreditDto> castList) {
        this.context = context;
        this.castList = castList;
    }

    @NonNull
    @Override
    public CastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cast, parent, false);
        return new CastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CastViewHolder holder, int position) {
        CreditDto cast = castList.get(position);
        holder.tvCastName.setText(cast.getName() != null ? cast.getName() : "Unknown");
        
        if (cast.getProfileUrl() != null && !cast.getProfileUrl().isEmpty()) {
            Glide.with(context)
                    .load(cast.getProfileUrl())
                    .placeholder(R.drawable.ic_avatar)
                    .circleCrop()
                    .into(holder.ivCastImage);
        } else {
            holder.ivCastImage.setImageResource(R.drawable.ic_avatar);
        }
    }

    @Override
    public int getItemCount() {
        return castList != null ? castList.size() : 0;
    }

    static class CastViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCastImage;
        TextView tvCastName;

        public CastViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCastImage = itemView.findViewById(R.id.ivCastImage);
            tvCastName = itemView.findViewById(R.id.tvCastName);
        }
    }
}
