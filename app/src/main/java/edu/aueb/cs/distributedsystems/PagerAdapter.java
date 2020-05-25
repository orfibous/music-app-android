package edu.aueb.cs.distributedsystems;

import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class PagerAdapter extends FragmentPagerAdapter {

    private int songPosition;
    private ThreadManager songThreadManager;
    private MusicHandler mh;
    private final SeekBar seekBar;
    private ImageButton play_pause_button;
    private LinearLayout player_window;
    private TextView songPositionTextView;
    private TextView songDurationTextView;
    private TextView songTitle;
    private TextView songAuthorName;
    private ImageButton skip_to_next_button;
    private ImageButton skip_to_previous_button;
    private List<String> musicFilesList;
    private List<String> activeSongList;
    private Consumer consumer;
    private final Broker broker;

    private int numberOfTabs;
    public PagerAdapter(FragmentManager fm, int numberOfTabs, int songPosition, ThreadManager songThreadManager, MusicHandler mh, SeekBar seekBar, ImageButton play_pause_button, LinearLayout player_window, TextView songPositionTextView, TextView songDurationTextView, TextView songTitle, TextView songAuthorName, ImageButton skip_to_next_button, ImageButton skip_to_previous_button, List<String> musicFilesList, List<String> activeSongList, Consumer consumer, Broker broker){
        super(fm);
        this.numberOfTabs = numberOfTabs;
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
        this.consumer = consumer;
        this.broker = broker;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new CollectionFragment(songPosition, songThreadManager, mh, seekBar, play_pause_button, player_window, songPositionTextView, songDurationTextView, songTitle, songAuthorName, skip_to_next_button, skip_to_previous_button, musicFilesList, activeSongList, consumer, broker);
            case 1:
                return new LocalCollectionFragment(songPosition, songThreadManager, mh, seekBar, play_pause_button, player_window, songPositionTextView, songDurationTextView, songTitle, songAuthorName, skip_to_next_button, skip_to_previous_button, musicFilesList, activeSongList);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }
}
