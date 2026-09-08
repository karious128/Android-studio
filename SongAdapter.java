package com.example.a25app1_23251109128_zgq_jellymusic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private List<String> songNames;
    private List<Integer> songResIds;
    private OnDeleteClickListener onDeleteClickListener;
    private OnPlayClickListener onPlayClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public interface OnPlayClickListener {
        void onPlayClick(int position);
    }

    public SongAdapter(List<String> songNames, List<Integer> songResIds) {
        this.songNames = songNames;
        this.songResIds = songResIds;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    public void setOnPlayClickListener(OnPlayClickListener listener) {
        this.onPlayClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvSongName.setText(songNames.get(position));
        holder.itemView.setOnClickListener(v -> {
            if (onPlayClickListener != null) {
                onPlayClickListener.onPlayClick(position);
            }
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songNames.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSongName;
        Button btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvSongName = itemView.findViewById(R.id.tvSongName);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}