
/*
 1- ديفيد ابراهيم سلامة
        2-رميز بركات حمزة
        3- اندرو اسحق ابراهيم
        4-مارتينا ايهاب فؤاد
*/

package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
 1- ديفيد ابراهيم سلامة
        2-رميز بركات حمزة
        3- اندرو اسحق ابراهيم
        4-مارتينا ايهاب فؤاد
*/

public class Sender {
    private static Socket clientSocket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static Scanner scanner = new Scanner(System.in);
    public static int MAX_SEQ = 7;
    private static EventType eventType = EventType.NO_EVENT;
    private static Frame receivedFrame;
    private static Random rand = new Random();

    public static void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public static void sendMessage(String msg) throws IOException {
        out.println(msg);

    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    public static void main(String[] args) throws IOException {
        protocol5();
    }

    public static void protocol5() throws IOException {
        int next_frame_to_send = 0;
        int ack_expected = 0;
        int frame_expected = 0;
        Frame r;
        Packet[] buffer = new Packet[MAX_SEQ + 1];
        int nbuffered;
        int i;
        eventType = EventType.NO_EVENT;

        enableNetworkLayer();
        ack_expected = 0;
        next_frame_to_send = 0;
        frame_expected = 0;         /* number of frame expected inbound */
        nbuffered = 0;


        startConnection("127.0.0.1", 6666);


        while (true) {
            try {
                Thread.sleep(rand.nextInt(1200));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            wait_for_event();

            switch (eventType) {
                case NETWORK_LAYER_READY:
                    buffer[next_frame_to_send] = from_network_layer();
                    nbuffered++;
                    send_data(next_frame_to_send, frame_expected, buffer);
                    next_frame_to_send = (next_frame_to_send + 1) % (MAX_SEQ + 1);
                    break;
                case FRAME_ARRIVAL:
                    r = from_physical_layer();
                    if(r.seq ==frame_expected){
                        to_network_layer(r);
                        frame_expected = (frame_expected+1)% (MAX_SEQ+1);
                    }

                    /* Ack n implies n − 1, n − 2, etc. Check for this. */
                    if(r.ack == -1 ){
                        System.out.println("=== Didn't Receive ack for frame No :" +ack_expected);
                        System.out.println("=== Resending Frames");
                        next_frame_to_send = ack_expected;
                        for (i = 1; i <= nbuffered; i++) {
                            send_data(next_frame_to_send, frame_expected, buffer);
                            /* resend frame * / inc(next frame to send);/ * prepare to send the next one */
                            next_frame_to_send = (next_frame_to_send + 1) % (MAX_SEQ + 1);
                        }
                        System.out.println("=== Finished Resending");
                    }
                    else {

                        System.out.println("=== Received Ack for Frame No : " +r.ack);
                        while (between(ack_expected, r.ack, next_frame_to_send)) {
                            nbuffered = nbuffered - 1;
                            //todo stop timer (ack_expected)
                            ack_expected = (ack_expected + 1) % (MAX_SEQ + 1);

                        }
                    }

                    receivedFrame = null;
                    break;
                case CKSUM_ERR:
                    break;

                case TIMEOUT:
                    next_frame_to_send = ack_expected;
                    for (i = 1; i <= nbuffered; i++) {
                        send_data(next_frame_to_send, frame_expected, buffer);
                        /* resend frame * / inc(next frame to send);/ * prepare to send the next one */
                        next_frame_to_send = (next_frame_to_send + 1) % (MAX_SEQ + 1);
                    }
            }
            if (nbuffered < MAX_SEQ+1) {
                enableNetworkLayer();
            } else {
                disableNetworkLayer();
            }

        }

    }

    private static void disableNetworkLayer() {
        eventType = EventType.NO_EVENT;
    }

    private static void enableNetworkLayer() {
        if (rand.nextInt(100) > 70) {
            eventType = EventType.NETWORK_LAYER_READY;
        } else if (receivedFrame != null) {

            eventType = EventType.FRAME_ARRIVAL;
        } else {
            eventType = EventType.NO_EVENT;
        }
    }

    private static boolean between(int ack_expected, int ack, int next_frame_to_send) {
        if (((ack_expected <= ack) && (ack < next_frame_to_send)) ||
                ((next_frame_to_send < ack_expected) && (ack_expected <= ack)) ||
                ((ack < next_frame_to_send) && (next_frame_to_send < ack_expected)))
            return true;
        else return false;
    }

    private static void to_network_layer(Frame r) {
    }

    private static Packet from_network_layer() {
        Packet p = new Packet();
        for (int i = 0; i < Packet.MAX_PACKET_SIZE; i++) {
            p.x[i] = (char) (rand.nextInt(26) + 97);
        }
        //System.out.println("from_network_layer() is called , packet = "+p.toString());
        return p;
    }

    private static Frame from_physical_layer() {

        return receivedFrame;
    }

    private static void checkReceivedFrame (){
        try {
            if (in.ready()&&receivedFrame==null) {
                receivedFrame = Frame.createFrame(in.readLine());
                eventType = EventType.FRAME_ARRIVAL;
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void send_data(int frame_nr, int frame_expected, Packet[] buffer) throws IOException {
        Frame s = new Frame();
        s.info = buffer[frame_nr];
        s.seq = frame_nr;
        s.ack = (frame_expected + MAX_SEQ) % (MAX_SEQ + 1);
        to_physical_layer(s);
        //todo start timer(frame nr);

    }

    private static void to_physical_layer(Frame s) throws IOException {
        // System.out.println("to_physical_layer: "+ s.toString());
        System.out.println("Sending " + s.toString());
       sendMessage(s.toString());


    }


    private static void wait_for_event() {

       checkReceivedFrame();
    }

    enum EventType {
        FRAME_ARRIVAL, CKSUM_ERR, TIMEOUT, NETWORK_LAYER_READY, NO_EVENT
    }
}