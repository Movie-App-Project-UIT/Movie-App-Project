// WatchingAdapter.java
package com.example.pemomovie.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.pemomovie.R;
import com.example.pemomovie.dto.WatchHistoryItemDto;
import com.example.pemomovie.dto.MediaItemDto;
import com.example.pemomovie.dto.EpisodeDto;
import com.example.pemomovie.ui.main.PlayActivity;

import java.util.ArrayList;
import java.util.List;

public class WatchingAdapter extends RecyclerView.Adapter<WatchingAdapter.ViewHolder> {
    private List<WatchHistoryItemDto> historyList = new ArrayList<>();
    private boolean isVertical = false;

    public WatchingAdapter() {}

    public WatchingAdapter(boolean isVertical) {
        this.isVertical = isVertical;
    }

    public void setData(List<WatchHistoryItemDto> data) {
        List<WatchHistoryItemDto> filteredList = new ArrayList<>();
        if (data != null) {
            for (WatchHistoryItemDto item : data) {
                int progressSec = item.getProgressSeconds() != null ? item.getProgressSeconds() : 0;
                
                int totalDurationSec = 0;
                if (item.getTotalDurationSeconds() != null && item.getTotalDurationSeconds() > 0) {
                    totalDurationSec = item.getTotalDurationSeconds();
                } else {
                    int totalDurationMin = 0;
                    if (item.getEpisode() != null && item.getEpisode().getDuration() != null) {
                        totalDurationMin = item.getEpisode().getDuration();
                    } else if (item.getMedia() != null && item.getMedia().getDuration() != null) {
                        totalDurationMin = item.getMedia().getDuration();
                    }
                    if (totalDurationMin > 0) {
                        totalDurationSec = totalDurationMin * 60;
                    }
                }

                if (totalDurationSec > 0) {
                    float percent = (float) progressSec / totalDurationSec;
                    if (percent < 0.95f) {
                        filteredList.add(item);
                    }
                } else {
                    // Nếu không có duration, cứ hiển thị
                    filteredList.add(item);
                }
            }
        }
        this.historyList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_watching_movie, parent, false);
        if (isVertical) {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.setMargins(0, 0, 0, (int) (16 * view.getContext().getResources().getDisplayMetrics().density));
            view.setLayoutParams(params);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WatchHistoryItemDto item = historyList.get(position);
        MediaItemDto media = item.getMedia();
        EpisodeDto episode = item.getEpisode();

        if (media != null) {
            holder.txtMovieName.setText(media.getTitle());
            
            // Tải ảnh poster
            String imageUrl = media.getBackdropUrl() != null ? media.getBackdropUrl() : media.getPosterUrl();
            RequestOptions requestOptions = new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(16));
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .apply(requestOptions)
                    .into(holder.imgMoviePoster);

            int totalDuration = 0;
            if (episode != null) {
                holder.txtEpisode.setText(episode.getTitle() != null ? episode.getTitle() : ("Tập " + episode.getEpisodeNumber()));
                holder.txtEpisode.setVisibility(View.VISIBLE);
                if (episode.getDuration() != null) {
                    totalDuration = episode.getDuration();
                }
            } else {
                holder.txtEpisode.setVisibility(View.GONE);
                if (media.getDuration() != null) {
                    totalDuration = media.getDuration();
                }
            }

            int progressSec = item.getProgressSeconds() != null ? item.getProgressSeconds() : 0;
            
            int totalDurationSec = 0;
            if (item.getTotalDurationSeconds() != null && item.getTotalDurationSeconds() > 0) {
                totalDurationSec = item.getTotalDurationSeconds();
            } else if (totalDuration > 0) {
                totalDurationSec = totalDuration * 60;
            }
            
            if (totalDurationSec > 0) {
                int percent = (int) (((float) progressSec / totalDurationSec) * 100);
                if (percent > 100) percent = 100;
                holder.progressWatching.setProgress(percent);
                holder.txtPercentage.setText(percent + "%");
                
                int timeLeftSec = totalDurationSec - progressSec;
                if (timeLeftSec < 0) timeLeftSec = 0;
                int timeLeftMin = timeLeftSec / 60;
                holder.txtTimeLeft.setText("Còn " + timeLeftMin + " phút");
            } else {
                holder.progressWatching.setProgress(0);
                holder.txtPercentage.setText("0%");
                holder.txtTimeLeft.setText("Đã xem " + (progressSec / 60) + " phút");
            }

            // Xử lý sự kiện click để tiếp tục xem phim
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(holder.itemView.getContext(), PlayActivity.class);
                intent.putExtra("MOVIE_ID", media.getId());
                if (episode != null) {
                    intent.putExtra("EPISODE_ID", episode.getId());
                }
                intent.putExtra("START_POSITION", progressSec);
                holder.itemView.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return historyList != null ? historyList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgMoviePoster;
        TextView txtMovieName;
        TextView txtEpisode;
        ProgressBar progressWatching;
        TextView txtPercentage;
        TextView txtTimeLeft;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMoviePoster = itemView.findViewById(R.id.imgMoviePoster);
            txtMovieName = itemView.findViewById(R.id.txtMovieName);
            txtEpisode = itemView.findViewById(R.id.txtEpisode);
            progressWatching = itemView.findViewById(R.id.progressWatching);
            txtPercentage = itemView.findViewById(R.id.txtPercentage);
            txtTimeLeft = itemView.findViewById(R.id.txtTimeLeft);
        }
    }
}
