package edu.aueb.cs.distributedsystems;


import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocalCollectionFragment extends Fragment {

    public LocalCollectionFragment() {
        // Required empty public constructor
    }

    public LocalCollectionFragment(int songPosition, ThreadManager songThreadManager, MusicHandler mh, SeekBar seekBar, ImageButton play_pause_button, LinearLayout player_window, TextView songPositionTextView, TextView songDurationTextView, TextView songTitle, TextView songAuthorName, ImageButton skip_to_next_button, ImageButton skip_to_previous_button, List<String> musicFilesList, List<String> activeSongList){
        this.songPosition = songPosition;
        this.songThreadManager = songThreadManager;
        this.mh = mh;
        this.seekBar = seekBar;
        this.play_pause_button = play_pause_button;
        this.player_window = player_window;
        this.songPositionTextView = songPositionTextView;
        this.songDurationTextView = songDurationTextView;
        this.songTitle = songTitle;
        this.songAuthorName = songAuthorName;
        this.skip_to_next_button = skip_to_next_button;
        this.skip_to_previous_button = skip_to_previous_button;
        this.musicFilesList = musicFilesList;
        this.activeSongList = activeSongList;
    }

    private List<String> musicFilesList;
    private List<String> activeSongList;

    private int songPosition;
    ThreadManager songThreadManager;
    private MusicHandler mh;
    private SeekBar seekBar;
    private ImageButton play_pause_button;
    private LinearLayout player_window;
    private TextView songPositionTextView;
    private TextView songDurationTextView;
    private TextView songTitle;
    private TextView songAuthorName;
    private ImageButton skip_to_next_button;
    private ImageButton skip_to_previous_button;

    public List<String> getActiveSongList(){
        return activeSongList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_activity, container, false);

        final ListView listView_local = (ListView) view.findViewById(R.id.listView_local);
        final TextAdapterAuthor textAdapterSongs = new TextAdapterAuthor(R.layout.item_author,R.id.author_text_item);
        musicFilesList.clear();
        mh.fillMusicList(musicFilesList);
        textAdapterSongs.setData(musicFilesList);
        listView_local.setAdapter(textAdapterSongs);
        activeSongList = musicFilesList;

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int songProgress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                songProgress = progress;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mh.seekTo(songProgress * 1000);
            }
        });

        if(mh.isPlaying()){
            int songDuration = mh.getDuration();
            mh.playMusic(play_pause_button);
            seekBar.setMax(songDuration);
            seekBar.setVisibility(View.VISIBLE);
            songDurationTextView.setText(String.format("%02d", songDuration / 60) + ":" + String.format("%02d", songDuration % 60));
            songTitle.setText(mh.getSongMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            songAuthorName.setText(mh.getSongMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
        }

        play_pause_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mh.playerStopped()){
                    mh.playMusic(play_pause_button);
                    if (songThreadManager.isStopped()){
                        songThreadManager.resume();
                    }
                }else {
                    mh.pauseMusic(play_pause_button);
                    if (songThreadManager.isRunning()){
                        songThreadManager.interrupt();
                    }
                }
            }
        });

        listView_local.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                activeSongList = musicFilesList;
                final String musicFilePath = activeSongList.get(position);
                final int songDuration = mh.createMusicMediaPlayer(musicFilePath, player_window, position) / 1000;
                mh.playMusic(play_pause_button);
                seekBar.setMax(songDuration);
                seekBar.setVisibility(View.VISIBLE);
                songDurationTextView.setText(String.format("%02d", songDuration / 60) + ":" + String.format("%02d", songDuration % 60));
                songTitle.setText(mh.getSongMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
                songAuthorName.setText(mh.getSongMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));

                if (songThreadManager.isRunning()){
                    songThreadManager.interrupt();
                }
                songThreadManager.setValues(1000, seekBar, songPositionTextView, getActivity(), mh);
                songThreadManager.start();
            }
        });

        return view;
    }


}
