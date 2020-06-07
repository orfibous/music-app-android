package edu.aueb.cs.distributedsystems;

import android.app.Activity;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.renderscript.Sampler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;


import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

class TextAdapter extends BaseAdapter{
    private int item;
    private int text;

    private List<String> activeSongList;
    private MusicHandler mh;
    private ThreadManager songThreadManager;
    private SeekBar seekBar;
    private ImageButton play_pause_button;
    private LinearLayout player_window;
    private TextView songDurationTextView;
    private TextView songTitle;
    private TextView songAuthorName;
    private TextView songPositionTextView;
    private Activity activity;
    private Consumer consumer;
    private Broker broker;
    private String songArtistName;
    private PlayNowTask playNowTask;
    

    public TextAdapter(int item, int text){
        this.item = item;
        this.text = text;
    }

    private List<String> data = new ArrayList<>();

    void setData(List<String> mData){
        data.clear();
        data.addAll(mData);
        notifyDataSetChanged();
    }

    @Override
    public int getCount(){
        return data.size();
    }

    @Override
    public String getItem(int position){
        return null;
    }

    @Override
    public long getItemId(int position){
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){

            convertView = LayoutInflater.from(parent.getContext()).inflate(item, parent, false);
            if(item == R.layout.item_song){
                convertView.setTag(new ViewHolderWithImage((TextView) convertView.findViewById(text)));
            }else {
                convertView.setTag(new ViewHolder((TextView) convertView.findViewById(text)));
            }

            if(item == R.layout.item_song){
                ViewHolderWithImage holder = (ViewHolderWithImage) convertView.getTag();
                final  String item = data.get(position);
                holder.info.setText(item.substring(item.lastIndexOf('/') + 1));
                holder.playNOW = (ImageButton) convertView.findViewById(R.id.song_live_play_button);
                holder.download = (ImageButton) convertView.findViewById(R.id.song_download_button);
                holder.playNOW.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO revert this to stream a song, instead of playing
                        try {
                            File cachePath = new File(Environment.getExternalStorageDirectory()+File.separator+"DistributedSystems");
                            if (cachePath.exists()) {
                                for (File f : cachePath.listFiles()){
                                    f.delete();
                                }
                            }
                            streamMusic(position);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                holder.download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO make this download a song
                        try {
                            downloadMusic(position);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }else{
                ViewHolder holder = (ViewHolder) convertView.getTag();
                final  String item = data.get(position);
                holder.info.setText(item.substring(item.lastIndexOf('/') + 1));
            }
        }

        return convertView;
    }

    private void downloadMusic(int position) throws ExecutionException, InterruptedException {
        List<Value> chunks;
        playNowTask = new PlayNowTask(consumer, broker, activeSongList, position, songArtistName);
        chunks = playNowTask.execute().get();

        String songName = activeSongList.get(position);
        File cachePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)+File.separator+"DistributedSystems");
        if (!cachePath.exists()) {
            cachePath.mkdirs();
        }
        cachePath = new File(cachePath.getAbsolutePath()+ File.separator+songName+".mp3");
        if (!cachePath.exists()) {
            try {
                cachePath.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            return;
        }

        //Write chucks to file
        try {
            FileOutputStream os = new FileOutputStream(cachePath);
            for (Value chunk : chunks){
                os.write(chunk.getMusicFile().getExtract());

            }
            os.close();
            System.out.println("Chunks written to file ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void streamMusic(int position) throws ExecutionException, InterruptedException {
        List<Value> chunks;
        playNowTask = new PlayNowTask(consumer, broker, activeSongList, position, songArtistName);
        chunks = playNowTask.execute().get();

        String songName = activeSongList.get(position);
        File cachePath = new File(Environment.getExternalStorageDirectory()+File.separator+"DistributedSystems");
        if (!cachePath.exists()) {
            cachePath.mkdirs();
        }
        cachePath = new File(cachePath.getAbsolutePath()+ File.separator+songName);
        if (!cachePath.exists()) {
            try {
                cachePath.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Write chucks to file
        try {
            FileOutputStream os = new FileOutputStream(cachePath);
            for (Value chunk : chunks){
                os.write(chunk.getMusicFile().getExtract());

            }
            os.close();
            System.out.println("Chunks written to file ");
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("This is the path of the new song: " + cachePath.getAbsolutePath());

        final int songDuration = mh.createMusicMediaPlayer(cachePath.getAbsolutePath(), player_window, position) / 1000;
                mh.playMusic(play_pause_button);
                seekBar.setMax(songDuration);
                seekBar.setVisibility(View.VISIBLE);
                songDurationTextView.setText(String.format("%02d", songDuration / 60) + ":" + String.format("%02d", songDuration % 60));
                songTitle.setText(mh.getSongMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
                songAuthorName.setText(mh.getSongMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));

                if (songThreadManager.isRunning()){
                    songThreadManager.interrupt();
                }
                songThreadManager.setValues(1000, seekBar, songPositionTextView, activity, mh);
                songThreadManager.start();



    }

    public void setPlayParameters(List<String> activeSongList, MusicHandler mh, ThreadManager songThreadManager, SeekBar seekBar, ImageButton play_pause_button, LinearLayout player_window, TextView songDurationTextView, TextView songTitle, TextView songAuthorName, TextView songPositionTextView, Activity activity, Consumer consumer, Broker broker, String songArtistName){

         this.activeSongList = activeSongList;
         this.mh = mh;
         this.songThreadManager = songThreadManager;
         this.seekBar = seekBar;
         this.play_pause_button = play_pause_button;
         this.player_window = player_window;
         this.songDurationTextView = songDurationTextView;
         this.songTitle = songTitle;
         this.songAuthorName = songAuthorName;
         this.songPositionTextView = songPositionTextView;
         this.activity = activity;
         this.consumer = consumer;
         this.broker = broker;
         this.songArtistName = songArtistName;
    }



    class ViewHolder{
        TextView info;

        ViewHolder(TextView mInfo){
            info = mInfo;
        }
    }

    class ViewHolderWithImage{
        TextView info;
        ImageButton playNOW;
        ImageButton download;

        ViewHolderWithImage(TextView mInfo){
            info = mInfo;
        }
    }
}