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

    public SectionAdapter(Context context, List<Section> sectionList) {
        this.context = context;
        this.sectionList = sectionList;
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.section_item, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        Section section = sectionList.get(position);
        holder.sectionTitle.setText(section.getTitle());

        PosterAdapter posterAdapter = new PosterAdapter(context, section.getMovies());
        holder.movieRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.movieRecycler.setAdapter(posterAdapter);

        holder.sectionSeeAll.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, com.example.pemomovie.ui.main.MovieActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
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

        public SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            sectionTitle = itemView.findViewById(R.id.sectionTitle);
            sectionSeeAll = itemView.findViewById(R.id.sectionSeeAll);
            movieRecycler = itemView.findViewById(R.id.movieRecycler);
        }
    }
}
