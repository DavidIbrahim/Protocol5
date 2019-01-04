/*
 1- ديفيد ابراهيم سلامة
        2-رميز بركات حمزة
        3- اندرو اسحق ابراهيم
        4-مارتينا ايهاب فؤاد
*/

package com.company;

public class Packet {
    public char[] x;

    public static int MAX_PACKET_SIZE = 5;
    public Packet() {
        x = new char[MAX_PACKET_SIZE];
    }

    @Override
    public String toString() {
        return
                 String.valueOf(x)
                ;
    }
}
