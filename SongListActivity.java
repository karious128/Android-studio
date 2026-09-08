package com.example.a25app1_23251109128_zgq_jellymusic;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class SongListActivity extends AppCompatActivity {

    private ListView listViewSongs;
    private List<SongItem> songItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        initViews();
        loadSongData();
        setupListView();
    }

    private void initViews() {
        listViewSongs = findViewById(R.id.listViewSongs);
        songItems = new ArrayList<>();
    }

    private void loadSongData() {
        // 中文歌曲数据
        String[] chineseTitles = { "花海", "爱你没差", "彩虹", "可爱女人", "戒烟", "普通朋友", "哥只是个传说", "快乐环岛", "背叛", "天天" };
        String[] chineseArtists = { "周杰伦", "周杰伦", "周杰伦", "周杰伦", "李荣浩", "陶喆", "陈旭", "TFBOYS", "曹格", "陶喆" };

        for (int i = 0; i < 10; i++) {
            songItems.add(new SongItem(chineseTitles[i], chineseArtists[i], "chinese"));
        }

        // 英文歌曲数据
        String[] englishTitles = { "STAY", "Maps", "Despacito", "Shape of You", "Welcome To New York", "Come Around Me", "Cruel Summer", "SPINNIN' ON IT", "Cupid", "Love on top" };
        String[] englishArtists = { "The Kid LAROI & Justin Bieber", "Maroon 5", "Luis Fonsi & Daddy Yankee & Justin Bieber", "Ed Sheeran", "Taylor Swift", "Justin Bieber", "Taylor Swift", "NMIXX", "FIFTY FIFTY", "Beyonce" };

        for (int i = 0; i < 10; i++) {
            songItems.add(new SongItem(englishTitles[i], englishArtists[i], "english"));
        }
    }

    private void setupListView() {
        SongListAdapter adapter = new SongListAdapter();
        listViewSongs.setAdapter(adapter);

        listViewSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SongItem item = songItems.get(position);
                String language = "chinese".equals(item.type) ? "中" : "英";
                String message = "该歌曲是\"" + item.songName + "\",歌手是\"" + item.artistName + "\",这是一首" + language + "文歌";
                Toast.makeText(SongListActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class SongItem {
        String songName;
        String artistName;
        String type; // "chinese" or "english"

        SongItem(String songName, String artistName, String type) {
            this.songName = songName;
            this.artistName = artistName;
            this.type = type;
        }
    }

    private class SongListAdapter extends ArrayAdapter<SongItem> {
        SongListAdapter() {
            super(SongListActivity.this, 0, songItems);
        }

        @Override
        public View getView(int position, View convertView, android.view.ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_song_list, parent, false);
            }

            SongItem item = songItems.get(position);
            TextView tvSongName = convertView.findViewById(R.id.tvSongName);
            TextView tvArtistName = convertView.findViewById(R.id.tvArtistName);
            ImageView ivSongType = convertView.findViewById(R.id.ivSongType);

            tvSongName.setText(item.songName);
            tvArtistName.setText(item.artistName);

            // 设置图片
            if ("chinese".equals(item.type)) {
                ivSongType.setImageResource(R.drawable.chinese);
            } else {
                ivSongType.setImageResource(R.drawable.english);
            }

            return convertView;
        }
    }
}