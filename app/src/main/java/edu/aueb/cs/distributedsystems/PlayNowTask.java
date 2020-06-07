package edu.aueb.cs.distributedsystems;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.List;

class PlayNowTask extends AsyncTask<URL, Integer, List<Value>> {

    private final Consumer consumer;
    private final Broker broker;
    private final List<String> activeSongList;
    private final int position;
    private final String songArtistName;

    public PlayNowTask(Consumer consumer, Broker broker, List<String> activeSongList, int position, String songArtistName) {

        this.consumer = consumer;
        this.broker = broker;
        this.activeSongList = activeSongList;
        this.position = position;
        this.songArtistName = songArtistName;
    }

    @Override
    protected List<Value> doInBackground(URL... urls) {
        try {

//            consumer.connect(broker);
            System.out.println("\n" + this.getClass().getSimpleName() + InetAddress.getByName(Globals.publisher_1_ip) + " -> Connecting to " + broker.getClass().getSimpleName() + InetAddress.getByName(Globals.broker_1_ip));
            Socket socket = new Socket("192.168.1.3",5500);
            consumer.cs = socket;
            System.out.println(this.getClass().getSimpleName() + InetAddress.getByName(Globals.publisher_1_ip) + ":" + consumer.cs.getLocalPort() + " -> Connected to " +  broker.getClass().getSimpleName() + consumer.cs.getRemoteSocketAddress());
            consumer.oocs = new ObjectOutputStream(consumer.cs.getOutputStream());
            consumer.oics = new ObjectInputStream(consumer.cs.getInputStream());
        }catch (Exception e){
            e.printStackTrace();
        }
            String songName = activeSongList.get(position); //Get song name from list of the artist
            songName = songName.substring(songName.lastIndexOf('/') + 1);
            songName = songName.substring(0,songName.length() -4);
            System.out.println("Requested song = " + songName);
            Request request = new Request(songArtistName, songName);
            try {
                consumer.getArtistList();
                consumer.request(request);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
//        try {
//            consumer.clientDisconnect();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return consumer.currentSong;
    }
}

