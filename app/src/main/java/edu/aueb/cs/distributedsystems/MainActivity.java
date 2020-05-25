package edu.aueb.cs.distributedsystems;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private  static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private static final  int REQUEST_PERMISSIONS = 12345;

    private static final int PERMISSIONS_COUNT = 1;

    @SuppressLint("NewApi")
    private boolean arePermissionsDenied(){
        for (int i = 0; i < PERMISSIONS_COUNT; i++){
            if(checkSelfPermission(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED){
                return true;
            }
        }
        return false;
    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult( requestCode, permissions, grantResults);
        if(arePermissionsDenied()){
            ((ActivityManager) (this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
            recreate();
        }else{
            onResume();
        }
    }

    private boolean isMusicPlayerInitialized;

    public Activity getActivity(){
        return  this;
    }

    List<String> activeSongList = new ArrayList<>();

    public void setActiveSongList(List<String> songList){
        activeSongList = songList;
    }

    public List<String> getActiveSongList(){
        return activeSongList;
    }

    public List<Fragment> getVisibleFragments() {
        List<Fragment> allFragments = getSupportFragmentManager().getFragments();
        if (allFragments == null || allFragments.isEmpty()) {
            return Collections.emptyList();
        }

        List<Fragment> visibleFragments = new ArrayList<Fragment>();
        for (Fragment fragment : allFragments) {
            if (fragment.isVisible()) {
                visibleFragments.add(fragment);
            }
        }
        return visibleFragments;
    }

    //Controls tabs
    @Override
    protected void onResume(){
        super.onResume();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && arePermissionsDenied()){
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
            return;
        }

        if(!isMusicPlayerInitialized){

            TabLayout tabLayout = findViewById(R.id.tab_layout);
            TabItem tabCollection = findViewById(R.id.tab_collection);
            TabItem tabLocalCollection = findViewById(R.id.tab_local_collection);
            final ViewPager viewPager = findViewById(R.id.view_pager);
            final SeekBar seekBar = findViewById(R.id.seekBar);
            final ImageButton play_pause_button = findViewById(R.id.play_pause_button);
            final LinearLayout player_window = findViewById(R.id.player_window);
            final TextView songPositionTextView = findViewById(R.id.song_current_position_text);
            final TextView songDurationTextView = findViewById(R.id.song_duration_text);
            final TextView songTitle = findViewById(R.id.song_title_text);
            final TextView songAuthorName = findViewById(R.id.song_author_text);
            final ImageButton skip_to_next_button = findViewById(R.id.skip_to_next_button);
            final ImageButton skip_to_previous_button = findViewById(R.id.skip_to_previous_button);
            final Consumer consumer = new Consumer();
            final Broker broker = new Broker();
            final List<String> musicFilesList = new ArrayList<>();

            int songPosition = 0;
            MusicHandler mh = new MusicHandler();
            final ThreadManager songThreadManager = new ThreadManager(1000, seekBar, songPositionTextView, this, mh);


            PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), songPosition, songThreadManager, mh, seekBar, play_pause_button, player_window, songPositionTextView, songDurationTextView, songTitle, songAuthorName, skip_to_next_button, skip_to_previous_button, musicFilesList, activeSongList, consumer, broker);

            viewPager.setAdapter(pagerAdapter);

            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    viewPager.setCurrentItem(tab.getPosition());
                    if(tab.getPosition() == 1){
                        musicFilesList.clear();
                        mh.fillMusicList(musicFilesList);
                        activeSongList = musicFilesList;
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

            skip_to_next_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<Fragment> fragmentList = getVisibleFragments();
                    List<String> skipSongList = null;
                    Fragment activeFragment = null;
                    System.out.println(tabLayout.getSelectedTabPosition() == 0 );
                    if (tabLayout.getSelectedTabPosition() == 0 ){
                        activeFragment = fragmentList.get(0);
                        skipSongList = ((CollectionFragment)activeFragment).getActiveSongList();
                    }else if(tabLayout.getSelectedTabPosition() == 1 ){
                        activeFragment = fragmentList.get(1);
                        skipSongList = ((LocalCollectionFragment)activeFragment).getActiveSongList();
                    }
                    int position =mh.getPositionInList();
                    //Check position value to avoid IndexOutOfBounds exception
                    if(position < skipSongList.size() -1) {
                        position++;
                    }else{
                        position = 0;
                    }
                    final String musicFilePath = skipSongList.get(position);
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

            skip_to_previous_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<Fragment> fragmentList = getVisibleFragments();
                    List<String> skipSongList = null;
                    Fragment activeFragment = null;
                    System.out.println(tabLayout.getSelectedTabPosition() == 0 );
                    if (tabLayout.getSelectedTabPosition() == 0 ){
                        activeFragment = fragmentList.get(0);
                        skipSongList = ((CollectionFragment)activeFragment).getActiveSongList();
                    }else if(tabLayout.getSelectedTabPosition() == 1 ){
                        activeFragment = fragmentList.get(1);
                        skipSongList = ((LocalCollectionFragment)activeFragment).getActiveSongList();
                    }
                    int position =mh.getPositionInList();
                    //Check position value to avoid IndexOutOfBounds exception
                    if(position > 0) {
                        position--;
                    }else{
                        position = skipSongList.size() -1;
                    }
                    final String musicFilePath = skipSongList.get(position);
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

            isMusicPlayerInitialized = true;
        }
    }

    public Fragment getVisibleFragment(){
        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if(fragments != null){
            for(Fragment fragment : fragments){
                if(fragment != null && fragment.isVisible())
                    return fragment;
            }
        }
        return null;
    }
}