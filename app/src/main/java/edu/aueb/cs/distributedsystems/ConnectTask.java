package edu.aueb.cs.distributedsystems;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.List;

class ConnectTask extends AsyncTask<URL, Integer, List<ArtistName>> {

    private final Consumer consumer;
    private final Broker broker;
    private final List<String> activeSongList;

    public ConnectTask(Consumer consumer, Broker broker, List<String> activeSongList){

        this.consumer = consumer;
        this.broker = broker;
        this.activeSongList = activeSongList;
    }

    @Override
    protected List<ArtistName> doInBackground(URL... urls) {
        try {

//            consumer.connect(broker);
            System.out.println("\n" + this.getClass().getSimpleName() + InetAddress.getByName(Globals.publisher_1_ip) + " -> Connecting to " + broker.getClass().getSimpleName() + InetAddress.getByName(Globals.broker_1_ip));
            Socket socket = new Socket("192.168.1.3", 5500);
            consumer.cs = socket;
            System.out.println(this.getClass().getSimpleName() + InetAddress.getByName(Globals.publisher_1_ip) + ":" + consumer.cs.getLocalPort() + " -> Connected to " + broker.getClass().getSimpleName() + consumer.cs.getRemoteSocketAddress());
            consumer.oocs = new ObjectOutputStream(consumer.cs.getOutputStream());
            consumer.oics = new ObjectInputStream(consumer.cs.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            consumer.getArtistList(); //TODO this gets the list
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return consumer.artistList;
    }
}
