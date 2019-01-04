

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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class Receiver {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static Random random = new Random();
    private static Frame receivedFrame;
    private static int frame_expected;

    private void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

    }


    public static void protocol5() throws IOException {

        Frame r;

        frame_expected = 0;         /* number of frame expected inbound */


        while (true) {
            if (isFrameReceived()) {
                r = from_physical_layer();
                System.out.println("===Received " + r.toString());
                if (r.seq == frame_expected) {
                    to_network_layer(r);
                    frame_expected = (frame_expected + 1) % (Sender.MAX_SEQ + 1);
                    System.out.println("Sending Ack for Frame: " + r.seq);

                    to_physical_layer(r.seq);
                } else {
                    System.out.println("Error: Expected Frame_Seq=" + frame_expected + ", received Frame_Seq=" + r.seq);

                    dealWithError();

                }
                receivedFrame = null;

            }
            try {
                Thread.sleep(random.nextInt(1500));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }

    }

    private static void dealWithError() throws IOException {
        while (in.ready()){
            in.readLine();
        }
        to_physical_layer(-1);
    }

    private static void to_physical_layer(int ack) {
        Frame dummy = new Frame();

        dummy.ack = ack;
        dummy.seq = frame_expected;
        out.println(dummy.toString());
    }

    private static void to_network_layer(Frame r) {
    }

    private static Frame from_physical_layer() {
        return receivedFrame;
    }

    private static boolean isFrameReceived() {
        String inputLine;
        if (receivedFrame != null) return true;
        try {
            if ((inputLine = in.readLine()) != null) {
                receivedFrame = Frame.createFrame(inputLine);


                // 20 % of received frames are damaged
                if (random.nextInt(5) == 4) {
                    receivedFrame.seq = (receivedFrame.seq + random.nextInt(5)+1) % Sender.MAX_SEQ;
                }

                return true;

            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        receivedFrame = null;
        return false;
    }

    public void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }

    public static void main(String[] args) throws IOException {
        Receiver server = new Receiver();
        server.start(6666);
        protocol5();
    }
}