package edu.aueb.cs.distributedsystems;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MusicHandler {

    private MediaPlayer mp;
    private boolean playerStopped;
    private MediaMetadataRetriever mmr;
    private int positionInList;

    public MusicHandler(){
        mp = new MediaPlayer();
        mmr = new MediaMetadataRetriever();
        playerStopped = false;
    }

    public List<String> searchSongsOfAuthor(String name, List<String> musicFilesList){
        List<String> list = new ArrayList<>();
        for (String song : musicFilesList){
            mmr.setDataSource(song);
            String artist = this.getSongMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            if (String.valueOf(name).equals(String.valueOf(artist))){
                list.add(song);
            }
        }

        return list;
    }

    public void fillFromBroker(List<ArtistName> artistNameList, List<String> musicAuthorsList, List<String> musicFilesList){
        for (ArtistName artistName : artistNameList){
            musicAuthorsList.add(artistName.getArtist());
            for (String song : artistName.songList){
                musicFilesList.add(song);
            }
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            musicAuthorsList.sort(Comparator.comparing(String::toString));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void fillAuthorList(List<String> musicAuthorsList, List<String> musicFilesList){ //TODO make this print the list based on all broker entries
        musicAuthorsList.clear();
        for (String song : musicFilesList){
            boolean unique = true;
            mmr.setDataSource(song);
            String artist = this.getSongMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            for(String name : musicAuthorsList){
                if (String.valueOf(name).equals(String.valueOf(artist))) unique = false;
            }
            if (unique){
                musicAuthorsList.add(artist);
            }

        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            musicAuthorsList.sort(Comparator.comparing(String::toString));
        }

    }

    public void fillMusicList(List<String> musicFilesList){
        musicFilesList.clear();
        traverseFileRecursively(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),musicFilesList);
        traverseFileRecursively(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),musicFilesList);
    }

    public void addMusicFilesFrom(File file,  List<String> musicFilesList){
        final String path = file.getAbsolutePath();
        if(path.endsWith(".mp3") || path.endsWith(".wav")|| path.endsWith(".wma")|| path.endsWith(".flac")){
            musicFilesList.add(path);
        }
    }

    public void traverseFileRecursively(File directory,List<String> musicFilesList){
        if (directory.exists() && directory.isDirectory()){
            File [] files = directory.listFiles();
            for(File f : files ){
                if(f.isDirectory()){
                    traverseFileRecursively(f, musicFilesList);
                }else {
                    addMusicFilesFrom(f, musicFilesList);
                }
            }
        }
    }

    public String getSongMetadata(int field){
        String value = mmr.extractMetadata(field);
        if (value == null) return "Unknown";
        if (value == "") return "Unknown";
        if (value == " ") return "Unknown";

        return value;
    }

    //Used for playing a song from local storage
    public int createMusicMediaPlayer(String path, LinearLayout player_window, int position){

        positionInList = position;

        player_window.setVisibility(View.VISIBLE);
        try{
            mmr.setDataSource(path);
            mp.reset();
            mp.setDataSource(path);
            mp.prepare();
        }catch (Exception e){
            e.printStackTrace();
        }
        return mp.getDuration();
    }

    //Used for playing a song sent from the server
//    public int createAsyncMusicMediaPlayer(String URL, LinearLayout player_window, int position){
//
//        positionInList = position;
//
//        player_window.setVisibility(View.VISIBLE);
//        try{
//            mmr.setDataSource(URL);
//            mp.reset();
//            mp.setDataSource(URL);
//            mp.prepareAsync();
//            mp.setOnPreparedListener((MediaPlayer.OnPreparedListener) mp);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return mp.getDuration();
//    }

    public void playMusic(ImageButton imgbutton){
        if(mp != null){
            try {
                mp.start();
                imgbutton.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                playerStopped = false;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void playMusicAsync(ImageButton imgbutton){
        if(mp != null){
            try {
                imgbutton.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                playerStopped = false;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void pauseMusic(ImageButton imgbutton){
        if(mp != null){
            try {
                mp.pause();
                imgbutton.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                playerStopped = true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void stopMusic(ImageButton imgbutton, LinearLayout player_window){
        if(mp != null){
            try{
                mp.release();
                imgbutton.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);

                player_window.setVisibility(View.GONE);

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void seekTo(int songProgress){
        mp.seekTo(songProgress);
    }

    public boolean playerStopped(){
        return playerStopped;
    }

    public boolean isPlaying(){
        return mp.isPlaying();
    }

    public int getDuration(){
        return mp.getDuration();
    }

    public int getDurationDivided(){
        return mp.getDuration() /1000;
    }

    public int getPositionInList(){
        return positionInList;
    }

    public MediaPlayer getMp() {
        return mp;
    }

    public int getCurrentPositionDivided(){
        return mp.getCurrentPosition() / 1000;
    }
}
