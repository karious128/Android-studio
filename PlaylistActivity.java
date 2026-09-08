package com.example.a25app1_23251109128_zgq_jellymusic;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PlaylistActivity extends AppCompatActivity {

    private String playlistType; // "english" or "chinese"
    private List<String> songNames = new ArrayList<>();
    private List<Integer> songResIds = new ArrayList<>();
    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private TextView tvNowPlaying, tvPlaylistTitle, tvTime; // 新增 tvTime
    private Button btnPlayPause, btnPrev, btnNext, btnStop, btnPlayAll, btnPlayMode;
    private Handler handler = new Handler();
    private Runnable updateSeekBar;
    private int currentPlayIndex = -1;
    private boolean isPlaying = false;
    private boolean isRandomMode = false; // 从 SharedPreferences 读取

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        initViews();
        loadPlaylistData();
        setupRecyclerView();
        setupControls();
        loadPlayMode();
        updatePlaylistTitle();
        updatePlayModeButtonText(); // 初始化按钮文本

        // 确保播放模式按钮正确显示
        btnPlayMode.setOnClickListener(v -> togglePlayMode());
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        seekBar = findViewById(R.id.seekBar);
        tvNowPlaying = findViewById(R.id.tvNowPlaying);
        tvPlaylistTitle = findViewById(R.id.tvPlaylistTitle);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnStop = findViewById(R.id.btnStop);
        btnPlayAll = findViewById(R.id.btnPlayAll);
        btnPlayMode = findViewById(R.id.btnPlayMode);
        tvTime = findViewById(R.id.tvTime); // 新增：获取时间显示TextView
    }

    private String formatDuration(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void loadPlaylistData() {
        playlistType = getIntent().getStringExtra("type");
        songNames.clear();
        songResIds.clear();

        if ("chinese".equals(playlistType)) {
            String[] chineseTitles = {
                    "花海", "爱你没差", "彩虹", "可爱女人",
                    "戒烟", "普通朋友", "哥只是个传说", "快乐环岛",
                    "背叛", "天天"
            };
            String[] chineseArtists = {
                    "周杰伦", "周杰伦", "周杰伦", "周杰伦",
                    "李荣浩", "陶喆", "陈旭", "TFBOYS",
                    "曹格", "陶喆"
            };

            for (int i = 0; i < 10; i++) {
                String resName = "csong" + (i + 1);
                int resId = getResources().getIdentifier(resName, "raw", getPackageName());
                if (resId != 0) {
                    songResIds.add(resId);
                    songNames.add(chineseTitles[i] + " - " + chineseArtists[i]);
                }
            }
        } else if ("english".equals(playlistType)) {
            String[] englishTitles = {
                    "STAY", "Maps", "Despacito", "Shape of You",
                    "Welcome To New York", "Come Around Me", "Cruel Summer", "SPINNIN' ON IT",
                    "Cupid", "Love on top"
            };
            String[] englishArtists = {
                    "The Kid LAROI & Justin Bieber", "Maroon 5", "Luis Fonsi & Daddy Yankee & Justin Bieber", "Ed Sheeran",
                    "Taylor Swift", "Justin Bieber", "Taylor Swift", "NMIXX",
                    "FIFTY FIFTY", "Beyonce"
            };

            for (int i = 0; i < 10; i++) {
                String resName = "esong" + (i + 1);
                int resId = getResources().getIdentifier(resName, "raw", getPackageName());
                if (resId != 0) {
                    songResIds.add(resId);
                    songNames.add(englishTitles[i] + " - " + englishArtists[i]);
                }
            }
        }
    }

    private void updatePlaylistTitle() {
        tvPlaylistTitle.setText(playlistType.equals("english") ? "英文歌单" : "中文歌单");
    }

    private void setupRecyclerView() {
        adapter = new SongAdapter(songNames, songResIds);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnPlayClickListener(position -> playSongAt(position));
        adapter.setOnDeleteClickListener(position -> showDeleteConfirm(position));
    }

    private void showDeleteConfirm(int position) {
        new AlertDialog.Builder(this)
                .setTitle("删除歌曲")
                .setMessage("确定要删除这首歌曲吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    // 实际无法删除 res/raw 文件，所以只模拟删除（教学用途）
                    Toast.makeText(this, "歌曲已从列表移除", Toast.LENGTH_SHORT).show();
                    songNames.remove(position);
                    songResIds.remove(position);
                    adapter.notifyItemRemoved(position);
                    if (currentPlayIndex == position) {
                        stopMusic();
                    } else if (currentPlayIndex > position) {
                        currentPlayIndex--;
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void setupControls() {
        btnPlayAll.setOnClickListener(v -> playSongAt(0));
        btnPlayPause.setOnClickListener(v -> {
            if (currentPlayIndex == -1 && !songResIds.isEmpty()) {
                playSongAt(0);
            } else {
                togglePlayPause();
            }
        });
        btnStop.setOnClickListener(v -> stopMusic());
        btnPrev.setOnClickListener(v -> playPrev());
        btnNext.setOnClickListener(v -> playNext());

        // SeekBar 拖动
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    updateCurrentTime(); // 拖动时更新时间
                }
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        // 自动更新进度
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    updateCurrentTime(); // 播放时更新时间
                    handler.postDelayed(this, 1000);
                }
            }
        };
    }

    private void loadPlayMode() {
        isRandomMode = getSharedPreferences("user", MODE_PRIVATE)
                .getBoolean("random_mode", false);
    }

    private void updatePlayModeButtonText() {
        btnPlayMode.setText(isRandomMode ? "顺序播放" : "随机播放");
    }

    private void togglePlayMode() {
        isRandomMode = !isRandomMode;
        // 保存到 SharedPreferences
        getSharedPreferences("user", MODE_PRIVATE)
                .edit()
                .putBoolean("random_mode", isRandomMode)
                .apply();
        updatePlayModeButtonText();
        Toast.makeText(this, isRandomMode ? "随机播放已开启" : "顺序播放已开启", Toast.LENGTH_SHORT).show();
    }

    private void playSongAt(int index) {
        if (index < 0 || index >= songResIds.size()) return;

        stopMusic();

        currentPlayIndex = index;
        int resId = songResIds.get(index);

        // 使用 MediaPlayer.create() 直接创建并准备
        mediaPlayer = MediaPlayer.create(this, resId);
        if (mediaPlayer == null) {
            Toast.makeText(this, "无法加载歌曲", Toast.LENGTH_SHORT).show();
            return;
        }

        mediaPlayer.setOnPreparedListener(mp -> {
            mp.start();
            isPlaying = true;
            btnPlayPause.setText("⏸");

            // 格式化时长并显示
            String duration = formatDuration(mp.getDuration());
            tvNowPlaying.setText("当前播放：" + songNames.get(currentPlayIndex) + " (" + duration + ")");

            seekBar.setMax(mediaPlayer.getDuration());
            updateCurrentTime(); // 初始化时间显示
            handler.post(updateSeekBar);
        });

        // 设置播放完成监听
        mediaPlayer.setOnCompletionListener(mp -> {
            playNextAuto();
        });
    }

    private void togglePlayPause() {
        if (mediaPlayer == null) return;
        if (isPlaying) {
            mediaPlayer.pause();
            btnPlayPause.setText("▶");
        } else {
            mediaPlayer.start();
            btnPlayPause.setText("⏸");
            handler.post(updateSeekBar);
        }
        isPlaying = !isPlaying;
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            btnPlayPause.setText("▶");
            tvNowPlaying.setText("当前播放：无");
            seekBar.setProgress(0);
            handler.removeCallbacks(updateSeekBar);
            currentPlayIndex = -1;
        }
    }

    private void playPrev() {
        if (songResIds.isEmpty()) return;
        int newIndex = currentPlayIndex - 1;
        if (newIndex < 0) newIndex = songResIds.size() - 1; // 循环
        playSongAt(newIndex);
    }

    private void playNext() {
        playNextAuto();
    }

    private void playNextAuto() {
        if (songResIds.isEmpty()) return;
        int newIndex;
        if (isRandomMode) {
            Random rand = new Random();
            newIndex = rand.nextInt(songResIds.size());
        } else {
            newIndex = (currentPlayIndex + 1) % songResIds.size();
        }
        playSongAt(newIndex);
    }

    // 新增：更新当前播放时间显示
    private void updateCurrentTime() {
        if (mediaPlayer != null) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            int totalDuration = mediaPlayer.getDuration();

            String currentTime = formatDuration(currentPosition);
            String totalTime = formatDuration(totalDuration);

            tvTime.setText(currentTime + "-" + totalTime);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}