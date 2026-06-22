package com.example.pemomovie.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pemomovie.R;
import com.example.pemomovie.ui.main.MovieActivity;

import java.util.List;

public class GenreTagAdapter extends RecyclerView.Adapter<GenreTagAdapter.GenreViewHolder> {

    private Context context;
    private List<String> genreList;

    public GenreTagAdapter(Context context, List<String> genreList) {
        this.context = context;
        this.genreList = genreList;
    }

    @NonNull
    @Override
    public GenreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_genre_tag, parent, false);
        return new GenreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenreViewHolder holder, int position) {
        String genre = genreList.get(position);
        holder.tvGenreName.setText(genre);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MovieActivity.class);
            intent.putExtra("GENRE_NAME", genre);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return genreList != null ? genreList.size() : 0;
    }

    static class GenreViewHolder extends RecyclerView.ViewHolder {
        TextView tvGenreName;

        public GenreViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGenreName = itemView.findViewById(R.id.tvGenreName);
        }
    }
}
