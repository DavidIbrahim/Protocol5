/*
 1- ديفيد ابراهيم سلامة
        2-رميز بركات حمزة
        3- اندرو اسحق ابراهيم
        4-مارتينا ايهاب فؤاد
*/

package com.company;

import java.io.Serializable;

public class Frame implements Serializable {
    public int seq;
    public int ack;
    public Packet info;

    public Frame(int seq, int ack, Packet info) {
        this.seq = seq;
        this.ack = ack;
        this.info = info;
    }

    public Frame() {
        seq=0;
        ack=0;
        info = new Packet();
    }

    @Override
    public String toString() {
        return "Frame{" +
                "seq=" + seq +
                ", ack=" + ack +
                ", info=" +info.toString() +
                '}';
    }

    public static Frame createFrame(String frameString){
        Frame f = new Frame();
        f.seq = Integer.parseInt(frameString.substring(frameString.indexOf("=")+1,frameString.indexOf(",")));
        frameString = frameString.substring(frameString.indexOf(",")+1);

        f.ack = Integer.parseInt(frameString.substring(frameString.indexOf("=")+1,frameString.indexOf(",")));
        frameString = frameString.substring(frameString.indexOf(",")+1);
        f.info = new Packet();
        f.info.x = frameString.substring(frameString.indexOf("=")+1,frameString.indexOf("}")).toCharArray();
        return f;

    }


}
