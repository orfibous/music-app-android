package edu.aueb.cs.distributedsystems;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class CollectionFragment extends Fragment {

    public CollectionFragment() {
        // Required empty public constructor
    }

    public CollectionFragment(int songPosition, ThreadManager songThreadManager, MusicHandler mh, SeekBar seekBar, ImageButton play_pause_button, LinearLayout player_window, TextView songPositionTextView, TextView songDurationTextView, TextView songTitle, TextView songAuthorName, ImageButton skip_to_next_button, ImageButton skip_to_previous_button, List<String> musicFilesList, List<String> activeSongList, Consumer consumer, Broker broker){
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
        this.musicFilesList =musicFilesList;
        this.activeSongList = activeSongList;
        this.consumer = consumer;
        this.broker = broker;
    }

    private List<String> musicFilesList;
    private List<String> activeSongList;
    private Consumer consumer;
    private Broker broker;
    private List<String> musicAuthorsList;
    private ArtistName artistNameList;

    private int songPosition = 0;
    private ThreadManager songThreadManager;
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_activity_authors, container, false);

        final ImageButton goBackButton = (ImageButton) view.findViewById(R.id.go_back_to_songs_button);
        final TextView authorNameHeader = (TextView) view.findViewById(R.id.author_name_header);
        final LinearLayout headerLayout = (LinearLayout) view.findViewById(R.id.header_layout);

        final ListView listView = (ListView) view.findViewById(R.id.listView);
        final ListView listViewAuthor = (ListView) view.findViewById(R.id.authors_listView);
        final TextAdapter textAdapterSongs = new TextAdapter(R.layout.item_song, R.id.song_text_item);
        final TextAdapter textAdapterAuthors = new TextAdapter(R.layout.item_author, R.id.author_text_item);


        musicAuthorsList = new ArrayList<>();
        mh.fillMusicList(musicFilesList);
        mh.fillAuthorList(musicAuthorsList, musicFilesList);
        textAdapterAuthors.setData(musicAuthorsList);
        textAdapterSongs.setData(musicFilesList);
        listView.setAdapter(textAdapterSongs);
        listViewAuthor.setAdapter(textAdapterAuthors);
        listViewAuthor.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        headerLayout.setVisibility(View.GONE);
        textAdapterSongs.setPlayParameters(activeSongList, mh, songThreadManager, seekBar, play_pause_button, player_window, songDurationTextView, songTitle, songAuthorName, songPositionTextView, getActivity(), consumer, broker, null);
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

        //Listerner for the button that returns to the Artist List  Menu

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                mh.fillMusicList(musicFilesList);
//                mh.fillAuthorList(musicAuthorsList, musicFilesList);
                textAdapterAuthors.setData(musicAuthorsList);
                textAdapterSongs.setData(musicFilesList);
                listView.setAdapter(textAdapterSongs);
                listViewAuthor.setAdapter(textAdapterAuthors);
                listViewAuthor.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
                headerLayout.setVisibility(View.GONE);
                activeSongList = musicFilesList;

            }
        });

        mh.getMp().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                int position =mh.getPositionInList();
                //Check position value to avoid IndexOutOfBounds exception
                if(position < activeSongList.size() -1) {
                    position++;
                }else{
                    position = 0;
                }
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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

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

        listViewAuthor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String artistName = musicAuthorsList.get(position);
                final List<String> tempList = mh.searchSongsOfAuthor(artistName,musicFilesList);
                activeSongList = tempList;
                textAdapterSongs.setData(tempList);
                listView.setAdapter(textAdapterSongs);
                listViewAuthor.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                headerLayout.setVisibility(View.VISIBLE);
                authorNameHeader.setText(artistName);
                textAdapterSongs.setPlayParameters(activeSongList, mh, songThreadManager, seekBar, play_pause_button, player_window, songDurationTextView, songTitle, songAuthorName, songPositionTextView, getActivity(), consumer, broker, artistName);
            }
        });

        return view;
    }
}

