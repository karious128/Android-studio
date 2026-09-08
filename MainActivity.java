package com.example.a25app1_23251109128_zgq_jellymusic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity {
    private TextView tvUsername, tvCurrentSong, tvCurrentTime,tvBirthday;;
    private ImageView ivUserType, ivVipType; // ✅ 独立的VIP图片和头像图片
    private Button btnVip, btnPrev, btnPlay, btnNext, btnStop, btnFeedback;
    private SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    private Spinner spinnerPlayMode;
    private Handler handler = new Handler();
    private boolean isPlaying = false;
    private boolean isVip = false;
    private String currentPlaylist = "";
    private List<Integer> songList = new ArrayList<>();
    private int currentSongIndex = 0;
    private boolean isRandomMode = false;
    private boolean isBackgroundMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        loadUserInfo();
        setupClickListeners();
        setupPlayModeSpinner();
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isPlaying) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    int duration = mediaPlayer.getDuration();
                    tvCurrentTime.setText(formatTime(currentPosition) + "/" + formatTime(duration));
                    seekBar.setProgress(currentPosition);
                    handler.postDelayed(this, 1000);
                }
            }
        };
    }

    private void initViews() {
        tvUsername = findViewById(R.id.tvUsername);
        ivVipType = findViewById(R.id.ivVipType); // VIP图片
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        btnVip = findViewById(R.id.btnVip);
        btnPrev = findViewById(R.id.btnPrev);
        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        btnStop = findViewById(R.id.btnStop);
        seekBar = findViewById(R.id.seekBar);
        tvCurrentSong = findViewById(R.id.tvCurrentSong);
        btnFeedback = findViewById(R.id.btnFeedback);
        spinnerPlayMode = findViewById(R.id.spinnerPlayMode);
        tvBirthday = findViewById(R.id.tvBirthday);
    }

    private void loadUserInfo() {
        String username = getIntent().getStringExtra("username");
        tvUsername.setText(username);

        SharedPreferences sp = getSharedPreferences("login_prefs", MODE_PRIVATE);
        isVip = sp.getBoolean("isVip", false);
        updateVipIcon();

        // 从userDatabase中读取生日
        JSONObject userDb = loadUserDatabase();
        if (userDb.has(username)) {
            try {
                JSONObject userData = userDb.getJSONObject(username);
                String birthday = userData.getString("birthday");
                tvBirthday.setText("生日: " + birthday);
            } catch (JSONException e) {
                e.printStackTrace();
                tvBirthday.setText("生日: 未知");
            }
        } else {
            tvBirthday.setText("生日: 未知");
        }
    }

    private JSONObject loadUserDatabase() {
        SharedPreferences sp = getSharedPreferences("login_prefs", MODE_PRIVATE);
        String jsonStr = sp.getString("user_database", "{}");
        try {
            return new JSONObject(jsonStr);
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private void updateVipIcon() {
        ivVipType.setImageResource(isVip ? R.drawable.vip : R.drawable.vip_disabled);
    }

    private void setupPlayModeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.play_mode_options,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerPlayMode.setAdapter(adapter);

        // 从SharedPreferences中获取当前播放模式
        isRandomMode = getSharedPreferences("user", MODE_PRIVATE)
                .getBoolean("random_mode", false);

        // 设置Spinner的选中项
        spinnerPlayMode.setSelection(isRandomMode ? 1 : 0);

        // 设置Spinner的点击监听器
        spinnerPlayMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                isRandomMode = (position == 1);
                // 保存到SharedPreferences
                getSharedPreferences("user", MODE_PRIVATE)
                        .edit()
                        .putBoolean("random_mode", isRandomMode)
                        .apply();
                Toast.makeText(MainActivity.this,
                        isRandomMode ? "随机播放已开启" : "顺序播放已开启",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupClickListeners() {
        btnVip.setOnClickListener(v -> showVipDialog());
        findViewById(R.id.ivEnglish).setOnClickListener(v -> openPlaylist("english"));
        findViewById(R.id.ivChinese).setOnClickListener(v -> openPlaylist("chinese"));
        btnPlay.setOnClickListener(v -> togglePlay());
        btnStop.setOnClickListener(v -> stopMusic());
        btnPrev.setOnClickListener(v -> playPrev());
        btnNext.setOnClickListener(v -> playNext());
        btnFeedback.setOnClickListener(v -> showFeedbackDialog());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void showVipDialog() {
        if (isVip) {
            Toast.makeText(this, "您已成为VIP用户", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("VIP激活");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("确定", (dialog, which) -> {
            String key = input.getText().toString().trim();
            if ("key".equals(key)) {
                isVip = true;
                updateVipIcon();
                SharedPreferences sp = getSharedPreferences("login_prefs", MODE_PRIVATE);
                sp.edit().putBoolean("isVip", true).apply();
                Toast.makeText(this, "VIP激活成功！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "密钥错误！", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void openPlaylist(String type) {
        currentPlaylist = type;
        songList.clear();
        for (int i = 1; i <= 10; i++) {
            String name = type.equals("english") ? "esong" + i : "csong" + i;
            int resId = getResources().getIdentifier(name, "raw", getPackageName());
            if (resId != 0) {
                songList.add(resId);
            }
        }
        if (songList.isEmpty()) {
            Toast.makeText(this, "歌单资源未找到，请检查 raw 文件夹", Toast.LENGTH_SHORT).show();
            return;
        }
        currentSongIndex = 0;
        Intent intent = new Intent(this, PlaylistActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("songList", new ArrayList<>(songList));
        startActivity(intent);
    }

    private void togglePlay() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            btnPlay.setText("▶");
        } else if (mediaPlayer != null) {
            mediaPlayer.start();
            isPlaying = true;
            btnPlay.setText("⏸");
            handler.post(updateSeekBar);
        } else {
            Toast.makeText(this, "请先选择歌单", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            btnPlay.setText("▶");
            tvCurrentSong.setText("当前播放：无");
            seekBar.setProgress(0);
            handler.removeCallbacks(updateSeekBar);
        }
    }

    private void playPrev() {
        if (songList.isEmpty()) return;
        currentSongIndex--;
        if (currentSongIndex < 0) currentSongIndex = songList.size() - 1;
        playSongAt(currentSongIndex);
    }

    private void playNext() {
        if (songList.isEmpty()) return;
        currentSongIndex = (currentSongIndex + 1) % songList.size();
        playSongAt(currentSongIndex);
    }

    private void playSongAt(int index) {
        if (index < 0 || index >= songList.size()) return;
        stopMusic();
        currentSongIndex = index;
        int resId = songList.get(index);
        mediaPlayer = MediaPlayer.create(this, resId);
        if (mediaPlayer == null) {
            Toast.makeText(this, "无法加载歌曲", Toast.LENGTH_SHORT).show();
            return;
        }
        mediaPlayer.setOnPreparedListener(mp -> {
            mp.start();
            isPlaying = true;
            btnPlay.setText("⏸");
            tvCurrentSong.setText("当前播放：" + getSongTitle(resId));
            seekBar.setMax(mediaPlayer.getDuration());
            handler.post(updateSeekBar);
        });
        mediaPlayer.setOnCompletionListener(mp -> {
            playNext();
        });
    }

    private String getSongTitle(int resId) {
        String title = "";
        if (resId == R.raw.esong1) title = "STAY - The Kid LAROI & Justin Bieber";
        else if (resId == R.raw.esong2) title = "Maps - Maroon 5";
        else if (resId == R.raw.esong3) title = "Despacito - Luis Fonsi & Daddy Yankee & Justin Bieber";
        else if (resId == R.raw.esong4) title = "Shape of You - Ed Sheeran";
        else if (resId == R.raw.esong5) title = "Welcome To New York - Taylor Swift";
        else if (resId == R.raw.esong6) title = "Come Around Me - Justin Bieber";
        else if (resId == R.raw.esong7) title = "Cruel Summer - Taylor Swift";
        else if (resId == R.raw.esong8) title = "SPINNIN' ON IT - NMIXX";
        else if (resId == R.raw.esong9) title = "Cupid - FIFTY FIFTY";
        else if (resId == R.raw.esong10) title = "Love on top - Beyonce";
        else if (resId == R.raw.csong1) title = "花海 - 周杰伦";
        else if (resId == R.raw.csong2) title = "爱你没差 - 周杰伦";
        else if (resId == R.raw.csong3) title = "彩虹 - 周杰伦";
        else if (resId == R.raw.csong4) title = "可爱女人 - 周杰伦";
        else if (resId == R.raw.csong5) title = "戒烟 - 李荣浩";
        else if (resId == R.raw.csong6) title = "普通朋友 - 陶喆";
        else if (resId == R.raw.csong7) title = "哥只是个传说 - 陈旭";
        else if (resId == R.raw.csong8) title = "快乐环岛 - TFBOYS";
        else if (resId == R.raw.csong9) title = "背叛 - 曹格";
        else if (resId == R.raw.csong10) title = "天天 - 陶喆";
        return title;
    }

    private void showFeedbackDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("问题反馈");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("提交", (dialog, which) -> {
            String feedback = input.getText().toString().trim();
            if (!feedback.isEmpty()) {
                saveFeedbackToFile(feedback);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void saveFeedbackToFile(String content) {
        try {
            File file = new File(getFilesDir(), "23251109128zgqQuestion.txt");
            FileWriter writer = new FileWriter(file, true);
            writer.write("\n" + content);
            writer.close();
            Toast.makeText(this, "反馈已保存", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_english_playlist) {
            openPlaylist("english");
            return true;
        } else if (item.getItemId() == R.id.action_chinese_playlist) {
            openPlaylist("chinese");
            return true;
        } else if (item.getItemId() == R.id.action_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            return true;
        }else if (item.getItemId() == R.id.action_song_list) {
            Intent intent = new Intent(this, SongListActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            btnPlay.setText("▶");
        }
    }

    private String formatTime(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPlaying) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int duration = mediaPlayer.getDuration();
                tvCurrentTime.setText(formatTime(currentPosition) + "/" + formatTime(duration));
                seekBar.setProgress(currentPosition);
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (isBackgroundMode && mediaPlayer != null) {
            mediaPlayer.start();
            isPlaying = true;
            btnPlay.setText("⏸");
            handler.post(updateSeekBar);
            isBackgroundMode = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}