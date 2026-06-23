package com.example.pemomovie.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pemomovie.R;
import com.example.pemomovie.model.Section;

import java.util.List;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.SectionViewHolder> {

    private Context context;
    private List<Section> sectionList;
    private final RecyclerView.RecycledViewPool sharedPool = new RecyclerView.RecycledViewPool();

    public SectionAdapter(Context context, List<Section> sectionList) {
        this.context = context;
        this.sectionList = sectionList;
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.section_item, parent, false);
        SectionViewHolder holder = new SectionViewHolder(view);
        holder.movieRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.movieRecycler.setRecycledViewPool(sharedPool);
        holder.posterAdapter = new PosterAdapter(context, new java.util.ArrayList<>());
        holder.movieRecycler.setAdapter(holder.posterAdapter);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        Section section = sectionList.get(position);
        holder.sectionTitle.setText(section.getTitle());

        holder.posterAdapter.updateData(section.getMovies());

        com.example.pemomovie.custom.GradientTextView.applyHorizontalGradient(
                holder.sectionSeeAll,
                android.graphics.Color.parseColor("#6C29D6"), // tím
                android.graphics.Color.parseColor("#F43393")  // hồng
        );

        holder.sectionSeeAll.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, com.example.pemomovie.ui.main.SeeAllActivity.class);
            intent.putExtra("SECTION_TITLE", section.getTitle());
            intent.putExtra("MOVIE_LIST", new java.util.ArrayList<>(section.getMovies()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return sectionList == null ? 0 : sectionList.size();
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView sectionTitle;
        TextView sectionSeeAll;
        RecyclerView movieRecycler;
        PosterAdapter posterAdapter;

        public SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            sectionTitle = itemView.findViewById(R.id.sectionTitle);
            sectionSeeAll = itemView.findViewById(R.id.sectionSeeAll);
            movieRecycler = itemView.findViewById(R.id.movieRecycler);
        }
    }
}
