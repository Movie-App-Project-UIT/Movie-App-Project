package com.example.pemomovie.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.pemomovie.R;
import com.example.pemomovie.dto.MediaItemDto;
import com.example.pemomovie.ui.main.PlayActivity;
import java.util.List;

public class GlobalSearchAdapter extends RecyclerView.Adapter<GlobalSearchAdapter.ViewHolder> {

    private List<MediaItemDto> items;
    private Context context;

    public GlobalSearchAdapter(Context context, List<MediaItemDto> items) {
        this.context = context;
        this.items = items;
    }

    public void updateData(List<MediaItemDto> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaItemDto item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        
        String year = "N/A";
        String type = "TV_SHOW".equals(item.getMediaType()) ? "Phim Bộ" : "Phim Lẻ";
        holder.tvInfo.setText(year + " • " + type);

        String imageUrl = item.getPosterUrl() != null ? item.getPosterUrl() : item.getBackdropUrl();
        Glide.with(context)
             .load(imageUrl)
             .placeholder(R.drawable.ic_avatar)
             .centerCrop()
             .into(holder.ivPoster);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlayActivity.class);
            intent.putExtra("MOVIE_ID", item.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvTitle;
        TextView tvInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivSearchPoster);
            tvTitle = itemView.findViewById(R.id.tvSearchTitle);
            tvInfo = itemView.findViewById(R.id.tvSearchInfo);
        }
    }
}
