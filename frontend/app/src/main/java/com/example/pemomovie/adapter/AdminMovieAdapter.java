package com.example.pemomovie.adapter;

import android.content.Context;
import android.graphics.Color;
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
import java.util.ArrayList;
import java.util.List;
import android.widget.Filter;
import android.widget.Filterable;

public class AdminMovieAdapter extends RecyclerView.Adapter<AdminMovieAdapter.ViewHolder> implements Filterable {

    private final Context context;
    private List<MediaItemDto> movies = new ArrayList<>();
    private List<MediaItemDto> moviesFull = new ArrayList<>();
    private final OnItemClickListener listener;
    private boolean isInactiveTab = false;
    private boolean hideEditButton = false;
    private boolean hideDeleteButton = false;

    public interface OnItemClickListener {
        void onItemClick(MediaItemDto movie);
        void onDeleteClick(MediaItemDto movie);
    }

    public AdminMovieAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setMovies(List<MediaItemDto> movies) {
        this.movies = movies;
        this.moviesFull = new ArrayList<>(movies);
        notifyDataSetChanged();
    }

    public void setInactiveTab(boolean isInactiveTab) {
        this.isInactiveTab = isInactiveTab;
    }

    public void setHideEditButton(boolean hideEditButton) {
        this.hideEditButton = hideEditButton;
    }

    public void setHideDeleteButton(boolean hideDeleteButton) {
        this.hideDeleteButton = hideDeleteButton;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaItemDto movie = movies.get(position);
        holder.tvTitle.setText(movie.getTitle());
        holder.tvType.setText(movie.getMediaType() != null ? movie.getMediaType() : "Movie");
        
        holder.tvRating.setText(movie.getVoteAverage() != null ? String.format(java.util.Locale.US, "%.1f", movie.getVoteAverage()) : "N/A");
        
        if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
            holder.tvGenres.setText(String.join(", ", movie.getGenres()));
        } else {
            holder.tvGenres.setText("Chưa cập nhật thể loại");
        }
        
        Integer views = movie.getViewCount();
        if (views != null) {
            String viewsStr;
            if (views >= 1000000) {
                viewsStr = String.format(java.util.Locale.US, "%.1fM lượt xem", views / 1000000.0);
            } else if (views >= 1000) {
                viewsStr = String.format(java.util.Locale.US, "%.1fK lượt xem", views / 1000.0);
            } else {
                viewsStr = views + " lượt xem";
            }
            holder.tvViews.setText(viewsStr);
        } else {
            holder.tvViews.setText("0 lượt xem");
        }
        
        if (isInactiveTab) {
            holder.btnDelete.setImageResource(R.drawable.ic_save); // Restore icon
            holder.btnDelete.setColorFilter(Color.parseColor("#10B981")); // Green to restore
        } else {
            holder.btnDelete.setImageResource(R.drawable.ic_delete); // Delete icon
            holder.btnDelete.setColorFilter(Color.parseColor("#EF4444")); // Red to delete
        }
        
        if (holder.btnEdit != null) {
            if (hideEditButton) {
                ((View) holder.btnEdit.getParent()).setVisibility(View.GONE);
            } else {
                ((View) holder.btnEdit.getParent()).setVisibility(View.VISIBLE);
            }
        }
        
        if (holder.btnDelete != null) {
            if (hideDeleteButton) {
                ((View) holder.btnDelete.getParent()).setVisibility(View.GONE);
            } else {
                ((View) holder.btnDelete.getParent()).setVisibility(View.VISIBLE);
            }
        }

        Glide.with(context)
                .load(movie.getPosterUrl())
                .placeholder(R.drawable.bg_poster_rounded)
                .into(holder.ivPoster);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(movie));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(movie));
        if (holder.btnEdit != null) {
            holder.btnEdit.setOnClickListener(v -> listener.onItemClick(movie)); // Reuse item click for edit
        }
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<MediaItemDto> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(moviesFull);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (MediaItemDto item : moviesFull) {
                        if (item.getTitle() != null && item.getTitle().toLowerCase().contains(filterPattern)) {
                            filteredList.add(item);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                movies.clear();
                if (results.values != null) {
                    movies.addAll((List) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster, btnDelete, btnEdit;
        TextView tvTitle, tvType, tvRating, tvGenres, tvViews;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvType = itemView.findViewById(R.id.tvType);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvGenres = itemView.findViewById(R.id.tvGenres);
            tvViews = itemView.findViewById(R.id.tvViews);
        }
    }
}
