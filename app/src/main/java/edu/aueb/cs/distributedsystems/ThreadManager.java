package edu.aueb.cs.distributedsystems;

import android.app.Activity;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadManager implements Runnable {

    private Thread worker;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private int interval;
    static volatile int  songPosition;
    int songDuration;
    SeekBar seekBar;
    TextView songPositionTextView;
    Activity activity;
    MusicHandler mh;

    public ThreadManager(int interval, final SeekBar seekBar, final TextView songPositionTextView, Activity activity,MusicHandler mh) {
        this.interval = interval;
        this.songPosition = seekBar.getProgress();
        this.songDuration = mh.getDurationDivided();
        this.seekBar = seekBar;
        this.songPositionTextView = songPositionTextView;
        this.activity = activity;
        this.mh = mh;
    }

    public Thread start() {

        if (worker != null){
            running.set(false);
        }

        songPosition = 0;
        seekBar.setProgress(0);
        songPositionTextView.setText(String.format("%02d",0) +":" + String.format("%02d",0));

        worker = new Thread(this);
        worker.start();
        return worker;
    }


    public Thread resume(){
        worker = new Thread(this);
        worker.start();
        return worker;
    }

    public void stop(){
        running.set(false);
    };

    public void interrupt() {
        running.set(false);
        worker.interrupt();
    }

    boolean isRunning() {
        return running.get();
    }

    boolean isStopped() {
        return stopped.get();
    }

    @Override
    public void run() {
        running.set(true);
        stopped.set(false);
        do{
            try {
                worker.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread was interrupted, Failed to complete operation");
            }
            songPosition = mh.getCurrentPositionDivided();

             if (songPosition == songDuration){
                 running.set(false);
             }

            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    seekBar.setProgress(songPosition);
                    songPositionTextView.setText(String.format("%02d",songPosition / 60) +":" + String.format("%02d",songPosition % 60));
                }
            });
        }while(songPosition < songDuration && running.get());

        stopped.set(true);
    }

    public void setValues(int interval, final SeekBar seekBar, final TextView songPositionTextView, Activity activity,MusicHandler mh) {
        this.interval = interval;
        this.songPosition = seekBar.getProgress();
        this.songDuration = mh.getDurationDivided();
        this.seekBar = seekBar;
        this.songPositionTextView = songPositionTextView;
        this.activity = activity;
    }

    public void setSongPosition(int songPosition) {
        this.songPosition = songPosition;
    }
}
