package edu.aueb.cs.distributedsystems;

import android.app.Activity;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
                            streamMusic(position);
                    }
                });
                holder.download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO make this download a song
                            downloadMusic(position);
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

    private void downloadMusic(int position){

        playNowTask = new PlayNowTask(consumer, broker, activeSongList, position, songArtistName);
        playNowTask.execute();

        final String songName = activeSongList.get(position);
        File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+songName);

        if (!path.exists()){ //TODO test file creation
            path.mkdirs();
        }

        //TODO get the downloaded file
    }

    private void streamMusic(int position){

        playNowTask = new PlayNowTask(consumer, broker, activeSongList, position, songArtistName);
        playNowTask.execute();

        final String songName = activeSongList.get(position);
        File cachePath = new File(Environment.getExternalStorageDirectory()+"/DistributedSystems/"+songName);

        if (!cachePath.exists()){ //TODO test file creation
            cachePath.mkdirs();
        }
        //TODO play the chunks or file
//        final int songDuration = mh.createAsyncMusicMediaPlayer(musicFilePath, player_window, position) / 1000;
//                mh.playMusic(play_pause_button);
//                seekBar.setMax(songDuration);
//                seekBar.setVisibility(View.VISIBLE);
//                songDurationTextView.setText(String.format("%02d", songDuration / 60) + ":" + String.format("%02d", songDuration % 60));
//                songTitle.setText(mh.getSongMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
//                songAuthorName.setText(mh.getSongMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
//
//                if (songThreadManager.isRunning()){
//                    songThreadManager.interrupt();
//                }
//                songThreadManager.setValues(1000, seekBar, songPositionTextView, activity, mh);
//                songThreadManager.start();

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